package co.edu.unicesar.reda.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Busca los reportes de CONDICIÓN que son antecedentes de un punto: misma categoría, dentro del
 * radio configurado, anteriores a una fecha y NO rechazados. Es la función estrella de REDA
 * ("la entidad ya sabía").
 *
 * Por qué es una interfaz (SOLID):
 *  - DIP: los servicios dependen de esta abstracción, no de SQL ni de JDBC.
 *  - OCP / GRASP Variaciones protegidas: hoy la implementación usa Haversine en SQL; en la
 *    Entrega 3 de BD habrá una función PL/pgSQL (y en el futuro, quizá PostGIS). Cambiar la forma
 *    de buscar no obliga a modificar los servicios: basta otra implementación.
 * El radio es parte de la definición de "cerca", por eso lo decide la implementación y no quien llama.
 */
public interface BuscadorAntecedentes {

    List<Antecedente> buscar(Integer idCategoria, BigDecimal latitud, BigDecimal longitud, OffsetDateTime anterioresA);

    /** Un reporte de condición encontrado como antecedente. */
    record Antecedente(Integer idReporte, OffsetDateTime fechaReporte, String estado,
                       String descripcion, double distanciaMetros) {
    }
}
