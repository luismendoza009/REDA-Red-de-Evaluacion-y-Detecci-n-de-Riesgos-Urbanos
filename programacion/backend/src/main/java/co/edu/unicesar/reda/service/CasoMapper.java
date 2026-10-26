package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.CasoDtos.*;
import co.edu.unicesar.reda.model.Abogado;
import co.edu.unicesar.reda.model.Caso;
import co.edu.unicesar.reda.model.Postulacion;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.model.EstadoCaso;
import co.edu.unicesar.reda.repository.CalificacionRepository;
import co.edu.unicesar.reda.repository.CasoRepository;
import org.springframework.data.domain.Limit;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Convierte casos a sus vistas. Hay tres niveles de detalle según quién mira (privacidad por diseño):
 *  - ciudadano: su caso y el perfil público de quienes se postularon;
 *  - abogado antes de ser elegido: resumen ANÓNIMO (CasoDisponibleResponse, en AbogadoCasosService);
 *  - abogado elegido: expediente completo (ExpedienteResponse, en AbogadoCasosService).
 */
@Component
class CasoMapper {

    /** Cuántos comentarios recientes se muestran al comparar abogados. */
    static final int COMENTARIOS_VISIBLES = 3;
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"));

    private final CasoRepository casos;
    private final CalificacionRepository calificaciones;

    CasoMapper(CasoRepository casos, CalificacionRepository calificaciones) {
        this.casos = casos;
        this.calificaciones = calificaciones;
    }

    /** Perfil para comparar: experiencia, casos en REDA y reputación (promedio calculado al consultar). */
    PerfilPublicoAbogado perfil(Abogado a, boolean conContacto) {
        CalificacionRepository.Reputacion rep = calificaciones.reputacionDe(a.getId());
        Double promedio = rep.getPromedio() == null ? null : Math.round(rep.getPromedio() * 10) / 10.0;
        List<ComentarioPublico> comentarios = calificaciones.recientesDe(a.getId(), Limit.of(COMENTARIOS_VISIBLES)).stream()
                .map(c -> new ComentarioPublico(c.getPuntaje(), c.getComentario(),
                        "Cliente en " + c.getCaso().getIncidente().getReporte().getZona().getNombre() + " · "
                                + c.getCaso().getFechaCreacion().format(MES),
                        c.getFecha()))
                .toList();
        return new PerfilPublicoAbogado(a.getId(), a.getUsuario().getNombre(), FotoPerfilService.ruta(a.getUsuario()),
                a.isActivo(), a.getEspecialidad(), a.getCiudad(),
                a.getAniosExperiencia(), a.getPresentacion(), casos.contarCasosTomados(a.getId()),
                conContacto ? a.getUsuario().getCorreo() : null, promedio, rep.getTotal(), comentarios);
    }

    CasoCiudadanoResponse paraCiudadano(Caso c) {
        Reporte r = c.getIncidente().getReporte();
        return new CasoCiudadanoResponse(c.getId(), r.getId(), r.getCategoria().getNombre(), r.getZona().getNombre(),
                c.getIncidente().getTipoDano().name(), c.getEstado().getNombre(), c.getFechaCreacion(),
                c.abogadoAsignado().map(a -> perfil(a, true)).orElse(null),
                c.getPostulaciones().stream()
                        .filter(p -> p.getEstado() != Postulacion.Estado.RETIRADA)
                        .map(p -> new PostulacionResponse(p.getId(), perfil(p.getAbogado(), false), p.getMensaje(),
                                p.getEstado().name(), p.getFechaPostulacion()))
                        .toList(),
                c.getEstado().tipo() == EstadoCaso.Nombre.CERRADO && c.getCalificacion() == null && c.abogadoAsignado().isPresent(),
                c.getCalificacion() == null ? null : c.getCalificacion().getPuntaje(),
                c.getCalificacion() == null ? null : c.getCalificacion().getComentario());
    }
}
