package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Expediente COMPLETO: solo lo ve el abogado que el ciudadano eligió (al elegirlo lo autorizó). */
public record ExpedienteResponse(Integer id, String estado, LocalDate fechaCreacion, LocalDate fechaCierre,
                                 String observacion, String nombreCiudadano, String correoCiudadano,
                                 ReporteDetalleResponse reporte,
                                 List<AntecedenteResponse> antecedentes,
                                 List<TestigoResponse> testigos, BigDecimal valorCobrado) {
}
