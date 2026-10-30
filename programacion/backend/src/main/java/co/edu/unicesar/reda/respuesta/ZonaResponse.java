package co.edu.unicesar.reda.respuesta;

import java.math.BigDecimal;

public record ZonaResponse(Integer id, String nombre, String tipo, BigDecimal latitud, BigDecimal longitud) {
}
