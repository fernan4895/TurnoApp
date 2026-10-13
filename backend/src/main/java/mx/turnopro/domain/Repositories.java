package mx.turnopro.domain;

import mx.turnopro.domain.Models.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.*;

public final class Repositories {
    private Repositories() {}
    @Repository public interface Services extends JpaRepository<ServiceEntity,Long> { List<ServiceEntity> findByActivoTrueOrderByOrdenVisualizacionAsc(); @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from ServiceEntity s where s.id=:id") Optional<ServiceEntity> lockById(@Param("id") Long id); }
    @Repository public interface Tickets extends JpaRepository<TicketEntity,Long> {
        @Query("select coalesce(max(t.secuencial),0) from TicketEntity t where t.servicio.id=:service and t.fechaOperativa=:day") int maxSequence(@Param("service") Long service,@Param("day") LocalDate day);
        List<TicketEntity> findByEstadoOrderByPrioridadDescCreadoEnAsc(Status estado);
        Optional<TicketEntity> findByCodigoConsultaAndNumero(String code,String number);
        long countByFechaOperativa(LocalDate date);
        long countByFechaOperativaAndEstado(LocalDate date,Status status);
        @Query("select avg(function('timestampdiff', second, t.llamadoEn, t.inicioAtencion)) / 60.0 from TicketEntity t where t.fechaOperativa=:day and t.estado=:status and t.llamadoEn is not null and t.inicioAtencion is not null") Double averageWait(@Param("day") LocalDate day,@Param("status") Status status);
        @Query("select avg(function('timestampdiff', second, t.inicioAtencion, t.finAtencion)) / 60.0 from TicketEntity t where t.fechaOperativa=:day and t.estado=:status and t.inicioAtencion is not null and t.finAtencion is not null") Double averageService(@Param("day") LocalDate day,@Param("status") Status status);
        @Query("select count(t) from TicketEntity t where t.fechaOperativa=:day and t.servicio.id=:service and t.estado in :states and (t.prioridad > :priority or (t.prioridad=:priority and t.secuencial < :sequence))") long ahead(@Param("day") LocalDate day,@Param("service") Long service,@Param("states") Collection<Status> states,@Param("priority") int priority,@Param("sequence") int sequence);
        List<TicketEntity> findTop8ByEstadoInOrderByLlamadoEnDesc(Collection<Status> states);
    }
    @Repository public interface History extends JpaRepository<TurnHistoryEntity,Long> { List<TurnHistoryEntity> findByTurnoIdOrderByCreadoEnAsc(Long ticketId); }
}
