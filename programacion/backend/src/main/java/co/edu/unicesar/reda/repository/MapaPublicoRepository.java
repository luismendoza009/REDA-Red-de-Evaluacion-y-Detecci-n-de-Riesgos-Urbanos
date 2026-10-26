package co.edu.unicesar.reda.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Lectura de la vista v_mapa_publico (migración 008), que ya excluye autor, datos del daño y
 * reportes rechazados, y redondea las coordenadas.
 * Separado de ReporteRepository por segregación de interfaces (ISP): quien solo pinta el mapa
 * público no tiene por qué depender del CRUD de reportes, y viceversa.
 */
@Repository
public class MapaPublicoRepository {

    private static final String SQL = """
            SELECT id_reporte, tipo_reporte, categoria, zona, estado, latitud, longitud, fecha,
                   nivel_gravedad, num_evidencias
            FROM v_mapa_publico
            ORDER BY fecha DESC
            """;

    private final JdbcTemplate jdbc;

    public MapaPublicoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record PuntoMapa(Integer idReporte, String tipoReporte, String categoria, String zona, String estado,
                            BigDecimal latitud, BigDecimal longitud, LocalDate fecha, Short nivelGravedad,
                            long numEvidencias) {
    }

    public List<PuntoMapa> puntos() {
        return jdbc.query(SQL, (rs, i) -> new PuntoMapa(
                rs.getInt("id_reporte"), rs.getString("tipo_reporte"), rs.getString("categoria"),
                rs.getString("zona"), rs.getString("estado"), rs.getBigDecimal("latitud"),
                rs.getBigDecimal("longitud"), rs.getObject("fecha", LocalDate.class),
                rs.getObject("nivel_gravedad") == null ? null : rs.getShort("nivel_gravedad"),
                rs.getLong("num_evidencias")));
    }
}
