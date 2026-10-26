package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Caso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CasoRepository extends JpaRepository<Caso, Integer> {

    /** Casos publicados a los abogados (reciben postulaciones), los más recientes primero. */
    @Query("SELECT c FROM Caso c WHERE c.estado.nombre = :estado ORDER BY c.fechaCreacion DESC")
    List<Caso> findByEstado(String estado);

    /** Casos de los incidentes que reportó un ciudadano. */
    @Query("SELECT c FROM Caso c WHERE c.incidente.reporte.autor.id = :idCiudadano ORDER BY c.fechaCreacion DESC")
    List<Caso> deCiudadano(Integer idCiudadano);

    /** Casos que un abogado tiene asignados (el ciudadano lo eligió). */
    @Query("""
            SELECT DISTINCT c FROM Caso c JOIN c.remisiones r
            WHERE r.abogado.id = :idAbogado AND r.estadoRemision = 'ACEPTADA'
            ORDER BY c.fechaCreacion DESC
            """)
    List<Caso> asignadosA(Integer idAbogado);

    /** Cuántos casos ha tomado un abogado en REDA (se muestra al ciudadano al comparar). */
    @Query("SELECT count(r) FROM Remision r WHERE r.abogado.id = :idAbogado AND r.estadoRemision = 'ACEPTADA'")
    long contarCasosTomados(Integer idAbogado);
}
