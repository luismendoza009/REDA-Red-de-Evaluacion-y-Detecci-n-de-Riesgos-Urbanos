package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.EntidadPublica;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntidadPublicaRepository extends JpaRepository<EntidadPublica, Integer> {

    List<EntidadPublica> findAllByOrderByNombreAsc();
}
