package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.model.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoCasoRepository extends JpaRepository<EstadoCaso, Integer> {

    Optional<EstadoCaso> findByNombre(String nombre);

    default EstadoCaso obtener(EstadoCaso.Nombre nombre) {
        return findByNombre(nombre.etiqueta())
                .orElseThrow(() -> new IllegalStateException("Falta el estado de caso '" + nombre.etiqueta() + "' en la base"));
    }
}
