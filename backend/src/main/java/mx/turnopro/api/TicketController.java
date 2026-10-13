package mx.turnopro.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import mx.turnopro.domain.Models.*;
import mx.turnopro.domain.Repositories.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api") public class TicketController {
    private final TicketService tickets; private final Services services;
    public TicketController(TicketService tickets,Services services){this.tickets=tickets;this.services=services;}
    public record CreateRequest(@NotBlank @Size(max=100) String name,@NotNull Long serviceId,@Size(max=24) String phone){}
    @GetMapping("/health") public Map<String,String> health(){return Map.of("status","UP");}
    @GetMapping("/servicios") public List<Map<String,Object>> services(){return services.findByActivoTrueOrderByOrdenVisualizacionAsc().stream().map(s->Map.<String,Object>of("id",s.id,"name",s.nombre,"description",s.descripcion,"prefix",s.prefijo,"averageMinutes",s.tiempoPromedioMin)).toList();}
    @PostMapping("/turnos") public TicketService.TicketView create(@Valid @RequestBody CreateRequest r){return tickets.create(new TicketService.CreateTicket(r.name(),r.serviceId(),r.phone()));}
    @GetMapping("/turnos/consulta/{code}") public TicketService.TicketView lookup(@PathVariable String code,@RequestParam @NotBlank String number){return tickets.lookup(number,code);}
    @GetMapping("/turnos/espera") @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','AGENTE')") public Map<String,Object> waiting(){return Map.of("content",tickets.waiting());}
    @GetMapping("/turnos/{id}/historial") @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','AGENTE')") public List<Map<String,Object>> history(@PathVariable Long id){return tickets.history(id);}
    @PostMapping("/turnos/{id}/{action}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR','AGENTE')") public TicketService.TicketView transition(@PathVariable Long id,@PathVariable String action,Authentication auth){return tickets.transition(id,action,auth.getName());}
    @GetMapping("/pantalla/llamados") public List<TicketService.TicketView> calls(){return tickets.publicCalls();}
    @GetMapping("/reportes/resumen") @PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')") public Map<String,Object> summary(){return tickets.summary();}
}
