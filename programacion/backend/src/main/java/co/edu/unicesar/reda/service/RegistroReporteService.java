package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.peticion.CrearIncidenteRequest;
import co.edu.unicesar.reda.peticion.FallaRequest;
import co.edu.unicesar.reda.respuesta.IncidenteCreadoResponse;
import co.edu.unicesar.reda.respuesta.ReporteDetalleResponse;
import co.edu.unicesar.reda.model.*;
import co.edu.unicesar.reda.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Caso de uso: un ciudadano REGISTRA un reporte (condición o incidente).
 * GRASP Controlador: coordina; las reglas las aplica el dominio (Reporte, Incidente).
 * Responsabilidad única: crear reportes. Consultar y revisar están en otros servicios.
 */
@Service
public class RegistroReporteService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final ZonaRepository zonas;
    private final EstadoRepository estados;
    private final EntidadPublicaRepository entidades;
    private final ReporteRelacionadoRepository relaciones;
    private final BuscadorAntecedentes buscadorAntecedentes;

    public RegistroReporteService(ReporteRepository reportes, UsuarioRepository usuarios,
                                  CategoriaRepository categorias, ZonaRepository zonas, EstadoRepository estados,
                                  EntidadPublicaRepository entidades, ReporteRelacionadoRepository relaciones,
                                  BuscadorAntecedentes buscadorAntecedentes) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.zonas = zonas;
        this.estados = estados;
        this.entidades = entidades;
        this.relaciones = relaciones;
        this.buscadorAntecedentes = buscadorAntecedentes;
    }

    /** Falla sin daño: queda como antecedente probatorio. */
    @Transactional
    public ReporteDetalleResponse crearCondicion(FallaRequest falla, Integer idAutor) {
        return ReporteMapper.detalle(reportes.save(nuevoReporte(TipoReporte.CONDICION, falla, idAutor)));
    }

    /**
     * Incidente (hubo daño). En la MISMA transacción:
     *   1. guarda el reporte y los datos del daño,
     *   2. busca los reportes de condición previos en el mismo punto,
     *   3. los vincula como ANTECEDENTE.
     * Si algo falla, no queda un incidente sin sus vínculos ni vínculos sin incidente.
     */
    @Transactional
    public IncidenteCreadoResponse crearIncidente(CrearIncidenteRequest datos, Integer idAutor) {
        Reporte reporte = nuevoReporte(TipoReporte.INCIDENTE, datos.falla(), idAutor);
        EntidadPublica entidad = datos.idEntidad() == null ? null
                : Busqueda.porId(entidades, datos.idEntidad(), "Entidad");
        Incidente incidente = reporte.registrarIncidente(datos.tipoDano(), datos.descripcionDano(),
                datos.fechaHecho(), datos.huboAtencionMedica(), entidad);
        if (datos.testigos() != null) {
            datos.testigos().forEach(t -> incidente.agregarTestigo(t.nombre(), t.contacto()));
        }
        reportes.save(reporte);

        List<BuscadorAntecedentes.Antecedente> antecedentes = buscadorAntecedentes.buscar(
                reporte.getCategoria().getId(), reporte.getLatitud(), reporte.getLongitud(), incidente.getFechaHecho());
        relaciones.saveAll(antecedentes.stream()
                .map(a -> ReporteRelacionado.antecedenteAutomatico(reporte.getId(), a.idReporte()))
                .toList());

        Long dias = antecedentes.isEmpty() ? null
                : ChronoUnit.DAYS.between(antecedentes.get(0).fechaReporte(), incidente.getFechaHecho());
        return new IncidenteCreadoResponse(ReporteMapper.detalle(reporte),
                antecedentes.stream().map(ReporteMapper::antecedente).toList(), dias);
    }

    private Reporte nuevoReporte(TipoReporte tipo, FallaRequest falla, Integer idAutor) {
        Estado pendiente = estados.findByNombre(NombreEstado.PENDIENTE.etiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado Pendiente en la base"));
        return Reporte.nuevo(tipo, Busqueda.porId(usuarios, idAutor, "Usuario"),
                Busqueda.porId(categorias, falla.idCategoria(), "Categoría"),
                Busqueda.porId(zonas, falla.idZona(), "Zona"),
                pendiente, falla.descripcion(), falla.latitud(), falla.longitud(), falla.nivelGravedad());
    }
}
