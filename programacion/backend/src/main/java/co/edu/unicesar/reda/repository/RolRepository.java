package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.Rol;
import co.edu.unicesar.reda.model.NombreRol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(NombreRol nombre);
}
