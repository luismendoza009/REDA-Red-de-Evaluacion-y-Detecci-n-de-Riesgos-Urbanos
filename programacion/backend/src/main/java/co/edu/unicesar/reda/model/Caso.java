package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Expediente legal que nace de un incidente cuando el ciudadano pide abogado.
 *
 * Ciclo:  Remitido (recibe postulaciones) --el ciudadano elige--> Aceptado por abogado --el abogado cierra--> Cerrado
 *
 * GRASP: el caso es el EXPERTO en sus reglas (quién puede postularse, quién elige, quién es el
 * abogado asignado) y el CREADOR de sus postulaciones y remisiones, que agrega.
 */
@Entity
@Table(name = "casos")
public class Caso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_caso")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incidente")
    private Incidente incidente;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_estado_caso")
    private EstadoCaso estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_operador")
    private Usuario operador;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "observacion", columnDefinition = "text")
    private String observacion;

    @Column(name = "fecha_consentimiento")
    private OffsetDateTime fechaConsentimiento;

    @OneToMany(mappedBy = "caso", cascade = CascadeType.PERSIST)
    @OrderBy("fechaPostulacion ASC")
    private List<Postulacion> postulaciones = new ArrayList<>();

    @OneToMany(mappedBy = "caso", cascade = CascadeType.PERSIST)
    private List<Remision> remisiones = new ArrayList<>();

    protected Caso() {
    }

    /**
     * El ciudadano pide abogado para su incidente. Reglas: solo el autor, solo incidentes ya
     * VALIDADOS por un operador, y con su autorización expresa para mostrar el resumen anónimo.
     */
    static Caso abrir(Incidente incidente, Usuario solicitante, Usuario operadorQueValido, EstadoCaso remitido,
                      boolean autorizaCompartir) {
        Reporte reporte = incidente.getReporte();
        if (!reporte.esDe(solicitante)) {
            throw new ReglaNegocioException("NO_ES_AUTOR", "Solo quien reportó el incidente puede pedir abogado");
        }
        if (reporte.getEstado().tipo() != NombreEstado.VALIDADO && reporte.getEstado().tipo() != NombreEstado.EN_PROCESO) {
            throw new ReglaNegocioException("INCIDENTE_NO_VALIDADO",
                    "Un operador debe validar el incidente antes de buscar abogado (estado actual: " + reporte.getEstado().getNombre() + ")");
        }
        if (!autorizaCompartir) {
            throw new ReglaNegocioException("SIN_AUTORIZACION", "Debe autorizar que los abogados vean el resumen anónimo del caso");
        }
        Caso c = new Caso();
        c.incidente = incidente;
        c.operador = operadorQueValido;
        c.estado = remitido;
        c.fechaCreacion = OffsetDateTime.now();
        c.fechaConsentimiento = c.fechaCreacion;
        c.observacion = "Caso abierto a solicitud del ciudadano.";
        return c;
    }

    /** Un abogado verificado manifiesta interés. */
    public Postulacion postular(Abogado abogado, String mensaje) {
        if (!abogado.isActivo()) {
            throw new ReglaNegocioException("ABOGADO_NO_VERIFICADO", "Su cuenta de abogado aún no ha sido verificada");
        }
        if (estado.tipo() != EstadoCaso.Nombre.REMITIDO) {
            throw new ReglaNegocioException("CASO_NO_DISPONIBLE", "Este caso ya no recibe postulaciones");
        }
        if (postulacionDe(abogado).isPresent()) {
            throw new ReglaNegocioException("YA_POSTULADO", "Ya se postuló a este caso");
        }
        Postulacion p = Postulacion.de(this, abogado, mensaje);
        postulaciones.add(p);
        return p;
    }

    /** El abogado desiste antes de que el ciudadano decida. */
    public void retirarPostulacion(Abogado abogado) {
        Postulacion p = postulacionDe(abogado).filter(Postulacion::estaPendiente).orElseThrow(() ->
                new ReglaNegocioException("SIN_POSTULACION", "No tiene una postulación pendiente en este caso"));
        p.resolver(Postulacion.Estado.RETIRADA, OffsetDateTime.now());
    }

    /**
     * El ciudadano ELIGE a su abogado. En una sola operación: la postulación queda ELEGIDA, las demás
     * NO_ELEGIDA, se crea la remisión ACEPTADA (lo facturable) y el caso pasa a "Aceptado por abogado".
     */
    public Remision elegir(Integer idPostulacion, Usuario ciudadano, EstadoCaso aceptado) {
        if (!incidente.getReporte().esDe(ciudadano)) {
            throw new ReglaNegocioException("NO_ES_AUTOR", "Solo el ciudadano dueño del caso puede elegir abogado");
        }
        if (estado.tipo() != EstadoCaso.Nombre.REMITIDO) {
            throw new ReglaNegocioException("CASO_NO_DISPONIBLE", "Este caso ya tiene abogado o está cerrado");
        }
        Postulacion elegida = postulaciones.stream()
                .filter(p -> p.getId().equals(idPostulacion) && p.estaPendiente())
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("POSTULACION_INVALIDA", "Esa postulación no está disponible"));
        OffsetDateTime ahora = OffsetDateTime.now();
        for (Postulacion p : postulaciones) {
            if (p.estaPendiente()) {
                p.resolver(p == elegida ? Postulacion.Estado.ELEGIDA : Postulacion.Estado.NO_ELEGIDA, ahora);
            }
        }
        Remision remision = Remision.aceptada(this, elegida.getAbogado(), TarifaRemision.para(incidente.getTipoDano()), ahora);
        remisiones.add(remision);
        this.estado = aceptado;
        return remision;
    }

    /** El abogado asignado da por terminado el caso. */
    public void cerrar(Usuario abogadoUsuario, EstadoCaso cerrado, String observacion) {
        if (!esAbogadoAsignado(abogadoUsuario)) {
            throw new ReglaNegocioException("NO_ES_ABOGADO_DEL_CASO", "Solo el abogado asignado puede cerrar el caso");
        }
        if (estado.tipo() != EstadoCaso.Nombre.ACEPTADO) {
            throw new ReglaNegocioException("CASO_NO_ACTIVO", "Solo se puede cerrar un caso en curso");
        }
        this.estado = cerrado;
        this.fechaCierre = OffsetDateTime.now();
        this.observacion = observacion.trim();
    }

    public Optional<Abogado> abogadoAsignado() {
        return remisiones.stream().filter(Remision::estaAceptada).map(Remision::getAbogado).findFirst();
    }

    public boolean esAbogadoAsignado(Usuario usuario) {
        return abogadoAsignado().map(a -> a.esUsuario(usuario)).orElse(false);
    }

    public Optional<Postulacion> postulacionDe(Abogado abogado) {
        return postulaciones.stream().filter(p -> p.getAbogado().getId().equals(abogado.getId())).findFirst();
    }

    public Integer getId() { return id; }
    public Incidente getIncidente() { return incidente; }
    public EstadoCaso getEstado() { return estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaCierre() { return fechaCierre; }
    public String getObservacion() { return observacion; }
    public List<Postulacion> getPostulaciones() { return Collections.unmodifiableList(postulaciones); }
}
