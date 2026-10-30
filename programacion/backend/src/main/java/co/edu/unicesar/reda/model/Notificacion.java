package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

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
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_lectura")
    private OffsetDateTime fechaLectura;

    protected Notificacion() {
    }

    public static Notificacion cambioDeReporte(Usuario destinatario, HistorialEstado historial) {
        Notificacion notificacion = new Notificacion();
        notificacion.destinatario = destinatario;
        notificacion.historial = historial;
        notificacion.fechaCreacion = OffsetDateTime.now();
        return notificacion;
    }

    public void marcarLeida() {
        if (fechaLectura == null) fechaLectura = OffsetDateTime.now();
    }

    public Integer getId() { return id; }
    public Usuario getDestinatario() { return destinatario; }
    public HistorialEstado getHistorial() { return historial; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaLectura() { return fechaLectura; }
}
