package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidad central: una falla de infraestructura reportada por un ciudadano.
 *
 * GRASP aplicado aquí:
 *  - Experto en información: el reporte conoce su estado y su historial, así que es él quien
 *    cambia de estado y deja constancia (cambiarEstado).
 *  - Creador: el reporte AGREGA sus cambios de estado y su incidente, así que él los crea
 *    (ningún servicio hace "new HistorialEstado").
 * No tiene setters públicos: el estado solo cambia por operaciones con significado de negocio.
 */
@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario autor;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_zona")
    private Zona zona;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_estado")
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_reporte", nullable = false, length = 10)
    private TipoReporte tipo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(name = "latitud", nullable = false, precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", nullable = false, precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "fecha_reporte", nullable = false)
    private LocalDate fechaReporte;

    @Column(name = "nivel_gravedad")
    private Short nivelGravedad;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<HistorialEstado> historial = new ArrayList<>();

    @OneToOne(mappedBy = "reporte", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Incidente incidente;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.PERSIST)
    @OrderBy("fechaCarga ASC")
    private List<Evidencia> evidencias = new ArrayList<>();

    protected Reporte() {
    }

    /** Fábrica común: todo reporte nace en estado Pendiente. */
    public static Reporte nuevo(TipoReporte tipo, Usuario autor, Categoria categoria, Zona zona, Estado pendiente,
                                String descripcion, BigDecimal latitud, BigDecimal longitud, Short nivelGravedad) {
        if (pendiente.tipo() != NombreEstado.PENDIENTE) {
            throw new IllegalArgumentException("Un reporte nuevo debe iniciar en Pendiente");
        }
        Reporte r = new Reporte();
        r.tipo = tipo;
        r.autor = autor;
        r.categoria = categoria;
        r.zona = zona;
        r.estado = pendiente;
        r.descripcion = descripcion.trim();
        r.latitud = latitud;
        r.longitud = longitud;
        r.nivelGravedad = nivelGravedad;
        r.fechaReporte = LocalDate.now(ZoneId.of("America/Bogota"));
        return r;
    }

    /**
     * Registra los datos del daño. Solo un reporte de tipo INCIDENTE puede tenerlos
     * (la base también lo garantiza con una FK compuesta; aquí se avisa antes, con un mensaje claro).
     */
    public Incidente registrarIncidente(TipoDano tipoDano, String descripcionDano, LocalDate fechaHecho,
                                        boolean huboAtencionMedica, EntidadPublica entidad) {
        if (tipo != TipoReporte.INCIDENTE) {
            throw new ReglaNegocioException("NO_ES_INCIDENTE", "Solo un reporte de tipo INCIDENTE puede registrar daños");
        }
        if (incidente != null) {
            throw new ReglaNegocioException("INCIDENTE_DUPLICADO", "Este reporte ya tiene registrado su incidente");
        }
        this.incidente = Incidente.de(this, tipoDano, descripcionDano, fechaHecho, huboAtencionMedica, entidad);
        return incidente;
    }

    /**
     * Cambia el estado siguiendo el ciclo permitido y deja el registro en el historial.
     * Regla: rechazar exige explicar por qué (el ciudadano tiene derecho a saberlo).
     */
    public HistorialEstado cambiarEstado(Estado nuevo, Usuario responsable, String observacion) {
        if (!responsable.esPersonalDeRevision()) {
            throw new ReglaNegocioException("SIN_PERMISO_ESTADO", "Solo un operador o administrador puede cambiar el estado");
        }
        if (!estado.puedePasarA(nuevo)) {
            throw new ReglaNegocioException("TRANSICION_INVALIDA",
                    "No se puede pasar de '" + estado.getNombre() + "' a '" + nuevo.getNombre() + "'. Permitidos: "
                            + estado.tipo().siguientesPermitidos().stream().map(NombreEstado::etiqueta).toList());
        }
        if (nuevo.tipo() == NombreEstado.RECHAZADO && (observacion == null || observacion.isBlank())) {
            throw new ReglaNegocioException("OBSERVACION_REQUERIDA", "Para rechazar un reporte hay que indicar el motivo");
        }
        HistorialEstado cambio = HistorialEstado.de(this, estado, nuevo, responsable, observacion);
        historial.add(cambio);
        this.estado = nuevo;
        return cambio;
    }

    /**
     * Agrega una foto. Creador (GRASP): el reporte agrega sus evidencias, así que él las crea.
     * Solo el autor puede subir fotos a su propio reporte.
     */
    public Evidencia agregarEvidencia(Usuario quienSube, String rutaImagen, FormatoImagen formato, int tamanoBytes,
                                      String hashSha256) {
        verificarPuedeRecibirEvidencia(quienSube, tamanoBytes);
        Evidencia evidencia = Evidencia.de(this, rutaImagen, formato, tamanoBytes, hashSha256);
        evidencias.add(evidencia);
        return evidencia;
    }

    /** Se expone aparte para validar ANTES de escribir el archivo en disco (no dejar archivos huérfanos). */
    public void verificarPuedeRecibirEvidencia(Usuario quienSube, int tamanoBytes) {
        if (!esDe(quienSube)) {
            throw new ReglaNegocioException("NO_ES_AUTOR", "Solo el autor del reporte puede agregarle fotos");
        }
        if (tamanoBytes <= 0 || tamanoBytes > Evidencia.TAMANO_MAXIMO_BYTES) {
            throw new ReglaNegocioException("TAMANO_NO_PERMITIDO", "La foto debe pesar entre 1 byte y 10 MB");
        }
    }

    public boolean esDe(Usuario usuario) {
        return autor.getId().equals(usuario.getId());
    }

    /**
     * El detalle y las fotos son privados (datos de salud, evidencia legal): los ven su autor, el
     * personal de revisión y el abogado que el ciudadano ELIGIÓ para el caso (al elegirlo, lo autorizó).
     * Experto en información: el reporte conoce a su autor y a su incidente, así que él decide (antes de
     * la auditoría GRASP esta regla estaba en el servicio).
     */
    public boolean puedeSerVistoPor(Usuario usuario) {
        return esDe(usuario) || usuario.esPersonalDeRevision() || esAbogadoDelCaso(usuario);
    }

    private boolean esAbogadoDelCaso(Usuario usuario) {
        return incidente != null && incidente.getCaso() != null && incidente.getCaso().esAbogadoAsignado(usuario);
    }

    public Integer getId() { return id; }
    public Usuario getAutor() { return autor; }
    public Categoria getCategoria() { return categoria; }
    public Zona getZona() { return zona; }
    public Estado getEstado() { return estado; }
    public TipoReporte getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getLatitud() { return latitud; }
    public BigDecimal getLongitud() { return longitud; }
    public LocalDate getFechaReporte() { return fechaReporte; }
    public Short getNivelGravedad() { return nivelGravedad; }
    public List<HistorialEstado> getHistorial() { return Collections.unmodifiableList(historial); }
    public Incidente getIncidente() { return incidente; }
    public List<Evidencia> getEvidencias() { return Collections.unmodifiableList(evidencias); }
}
