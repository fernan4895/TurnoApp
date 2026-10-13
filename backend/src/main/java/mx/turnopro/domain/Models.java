package mx.turnopro.domain;

import jakarta.persistence.*;
import java.time.*;

public final class Models {
    private Models() {}
    public enum Status { ESPERA, LLAMADO, EN_ATENCION, ATENDIDO, CANCELADO, AUSENTE }
    @Entity @Table(name="servicios") public static class ServiceEntity {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @Column(nullable=false,unique=true,length=80) public String nombre;
        @Column(nullable=false,length=180) public String descripcion="";
        @Column(nullable=false,length=5) public String prefijo;
        @Column(name="tiempo_promedio_min",nullable=false) public int tiempoPromedioMin=10;
        @Column(nullable=false) public boolean activo=true;
        @Column(nullable=false) public int prioridad=0;
        @Column(name="orden_visualizacion",nullable=false) public int ordenVisualizacion=0;
    }
    @Entity @Table(name="turnos",uniqueConstraints=@UniqueConstraint(name="uq_turno_operativo",columnNames={"sucursal_id","servicio_id","fecha_operativa","numero_secuencial"}),indexes={@Index(name="idx_turnos_fila",columnList="sucursal_id,servicio_id,estado,prioridad,creado_en")}) public static class TicketEntity {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @Column(name="numero_turno",nullable=false,length=16) public String numero;
        @Column(name="numero_secuencial",nullable=false) public int secuencial;
        @Column(name="fecha_operativa",nullable=false) public LocalDate fechaOperativa;
        @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="servicio_id") public ServiceEntity servicio;
        @Column(name="sucursal_id",nullable=false) public long sucursalId=1;
        @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) public Status estado=Status.ESPERA;
        @Column(nullable=false) public int prioridad=0;
        @Column(name="cliente_nombre",nullable=false,length=100) public String clienteNombre;
        @Column(name="cliente_telefono",length=24) public String clienteTelefono;
        @Column(name="codigo_consulta",nullable=false,unique=true,length=64) public String codigoConsulta;
        @Column(name="ventanilla_id") public Long ventanillaId;
        @Column(name="creado_en",nullable=false) public LocalDateTime creadoEn;
        @Column(name="llamado_en") public LocalDateTime llamadoEn;
        @Column(name="inicio_atencion") public LocalDateTime inicioAtencion;
        @Column(name="fin_atencion") public LocalDateTime finAtencion;
        @Column(name="motivo_cancelacion",length=250) public String motivoCancelacion;
        @Version public long version;
    }
    @Entity @Table(name="historial_turnos",indexes=@Index(name="idx_historial_turno",columnList="turno_id,creado_en")) public static class TurnHistoryEntity {
        @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
        @ManyToOne(optional=false,fetch=FetchType.LAZY) @JoinColumn(name="turno_id") public TicketEntity turno;
        @Column(name="estado_anterior",length=16) public String estadoAnterior;
        @Column(name="estado_nuevo",nullable=false,length=16) public String estadoNuevo;
        @Column(name="usuario_id") public Long usuarioId;
        @Column(length=250) public String detalle;
        @Column(name="creado_en",nullable=false) public LocalDateTime creadoEn;
    }
}
