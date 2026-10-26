package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Asignación facturable de un caso a un abogado. Con el flujo de postulaciones, una remisión se crea
 * ACEPTADA en el momento en que el ciudadano elige al abogado (eso es lo que se cobra).
 * Solo la crea {@link Caso#elegir}.
 */
@Entity
@Table(name = "remisiones")
public class Remision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_remision")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_caso")
    private Caso caso;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_abogado")
    private Abogado abogado;

    @Column(name = "fecha_envio", nullable = false)
    private OffsetDateTime fechaEnvio;

    @Column(name = "estado_remision", nullable = false, length = 10)
    private String estadoRemision;

    @Column(name = "fecha_respuesta")
    private OffsetDateTime fechaRespuesta;

    @Column(name = "motivo_rechazo", columnDefinition = "text")
    private String motivoRechazo;

    @Column(name = "valor_cobrado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorCobrado;

    protected Remision() {
    }

    static Remision aceptada(Caso caso, Abogado abogado, BigDecimal valor, OffsetDateTime cuando) {
        Remision r = new Remision();
        r.caso = caso;
        r.abogado = abogado;
        r.fechaEnvio = cuando;
        r.fechaRespuesta = cuando;
        r.estadoRemision = "ACEPTADA";
        r.valorCobrado = valor;
        return r;
    }

    public boolean estaAceptada() {
        return "ACEPTADA".equals(estadoRemision);
    }

    public Integer getId() { return id; }
    public Abogado getAbogado() { return abogado; }
    public OffsetDateTime getFechaEnvio() { return fechaEnvio; }
    public String getEstadoRemision() { return estadoRemision; }
    public BigDecimal getValorCobrado() { return valorCobrado; }
}
