package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Consultas de la bandeja del operador. */
@Repository
public interface RevisionRepository extends JpaRepository<Reporte, Integer> {

    /** Reportes que esperan revisión: primero los incidentes (hubo daño), luego por antigüedad. */
    @Query("""
            SELECT r FROM Reporte r
            WHERE r.estado.nombre IN ('Pendiente', 'En revisión')
            ORDER BY CASE WHEN r.tipo = co.edu.unicesar.reda.model.TipoReporte.INCIDENTE THEN 0 ELSE 1 END,
                     r.fechaReporte ASC
            """)
    List<Reporte> bandejaDeRevision();
}
