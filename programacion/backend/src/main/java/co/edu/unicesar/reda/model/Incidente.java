package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Daño sufrido a causa de una falla (especialización 1:1 de un Reporte de tipo INCIDENTE).
 * Contiene datos SENSIBLES de salud (descripción del daño, atención médica).
 */
@Entity
@Table(name = "incidentes")
public class Incidente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidente")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reporte")
    private Reporte reporte;

    /** Siempre INCIDENTE: existe para la FK compuesta que garantiza el tipo del reporte en la base. */
    @Column(name = "tipo_reporte", nullable = false, length = 10, updatable = false)
    private String tipoReporte = TipoReporte.INCIDENTE.name();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_entidad")
    private EntidadPublica entidad;

    @Column(name = "fecha_hora_hecho", nullable = false)
    private OffsetDateTime fechaHoraHecho;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dano", nullable = false, length = 15)
    private TipoDano tipoDano;

    @Column(name = "descripcion_dano", nullable = false, columnDefinition = "text")
    private String descripcionDano;

    @Column(name = "hubo_atencion_medica", nullable = false)
    private boolean huboAtencionMedica;

    @OneToOne(mappedBy = "incidente", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Caso caso;

    @OneToMany(mappedBy = "incidente", cascade = CascadeType.PERSIST)
    private List<Testigo> testigos = new ArrayList<>();

    protected Incidente() {
    }

    /** Creador (GRASP): el incidente agrega su caso, así que él lo crea. Un incidente, un caso. */
    public Caso solicitarAbogado(Usuario solicitante, Usuario operadorQueValido, EstadoCaso remitido,
                                 boolean autorizaCompartir) {
        if (caso != null) {
            throw new ReglaNegocioException("CASO_EXISTENTE", "Este incidente ya tiene un caso abierto");
        }
        this.caso = Caso.abrir(this, solicitante, operadorQueValido, remitido, autorizaCompartir);
        return caso;
    }

    public Testigo agregarTestigo(String nombre, String contacto) {
        Testigo t = Testigo.de(this, nombre, contacto);
        testigos.add(t);
        return t;
    }

    static Incidente de(Reporte reporte, TipoDano tipoDano, String descripcionDano, OffsetDateTime fechaHoraHecho,
                        boolean huboAtencionMedica, EntidadPublica entidad) {
        if (fechaHoraHecho.isAfter(reporte.getFechaReporte())) {
            throw new ReglaNegocioException("FECHA_HECHO_FUTURA", "La fecha del hecho no puede ser posterior al reporte");
        }
        if (huboAtencionMedica && !tipoDano.incluyeLesion()) {
            throw new ReglaNegocioException("ATENCION_SIN_LESION",
                    "Solo se registra atención médica cuando el daño incluye una lesión");
        }
        Incidente i = new Incidente();
        i.reporte = reporte;
        i.tipoDano = tipoDano;
        i.descripcionDano = descripcionDano.trim();
        i.fechaHoraHecho = fechaHoraHecho;
        i.huboAtencionMedica = huboAtencionMedica;
        i.entidad = entidad;
        return i;
    }

    public Integer getId() { return id; }
    public Reporte getReporte() { return reporte; }
    public EntidadPublica getEntidad() { return entidad; }
    public OffsetDateTime getFechaHoraHecho() { return fechaHoraHecho; }
    public TipoDano getTipoDano() { return tipoDano; }
    public String getDescripcionDano() { return descripcionDano; }
    public boolean isHuboAtencionMedica() { return huboAtencionMedica; }
    public Caso getCaso() { return caso; }
    public List<Testigo> getTestigos() { return Collections.unmodifiableList(testigos); }
}
