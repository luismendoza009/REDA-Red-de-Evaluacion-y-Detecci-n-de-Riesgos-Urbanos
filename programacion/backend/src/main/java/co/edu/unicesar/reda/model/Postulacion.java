package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.ZoneId;

/** Interés de un abogado en un caso. Solo la crea {@link Caso#postular}. */
@Entity
@Table(name = "postulaciones")
public class Postulacion {

    public enum Estado { PENDIENTE, ELEGIDA, NO_ELEGIDA, RETIRADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulacion")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caso")
    private Caso caso;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_abogado")
    private Abogado abogado;

    @Column(name = "mensaje", nullable = false, columnDefinition = "text")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private Estado estado;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDate fechaPostulacion;

    @Column(name = "fecha_resolucion")
    private LocalDate fechaResolucion;

    protected Postulacion() {
    }

    static Postulacion de(Caso caso, Abogado abogado, String mensaje) {
        Postulacion p = new Postulacion();
        p.caso = caso;
        p.abogado = abogado;
        p.mensaje = mensaje.trim();
        p.estado = Estado.PENDIENTE;
        p.fechaPostulacion = LocalDate.now(ZoneId.of("America/Bogota"));
        return p;
    }

    void resolver(Estado nuevo, LocalDate cuando) {
        this.estado = nuevo;
        this.fechaResolucion = cuando;
    }

    public boolean estaPendiente() {
        return estado == Estado.PENDIENTE;
    }

    public Integer getId() { return id; }
    public Caso getCaso() { return caso; }
    public Abogado getAbogado() { return abogado; }
    public String getMensaje() { return mensaje; }
    public Estado getEstado() { return estado; }
    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public LocalDate getFechaResolucion() { return fechaResolucion; }
}
