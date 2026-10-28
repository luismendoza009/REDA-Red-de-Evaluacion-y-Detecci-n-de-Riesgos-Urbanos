package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.AccesoDenegadoException;
import co.edu.unicesar.reda.respuesta.CasoDisponibleResponse;
import co.edu.unicesar.reda.respuesta.ExpedienteResponse;
import co.edu.unicesar.reda.respuesta.TestigoResponse;
import co.edu.unicesar.reda.model.*;
import co.edu.unicesar.reda.repository.AbogadoRepository;
import co.edu.unicesar.reda.repository.CasoRepository;
import co.edu.unicesar.reda.repository.EstadoCasoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Casos de uso del abogado: ver casos disponibles (resumen ANÓNIMO), postularse, retirarse,
 * ver el expediente COMPLETO de los casos donde el ciudadano lo eligió y cerrarlos.
 */
@Service
public class AbogadoCasosService {

    private final AbogadoRepository abogados;
    private final CasoRepository casos;
    private final EstadoCasoRepository estadosCaso;
    private final BuscadorAntecedentes buscadorAntecedentes;

    public AbogadoCasosService(AbogadoRepository abogados, CasoRepository casos, EstadoCasoRepository estadosCaso,
                               BuscadorAntecedentes buscadorAntecedentes) {
        this.abogados = abogados;
        this.casos = casos;
        this.estadosCaso = estadosCaso;
        this.buscadorAntecedentes = buscadorAntecedentes;
    }

    @Transactional(readOnly = true)
    public List<CasoDisponibleResponse> disponibles(Integer idUsuario) {
        Abogado yo = abogadoVerificado(idUsuario);
        return casos.findByEstado(EstadoCaso.Nombre.REMITIDO.etiqueta()).stream().map(c -> resumenAnonimo(c, yo)).toList();
    }

    @Transactional
    public CasoDisponibleResponse postular(Integer idCaso, String mensaje, Integer idUsuario) {
        Abogado yo = abogadoVerificado(idUsuario);
        Caso caso = Busqueda.porId(casos, idCaso, "Caso");
        caso.postular(yo, mensaje);
        casos.flush();
        return resumenAnonimo(caso, yo);
    }

    @Transactional
    public CasoDisponibleResponse retirar(Integer idCaso, Integer idUsuario) {
        Abogado yo = abogadoVerificado(idUsuario);
        Caso caso = Busqueda.porId(casos, idCaso, "Caso");
        caso.retirarPostulacion(yo);
        return resumenAnonimo(caso, yo);
    }

    @Transactional(readOnly = true)
    public List<ExpedienteResponse> misCasos(Integer idUsuario) {
        Abogado yo = abogadoVerificado(idUsuario);
        return casos.asignadosA(yo.getId()).stream().map(this::expediente).toList();
    }

    @Transactional
    public ExpedienteResponse cerrar(Integer idCaso, String observacion, Integer idUsuario) {
        Abogado yo = abogadoVerificado(idUsuario);
        Caso caso = Busqueda.porId(casos, idCaso, "Caso");
        caso.cerrar(yo.getUsuario(), estadosCaso.obtener(EstadoCaso.Nombre.CERRADO), observacion);
        casos.flush();
        return expediente(caso);
    }

    private Abogado abogadoVerificado(Integer idUsuario) {
        Abogado a = abogados.findByUsuarioId(idUsuario)
                .orElseThrow(() -> new AccesoDenegadoException("Esta cuenta no está registrada como abogado"));
        if (!a.isActivo()) {
            throw new AccesoDenegadoException("Su cuenta de abogado está pendiente de verificación de la tarjeta profesional");
        }
        return a;
    }

    /** Lo que ve un abogado ANTES de ser elegido: nada que identifique al ciudadano ni datos de salud en texto. */
    private CasoDisponibleResponse resumenAnonimo(Caso c, Abogado yo) {
        Incidente i = c.getIncidente();
        Reporte r = i.getReporte();
        List<BuscadorAntecedentes.Antecedente> antecedentes = antecedentesDe(r, i);
        Long dias = antecedentes.isEmpty() ? null
                : ChronoUnit.DAYS.between(antecedentes.get(0).fechaReporte(), i.getFechaHoraHecho());
        long postulados = c.getPostulaciones().stream().filter(p -> p.getEstado() != Postulacion.Estado.RETIRADA).count();
        return new CasoDisponibleResponse(c.getId(), r.getCategoria().getNombre(), r.getZona().getNombre(),
                i.getEntidad() == null ? null : i.getEntidad().getNombre(), i.getFechaHoraHecho(), i.getTipoDano().name(),
                i.isHuboAtencionMedica(), antecedentes.size(), dias, i.getTestigos().size(), r.getEvidencias().size(),
                TarifaRemision.para(i.getTipoDano()), (int) postulados,
                c.postulacionDe(yo).map(p -> p.getEstado().name()).orElse(null), c.getFechaCreacion());
    }

    /** El expediente completo: el ciudadano autorizó al elegir a este abogado. */
    private ExpedienteResponse expediente(Caso c) {
        Incidente i = c.getIncidente();
        Reporte r = i.getReporte();
        BigDecimal valor = c.valorCobrado().orElse(null);
        return new ExpedienteResponse(c.getId(), c.getEstado().getNombre(), c.getFechaCreacion(), c.getFechaCierre(),
                c.getObservacion(), r.getAutor().getNombre(), r.getAutor().getCorreo(), ReporteMapper.detalle(r),
                antecedentesDe(r, i).stream().map(ReporteMapper::antecedente).toList(),
                i.getTestigos().stream().map(t -> new TestigoResponse(t.getNombre(), t.getContacto())).toList(), valor);
    }

    private List<BuscadorAntecedentes.Antecedente> antecedentesDe(Reporte r, Incidente i) {
        return buscadorAntecedentes.buscar(r.getCategoria().getId(), r.getLatitud(), r.getLongitud(), i.getFechaHoraHecho());
    }
}
