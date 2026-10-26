package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Abogado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbogadoRepository extends JpaRepository<Abogado, Integer> {

    Optional<Abogado> findByUsuarioId(Integer idUsuario);

    boolean existsByTarjetaProfesional(String tarjetaProfesional);

    List<Abogado> findAllByOrderByActivoAscIdAsc();
}
