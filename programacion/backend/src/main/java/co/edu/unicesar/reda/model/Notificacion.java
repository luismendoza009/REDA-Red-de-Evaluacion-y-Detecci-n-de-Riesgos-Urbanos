package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "notificaciones", uniqueConstraints = {
        @UniqueConstraint(name = "uq_notificaciones_usuario_historial", columnNames = {"id_usuario", "id_historial"})
})
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialEstado historial;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_lectura")
    private LocalDate fechaLectura;

    protected Notificacion() {
    }

    public static Notificacion cambioDeReporte(Usuario destinatario, HistorialEstado historial) {
        Notificacion notificacion = new Notificacion();
        notificacion.destinatario = destinatario;
        notificacion.historial = historial;
        notificacion.fechaCreacion = LocalDate.now(ZoneId.of("America/Bogota"));
        return notificacion;
    }

    public void marcarLeida() {
        if (fechaLectura == null) fechaLectura = LocalDate.now(ZoneId.of("America/Bogota"));
    }

    public Integer getId() { return id; }
    public Usuario getDestinatario() { return destinatario; }
    public HistorialEstado getHistorial() { return historial; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public LocalDate getFechaLectura() { return fechaLectura; }
}
