package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Reporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Integer> {

    List<Reporte> findByAutorIdOrderByFechaReporteDesc(Integer idAutor);

    /** Carga el historial en la misma consulta para mostrar la línea de tiempo sin consultas N+1. */
    @EntityGraph(attributePaths = {"historial", "autor"})
    Optional<Reporte> findConHistorialById(Integer id);
}
