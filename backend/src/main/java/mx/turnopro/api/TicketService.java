package mx.turnopro.api;

import mx.turnopro.domain.Models.*;
import mx.turnopro.domain.Repositories.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class TicketService {
    private final Tickets tickets; private final Services services; private final History history; private final SimpMessagingTemplate ws; private final SecureRandom random=new SecureRandom();
    public TicketService(Tickets tickets,Services services,History history,SimpMessagingTemplate ws){this.tickets=tickets;this.services=services;this.history=history;this.ws=ws;}
    public record CreateTicket(String name, Long serviceId, String phone) {}
    public record TicketView(Long id,String number,String service,String status,long ahead,int estimatedMinutes,LocalDateTime createdAt,String consultationCode,String counter,String name) {}
    @Transactional public TicketView create(CreateTicket request){
        if(request.name()==null||request.name().isBlank()||request.name().length()>100)throw new IllegalArgumentException("El nombre es obligatorio (máximo 100 caracteres).");
        ServiceEntity service=services.lockById(request.serviceId()).filter(s->s.activo).orElseThrow(()->new IllegalArgumentException("Servicio no disponible.")); LocalDate day=LocalDate.now(); int seq=tickets.maxSequence(service.id,day)+1;
        TicketEntity t=new TicketEntity();t.servicio=service;t.secuencial=seq;t.numero=service.prefijo+String.format("%03d",seq);t.fechaOperativa=day;t.clienteNombre=request.name().trim();t.clienteTelefono=request.phone();t.codigoConsulta=token();t.creadoEn=LocalDateTime.now();TicketEntity saved=tickets.save(t);record(saved,null,saved.estado,"Solicitud pública");ws.convertAndSend("/topic/queue",Map.of("event","created","number",saved.numero));return view(saved);
    }
    private String token(){byte[] b=new byte[24];random.nextBytes(b);return Base64.getUrlEncoder().withoutPadding().encodeToString(b);}
    public TicketView lookup(String number,String code){TicketEntity t=tickets.findByCodigoConsultaAndNumero(code,number).orElseThrow(()->new NoSuchElementException("No se encontró el turno."));return view(t);}
    private TicketView view(TicketEntity t){long ahead=tickets.ahead(t.fechaOperativa,t.servicio.id,List.of(Status.ESPERA,Status.LLAMADO),t.prioridad,t.secuencial);return new TicketView(t.id,t.numero,t.servicio.nombre,t.estado.name(),ahead,(int)ahead*t.servicio.tiempoPromedioMin,t.creadoEn,t.codigoConsulta,t.ventanillaId==null?null:"Ventanilla "+t.ventanillaId,t.clienteNombre);}
    public List<TicketView> waiting(){return tickets.findByEstadoOrderByPrioridadDescCreadoEnAsc(Status.ESPERA).stream().map(this::view).toList();}
    public List<Map<String,Object>> history(Long ticketId){if(!tickets.existsById(ticketId))throw new NoSuchElementException("Turno no encontrado.");return history.findByTurnoIdOrderByCreadoEnAsc(ticketId).stream().map(h->{Map<String,Object> m=new LinkedHashMap<>();m.put("from",h.estadoAnterior);m.put("to",h.estadoNuevo);m.put("detail",h.detalle);m.put("createdAt",h.creadoEn);return m;}).toList();}
    @Transactional public TicketView transition(Long id,String action,String actor){TicketEntity t=tickets.findById(id).orElseThrow(()->new NoSuchElementException("Turno no encontrado."));Status previous=t.estado;LocalDateTime now=LocalDateTime.now();switch(action){case "llamar"->{if(t.estado!=Status.ESPERA)throw new IllegalStateException("Solo pueden llamarse turnos en espera.");t.estado=Status.LLAMADO;t.llamadoEn=now;}case "iniciar"->{if(t.estado!=Status.LLAMADO)throw new IllegalStateException("Primero debe llamarse el turno.");t.estado=Status.EN_ATENCION;t.inicioAtencion=now;}case "finalizar"->{if(t.estado!=Status.EN_ATENCION)throw new IllegalStateException("El turno no está en atención.");t.estado=Status.ATENDIDO;t.finAtencion=now;}case "ausente"->{if(t.estado!=Status.LLAMADO)throw new IllegalStateException("Solo un turno llamado puede marcarse ausente.");t.estado=Status.AUSENTE;}case "cancelar"->{if(t.estado==Status.ATENDIDO||t.estado==Status.CANCELADO)throw new IllegalStateException("Este turno ya no se puede cancelar.");t.estado=Status.CANCELADO;}default->throw new IllegalArgumentException("Acción no válida.");}TicketEntity saved=tickets.save(t);record(saved,previous,saved.estado,"Acción "+action+" por "+actor);ws.convertAndSend("/topic/queue",Map.of("event","status","number",saved.numero,"status",saved.estado.name()));return view(saved);}
    private void record(TicketEntity ticket,Status before,Status after,String detail){TurnHistoryEntity h=new TurnHistoryEntity();h.turno=ticket;h.estadoAnterior=before==null?null:before.name();h.estadoNuevo=after.name();h.detalle=detail;h.creadoEn=LocalDateTime.now();history.save(h);}
    public List<TicketView> publicCalls(){return tickets.findTop8ByEstadoInOrderByLlamadoEnDesc(List.of(Status.LLAMADO,Status.EN_ATENCION)).stream().map(t->new TicketView(t.id,t.numero,t.servicio.nombre,t.estado.name(),0,0,t.creadoEn,null,t.ventanillaId==null?null:"Ventanilla "+t.ventanillaId,null)).toList();}
    public Map<String,Object> summary(){LocalDate d=LocalDate.now();Map<String,Object> data=new HashMap<>();data.put("today",tickets.countByFechaOperativa(d));data.put("waiting",tickets.countByFechaOperativaAndEstado(d,Status.ESPERA));data.put("inProgress",tickets.countByFechaOperativaAndEstado(d,Status.EN_ATENCION));data.put("completed",tickets.countByFechaOperativaAndEstado(d,Status.ATENDIDO));data.put("cancelled",tickets.countByFechaOperativaAndEstado(d,Status.CANCELADO));data.put("avgWait",tickets.averageWait(d,Status.ATENDIDO));data.put("avgService",tickets.averageService(d,Status.ATENDIDO));return data;}
}
