package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Expediente COMPLETO: solo lo ve el abogado que el ciudadano eligió (al elegirlo lo autorizó). */
public record ExpedienteResponse(Integer id, String estado, OffsetDateTime fechaCreacion, OffsetDateTime fechaCierre,
                                 String observacion, String nombreCiudadano, String correoCiudadano,
                                 ReporteDetalleResponse reporte,
                                 List<AntecedenteResponse> antecedentes,
                                 List<TestigoResponse> testigos, BigDecimal valorCobrado) {
}
