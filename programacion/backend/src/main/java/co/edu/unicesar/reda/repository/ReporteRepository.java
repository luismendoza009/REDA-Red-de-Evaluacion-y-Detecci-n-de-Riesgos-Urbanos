package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Reporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByAutorIdOrderByFechaReporteDesc(Integer idAutor);

    /** Carga el historial en la misma consulta para mostrar la línea de tiempo sin consultas N+1. */
    @EntityGraph(attributePaths = {"historial", "autor"})
    Optional<Reporte> findConHistorialById(Integer id);

    /** Proyección de la vista v_mapa_publico (migración 008): no contiene datos personales. */
    interface PuntoMapa {
        Integer getIdReporte();
        String getTipoReporte();
        String getCategoria();
        String getZona();
        String getEstado();
        BigDecimal getLatitud();
        BigDecimal getLongitud();
        LocalDate getFecha();
        Short getNivelGravedad();
        Long getNumEvidencias();
    }

    @Query(value = """
            SELECT id_reporte AS idReporte, tipo_reporte AS tipoReporte, categoria, zona, estado,
                   latitud, longitud, fecha, nivel_gravedad AS nivelGravedad, num_evidencias AS numEvidencias
            FROM v_mapa_publico
            ORDER BY fecha DESC
            """, nativeQuery = true)
    List<PuntoMapa> mapaPublico();
}
