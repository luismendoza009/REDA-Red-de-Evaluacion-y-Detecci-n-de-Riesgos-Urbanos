package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Zona;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZonaRepository extends JpaRepository<Zona, Integer> {

    List<Zona> findAllByOrderByNombreAsc();
}
