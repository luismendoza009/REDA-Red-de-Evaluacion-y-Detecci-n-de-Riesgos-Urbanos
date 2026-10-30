package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.model.HistorialEstado;
import co.edu.unicesar.reda.model.Incidente;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.repository.MapaPublicoRepository.PuntoMapa;
import co.edu.unicesar.reda.respuesta.AntecedenteResponse;
import co.edu.unicesar.reda.respuesta.CambioEstadoResponse;
import co.edu.unicesar.reda.respuesta.IncidenteResponse;
import co.edu.unicesar.reda.respuesta.PuntoMapaResponse;
import co.edu.unicesar.reda.respuesta.ReporteDetalleResponse;
import co.edu.unicesar.reda.respuesta.ReporteResumenResponse;

/**
 * Convierte entidades en respuestas de la API (paquete respuesta).
 * GRASP Indirección: separa el modelo de dominio del contrato de la API. Si cambia una tabla,
 * no tiene por qué cambiar el JSON que usa el frontend (y viceversa). Además evita exponer
 * datos que no deben salir (correo del autor, hash de la contraseña...).
 */
final class ReporteMapper {

    private ReporteMapper() {
    }

    static ReporteResumenResponse resumen(Reporte r) {
        return new ReporteResumenResponse(r.getId(), r.getTipo().name(), r.getCategoria().getNombre(),
                r.getZona().getNombre(), r.getEstado().getNombre(), r.getFechaReporte(), r.getNivelGravedad());
    }

    static ReporteDetalleResponse detalle(Reporte r) {
        return new ReporteDetalleResponse(r.getId(), r.getTipo().name(), r.getCategoria().getNombre(),
                r.getZona().getNombre(), r.getEstado().getNombre(), r.getDescripcion(), r.getLatitud(),
                r.getLongitud(), r.getFechaReporte(), r.getNivelGravedad(),
                r.getIncidente() == null ? null : incidente(r.getIncidente()),
                r.getHistorial().stream().map(ReporteMapper::cambio).toList());
    }

    static CambioEstadoResponse cambio(HistorialEstado h) {
        return new CambioEstadoResponse(h.getEstadoAnterior().getNombre(), h.getEstadoNuevo().getNombre(),
                h.getFechaCambio(), h.getResponsable().getNombre(), h.getObservacion());
    }

    static IncidenteResponse incidente(Incidente i) {
        return new IncidenteResponse(i.getId(), i.getTipoDano().name(), i.getDescripcionDano(), i.getFechaHecho(),
                i.isHuboAtencionMedica(), i.getEntidad() == null ? null : i.getEntidad().getNombre());
    }

    static AntecedenteResponse antecedente(BuscadorAntecedentes.Antecedente a) {
        return new AntecedenteResponse(a.idReporte(), a.fechaReporte(), a.estado(), a.descripcion(),
                Math.round(a.distanciaMetros() * 10) / 10.0);
    }

    static PuntoMapaResponse punto(PuntoMapa p) {
        return new PuntoMapaResponse(p.idReporte(), p.tipoReporte(), p.categoria(), p.zona(), p.estado(),
                p.latitud(), p.longitud(), p.fecha(), p.nivelGravedad(), p.numEvidencias());
    }
}
