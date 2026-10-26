package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.service.BuscadorAntecedentes;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementación con SQL explícito (JdbcTemplate) y fórmula de Haversine, sin PostGIS.
 *
 * Dos pasos, igual que en la evidencia de índices de la Entrega 2:
 *  1. Filtro grueso con una "caja" de ±radio alrededor del punto: usa el índice
 *     ix_reportes_antecedentes (id_categoria, tipo_reporte, latitud).
 *  2. Distancia exacta con Haversine solo sobre los candidatos de la caja.
 * Todo va con parámetros con nombre: ningún dato del usuario se concatena al SQL.
 */
@Repository
public class BuscadorAntecedentesHaversine implements BuscadorAntecedentes {

    /** Metros que mide un grado de latitud (aproximación suficiente para radios de cientos de metros). */
    private static final double METROS_POR_GRADO = 111_320.0;

    private static final String SQL = """
            SELECT c.id_reporte, c.fecha_reporte, e.nombre AS estado, c.descripcion, c.distancia
            FROM (
                SELECT r.id_reporte, r.fecha_reporte, r.id_estado, r.descripcion,
                       2 * 6371000 * asin(sqrt(
                           power(sin(radians(r.latitud - :lat) / 2), 2) +
                           cos(radians(:lat)) * cos(radians(r.latitud)) *
                           power(sin(radians(r.longitud - :lon) / 2), 2))) AS distancia
                FROM reportes r
                WHERE r.id_categoria = :categoria
                  AND r.tipo_reporte = 'CONDICION'
                  AND r.latitud  BETWEEN :latMin AND :latMax
                  AND r.longitud BETWEEN :lonMin AND :lonMax
                  AND r.fecha_reporte < :antesDe
            ) c
            JOIN estados e ON e.id_estado = c.id_estado
            WHERE c.distancia <= :radio
              -- un reporte rechazado (duplicado, foto falsa...) no sirve como prueba de que la entidad sabía
              AND e.nombre <> 'Rechazado'
            ORDER BY c.fecha_reporte
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public BuscadorAntecedentesHaversine(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Antecedente> buscar(Integer idCategoria, BigDecimal latitud, BigDecimal longitud,
                                    OffsetDateTime anterioresA, int radioMetros) {
        double lat = latitud.doubleValue();
        double lon = longitud.doubleValue();
        double deltaLat = radioMetros / METROS_POR_GRADO;
        double deltaLon = radioMetros / (METROS_POR_GRADO * Math.cos(Math.toRadians(lat)));

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("lat", lat)
                .addValue("lon", lon)
                .addValue("categoria", idCategoria)
                .addValue("latMin", lat - deltaLat).addValue("latMax", lat + deltaLat)
                .addValue("lonMin", lon - deltaLon).addValue("lonMax", lon + deltaLon)
                .addValue("antesDe", anterioresA)
                .addValue("radio", radioMetros);

        return jdbc.query(SQL, p, (rs, i) -> new Antecedente(
                rs.getInt("id_reporte"),
                rs.getObject("fecha_reporte", OffsetDateTime.class),
                rs.getString("estado"),
                rs.getString("descripcion"),
                rs.getDouble("distancia")));
    }
}
