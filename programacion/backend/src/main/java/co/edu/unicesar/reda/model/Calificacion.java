package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** Calificación del ciudadano al abogado que llevó su caso. Solo la crea {@link Caso#calificar}. */
@Entity
@Table(name = "calificaciones")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_calificacion")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caso")
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_abogado")
    private Abogado abogado;

    @Column(name = "puntaje", nullable = false)
    private Short puntaje;

    @Column(name = "comentario", columnDefinition = "text")
    private String comentario;

    @Column(name = "fecha", nullable = false)
    private OffsetDateTime fecha;

    protected Calificacion() {
    }

    static Calificacion de(Caso caso, Abogado abogado, short puntaje, String comentario) {
        Calificacion c = new Calificacion();
        c.caso = caso;
        c.abogado = abogado;
        c.puntaje = puntaje;
        c.comentario = comentario == null || comentario.isBlank() ? null : comentario.trim();
        c.fecha = OffsetDateTime.now();
        return c;
    }

    public Integer getId() { return id; }
    public Caso getCaso() { return caso; }
    public Abogado getAbogado() { return abogado; }
    public Short getPuntaje() { return puntaje; }
    public String getComentario() { return comentario; }
    public OffsetDateTime getFecha() { return fecha; }
}
