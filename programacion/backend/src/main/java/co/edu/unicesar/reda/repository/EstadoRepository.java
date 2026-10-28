package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Estado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    Optional<Estado> findByNombre(String nombre);
}
