package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Notificacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    @EntityGraph(attributePaths = {"historial", "historial.reporte"})
    List<Notificacion> findByDestinatario_IdOrderByFechaCreacionDesc(Integer idUsuario, Pageable pageable);

    long countByDestinatario_IdAndFechaLecturaIsNull(Integer idUsuario);

    Optional<Notificacion> findByIdAndDestinatario_Id(Integer id, Integer idUsuario);
}
