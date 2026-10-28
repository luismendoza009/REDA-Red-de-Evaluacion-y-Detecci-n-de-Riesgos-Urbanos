package co.edu.unicesar.reda.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/** Contratos públicos del mapa. Ninguno lleva autor, datos del daño ni fotos (las fotos son privadas). */
public final class MapaDtos {

    private MapaDtos() {
    }

    /** Un reporte dentro de un punto del mapa. */
    public record ReporteEnPunto(Integer id, String tipo, String categoria, String estado, String situacion,
                                 LocalDate fecha, String descripcion, Short nivelGravedad, long numFotos) {
    }

    /** Conteo de reportes por situación pública dentro de un punto. */
    public record ConteoSituaciones(long conAbogado, long abiertos, long cerrados) {
    }

    /** Un punto del mapa: uno o varios reportes a menos del radio de agrupación. */
    public record PuntoResponse(Integer id, double latitud, double longitud, String zona, String frecuencia,
                                int totalReportes, int incidentes, List<String> categorias,
                                LocalDate primerReporte, LocalDate ultimoReporte,
                                ConteoSituaciones situaciones, List<ReporteEnPunto> reportes) {
    }

    /** Detalle público de un reporte: su línea de tiempo y cuántas fotos privadas tiene. */
    public record DetallePublicoResponse(Integer id, String tipo, String categoria, String zona, String estado,
                                         String situacion, LocalDate fecha, String descripcion, Short nivelGravedad,
                                         long numFotos, List<Hito> lineaDeTiempo) {
    }

    public record Hito(String estado, OffsetDateTime fecha) {
    }
}
