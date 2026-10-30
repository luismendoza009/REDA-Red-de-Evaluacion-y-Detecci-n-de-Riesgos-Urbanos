package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Punto del mapa público: sin autor ni datos del daño. */
public record PuntoMapaResponse(Integer idReporte, String tipo, String categoria, String zona, String estado,
                                BigDecimal latitud, BigDecimal longitud, LocalDate fecha, Short nivelGravedad,
                                long numEvidencias) {
}
