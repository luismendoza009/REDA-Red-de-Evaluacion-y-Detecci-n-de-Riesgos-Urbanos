package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import org.springframework.data.repository.CrudRepository;

/** "Busca por id o lanza 404": se usa en todos los servicios, así que vive en un solo lugar (DRY). */
final class Busqueda {

    private Busqueda() {
    }

    static <T> T porId(CrudRepository<T, Integer> repositorio, Integer id, String recurso) {
        return repositorio.findById(id).orElseThrow(() -> new RecursoNoEncontradoException(recurso, id));
    }
}
