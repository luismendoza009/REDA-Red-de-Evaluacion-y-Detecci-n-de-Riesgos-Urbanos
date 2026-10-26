package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Calificacion;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

    /** Promedio y número de calificaciones; se calculan al consultar (no se almacenan: 3FN). */
    interface Reputacion {
        Double getPromedio();
        long getTotal();
    }

    @Query("SELECT avg(c.puntaje) AS promedio, count(c) AS total FROM Calificacion c WHERE c.abogado.id = :idAbogado")
    Reputacion reputacionDe(Integer idAbogado);

    /** Últimos comentarios, del más reciente al más antiguo. */
    @Query("SELECT c FROM Calificacion c WHERE c.abogado.id = :idAbogado ORDER BY c.fecha DESC")
    List<Calificacion> recientesDe(Integer idAbogado, Limit limite);
}
