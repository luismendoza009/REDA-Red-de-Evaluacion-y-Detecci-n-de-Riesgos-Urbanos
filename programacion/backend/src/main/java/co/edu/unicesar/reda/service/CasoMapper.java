package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.CasoDtos.*;
import co.edu.unicesar.reda.model.Abogado;
import co.edu.unicesar.reda.model.Caso;
import co.edu.unicesar.reda.model.Postulacion;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.repository.CasoRepository;
import org.springframework.stereotype.Component;

/**
 * Convierte casos a sus vistas. Hay tres niveles de detalle según quién mira (privacidad por diseño):
 *  - ciudadano: su caso y el perfil público de quienes se postularon;
 *  - abogado antes de ser elegido: resumen ANÓNIMO (CasoDisponibleResponse, en AbogadoCasosService);
 *  - abogado elegido: expediente completo (ExpedienteResponse, en AbogadoCasosService).
 */
@Component
class CasoMapper {

    private final CasoRepository casos;

    CasoMapper(CasoRepository casos) {
        this.casos = casos;
    }

    PerfilPublicoAbogado perfil(Abogado a) {
        return new PerfilPublicoAbogado(a.getId(), a.getUsuario().getNombre(), a.getEspecialidad(), a.getCiudad(),
                a.getAniosExperiencia(), a.getPresentacion(), casos.contarCasosTomados(a.getId()));
    }

    CasoCiudadanoResponse paraCiudadano(Caso c) {
        Reporte r = c.getIncidente().getReporte();
        return new CasoCiudadanoResponse(c.getId(), r.getId(), r.getCategoria().getNombre(), r.getZona().getNombre(),
                c.getIncidente().getTipoDano().name(), c.getEstado().getNombre(), c.getFechaCreacion(),
                c.abogadoAsignado().map(this::perfil).orElse(null),
                c.getPostulaciones().stream()
                        .filter(p -> p.getEstado() != Postulacion.Estado.RETIRADA)
                        .map(p -> new PostulacionResponse(p.getId(), perfil(p.getAbogado()), p.getMensaje(),
                                p.getEstado().name(), p.getFechaPostulacion()))
                        .toList());
    }
}
