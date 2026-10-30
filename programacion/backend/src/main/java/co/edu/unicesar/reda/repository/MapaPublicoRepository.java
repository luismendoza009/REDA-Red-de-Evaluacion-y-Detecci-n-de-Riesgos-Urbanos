package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.SituacionCaso;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Lectura de la vista v_mapa_publico (migraciones 008 y 009), que ya excluye autor, datos del daño y
 * reportes rechazados, redondea las coordenadas y calcula la situación pública de cada caso.
 * Separado de ReporteRepository por segregación de interfaces (ISP): quien solo pinta el mapa
 * público no tiene por qué depender del CRUD de reportes, y viceversa.
 */
@Repository
public class MapaPublicoRepository {

    private static final String COLUMNAS = """
            id_reporte, tipo_reporte, categoria, zona, estado, latitud, longitud, fecha,
            nivel_gravedad, num_evidencias, descripcion, situacion
            """;

    private static final RowMapper<PuntoMapa> FILA = (rs, i) -> new PuntoMapa(
            rs.getInt("id_reporte"), rs.getString("tipo_reporte"), rs.getString("categoria"),
            rs.getString("zona"), rs.getString("estado"), rs.getBigDecimal("latitud"),
            rs.getBigDecimal("longitud"), rs.getObject("fecha", LocalDate.class),
            rs.getObject("nivel_gravedad") == null ? null : rs.getShort("nivel_gravedad"),
            rs.getLong("num_evidencias"), rs.getString("descripcion"),
            SituacionCaso.valueOf(rs.getString("situacion")));

    private final JdbcTemplate jdbc;

    public MapaPublicoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record PuntoMapa(Integer idReporte, String tipoReporte, String categoria, String zona, String estado,
                            BigDecimal latitud, BigDecimal longitud, LocalDate fecha, Short nivelGravedad,
                            long numEvidencias, String descripcion, SituacionCaso situacion) {
    }

    /** Un cambio de estado tal como se muestra al público: sin nombres ni observaciones internas. */
    public record HitoPublico(String estado, LocalDate fecha) {
    }

    public List<PuntoMapa> puntos() {
        return jdbc.query("SELECT " + COLUMNAS + " FROM v_mapa_publico ORDER BY fecha DESC", FILA);
    }

    public Optional<PuntoMapa> porId(Integer idReporte) {
        return jdbc.query("SELECT " + COLUMNAS + " FROM v_mapa_publico WHERE id_reporte = ?", FILA, idReporte)
                .stream().findFirst();
    }

    public List<HitoPublico> lineaDeTiempo(Integer idReporte) {
        return jdbc.query("""
                SELECT e.nombre AS estado, h.fecha_cambio
                FROM historial_estados h
                JOIN estados e ON e.id_estado = h.id_estado_nuevo
                WHERE h.id_reporte = ?
                ORDER BY h.id_historial
                """, (rs, i) -> new HitoPublico(rs.getString("estado"), rs.getObject("fecha_cambio", LocalDate.class)),
                idReporte);
    }
}
