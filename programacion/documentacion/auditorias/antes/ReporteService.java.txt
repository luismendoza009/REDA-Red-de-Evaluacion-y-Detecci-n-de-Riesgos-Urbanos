package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.config.PropiedadesReda;
import co.edu.unicesar.reda.dto.ReporteDtos.*;
import co.edu.unicesar.reda.exception.AccesoDenegadoException;
import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.*;
import co.edu.unicesar.reda.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Casos de uso de los reportes.
 * GRASP Controlador: recibe cada operación del sistema (crear, consultar, cambiar estado) y la
 * coordina: busca las entidades, DELEGA las reglas en el dominio (Reporte, Estado, Incidente) y
 * guarda. Por eso aquí casi no hay "if" de negocio: las decisiones viven en el modelo.
 * Cada método público es una transacción: o se hace todo, o nada.
 */
@Service
public class ReporteService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ZonaRepository zonas;
    private final EstadoRepository estados;
    private final EntidadPublicaRepository entidades;
    private final ReporteRelacionadoRepository relaciones;
    private final BuscadorAntecedentes buscadorAntecedentes;
    private final int radioAntecedentesMetros;

    public ReporteService(ReporteRepository reportes, UsuarioRepository usuarios, CategoriaRepository categorias,
                          ZonaRepository zonas, EstadoRepository estados, EntidadPublicaRepository entidades,
                          ReporteRelacionadoRepository relaciones, BuscadorAntecedentes buscadorAntecedentes,
                          PropiedadesReda propiedades) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.zonas = zonas;
        this.estados = estados;
        this.entidades = entidades;
        this.relaciones = relaciones;
        this.buscadorAntecedentes = buscadorAntecedentes;
        this.radioAntecedentesMetros = propiedades.antecedentes().radioMetros();
    }

    /** Un ciudadano reporta una falla sin daño (antecedente). */
    @Transactional
    public ReporteDetalleResponse crearCondicion(FallaRequest falla, Integer idAutor) {
        Reporte reporte = reportes.save(nuevoReporte(TipoReporte.CONDICION, falla, idAutor));
        return ReporteMapper.detalle(reporte);
    }

    /**
     * Un ciudadano reporta un incidente (hubo daño). En la MISMA transacción:
     *   1. guarda el reporte y los datos del daño,
     *   2. busca los reportes de condición previos de la misma categoría en el radio configurado,
     *   3. los vincula como ANTECEDENTE.
     * Si algo falla, no queda un incidente sin sus vínculos ni vínculos sin incidente.
     */
    @Transactional
    public IncidenteCreadoResponse crearIncidente(CrearIncidenteRequest datos, Integer idAutor) {
        Reporte reporte = nuevoReporte(TipoReporte.INCIDENTE, datos.falla(), idAutor);
        EntidadPublica entidad = datos.idEntidad() == null ? null : buscar(entidades, datos.idEntidad(), "Entidad");
        Incidente incidente = reporte.registrarIncidente(datos.tipoDano(), datos.descripcionDano(),
                datos.fechaHoraHecho(), datos.huboAtencionMedica(), entidad);
        reportes.save(reporte);

        List<BuscadorAntecedentes.Antecedente> antecedentes = buscadorAntecedentes.buscar(
                reporte.getCategoria().getId(), reporte.getLatitud(), reporte.getLongitud(),
                incidente.getFechaHoraHecho(), radioAntecedentesMetros);
        relaciones.saveAll(antecedentes.stream()
                .map(a -> ReporteRelacionado.antecedenteAutomatico(reporte.getId(), a.idReporte()))
                .toList());

        Long dias = antecedentes.isEmpty() ? null
                : ChronoUnit.DAYS.between(antecedentes.get(0).fechaReporte(), incidente.getFechaHoraHecho());
        return new IncidenteCreadoResponse(ReporteMapper.detalle(reporte),
                antecedentes.stream().map(ReporteMapper::antecedente).toList(), dias);
    }

    @Transactional(readOnly = true)
    public List<ReporteResumenResponse> misReportes(Integer idAutor) {
        return reportes.findByAutorIdOrderByFechaReporteDesc(idAutor).stream().map(ReporteMapper::resumen).toList();
    }

    /** El detalle (con datos del daño) solo lo ve su autor o el personal de revisión. */
    @Transactional(readOnly = true)
    public ReporteDetalleResponse detalle(Integer idReporte, Integer idSolicitante) {
        Reporte reporte = reportes.findConHistorialById(idReporte)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reporte", idReporte));
        verificarAcceso(reporte, buscar(usuarios, idSolicitante, "Usuario"));
        return ReporteMapper.detalle(reporte);
    }

    /** Antecedentes de un incidente, calculados en el momento (no solo los guardados). */
    @Transactional(readOnly = true)
    public List<AntecedenteResponse> antecedentes(Integer idReporte, Integer idSolicitante) {
        Reporte reporte = buscar(reportes, idReporte, "Reporte");
        verificarAcceso(reporte, buscar(usuarios, idSolicitante, "Usuario"));
        if (reporte.getIncidente() == null) {
            throw new ReglaNegocioException("NO_ES_INCIDENTE", "Solo los incidentes tienen antecedentes");
        }
        return buscadorAntecedentes.buscar(reporte.getCategoria().getId(), reporte.getLatitud(), reporte.getLongitud(),
                        reporte.getIncidente().getFechaHoraHecho(), radioAntecedentesMetros)
                .stream().map(ReporteMapper::antecedente).toList();
    }

    @Transactional
    public CambioEstadoResponse cambiarEstado(Integer idReporte, CambioEstadoRequest datos, Integer idResponsable) {
        Reporte reporte = buscar(reportes, idReporte, "Reporte");
        Estado nuevo = estados.findByNombre(datos.nuevoEstado().etiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + datos.nuevoEstado() + " en la base"));
        HistorialEstado cambio = reporte.cambiarEstado(nuevo, buscar(usuarios, idResponsable, "Usuario"),
                datos.observacion());
        // El reporte ya está gestionado por JPA: no se llama save() (eso haría un merge, que no
        // propaga la inserción del historial). flush() escribe el cambio y el historial ahora mismo,
        // para que un error de la base salga dentro de este método y no al confirmar.
        reportes.flush();
        return ReporteMapper.cambio(cambio);
    }

    private Reporte nuevoReporte(TipoReporte tipo, FallaRequest falla, Integer idAutor) {
        Estado pendiente = estados.findByNombre(NombreEstado.PENDIENTE.etiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado Pendiente en la base"));
        return Reporte.nuevo(tipo, buscar(usuarios, idAutor, "Usuario"),
                buscar(categorias, falla.idCategoria(), "Categoría"), buscar(zonas, falla.idZona(), "Zona"),
                pendiente, falla.descripcion(), falla.latitud(), falla.longitud(), falla.nivelGravedad());
    }

    private static void verificarAcceso(Reporte reporte, Usuario solicitante) {
        if (!reporte.esDe(solicitante) && !solicitante.esPersonalDeRevision()) {
            throw new AccesoDenegadoException("Solo el autor o el personal de revisión pueden ver este reporte");
        }
    }

    /** Busca por id o lanza 404 con el nombre del recurso (evita repetir el mismo orElseThrow en cada método). */
    private static <T> T buscar(org.springframework.data.repository.CrudRepository<T, Integer> repo, Integer id,
                                String recurso) {
        return repo.findById(id).orElseThrow(() -> new RecursoNoEncontradoException(recurso, id));
    }
}
