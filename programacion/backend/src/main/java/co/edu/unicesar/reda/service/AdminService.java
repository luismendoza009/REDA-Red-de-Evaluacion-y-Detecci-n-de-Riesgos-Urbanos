package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.model.Abogado;
import co.edu.unicesar.reda.repository.AbogadoRepository;
import co.edu.unicesar.reda.respuesta.AbogadoAdminResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso del administrador: verificar abogados (activar su cuenta tras revisar la tarjeta). */
@Service
public class AdminService {

    private final AbogadoRepository abogados;

    public AdminService(AbogadoRepository abogados) {
        this.abogados = abogados;
    }

    /** Primero los pendientes de verificación. */
    @Transactional(readOnly = true)
    public List<AbogadoAdminResponse> abogados() {
        return abogados.findAllByOrderByActivoAscIdAsc().stream().map(AdminService::respuesta).toList();
    }

    @Transactional
    public AbogadoAdminResponse activar(Integer idAbogado) {
        Abogado a = Busqueda.porId(abogados, idAbogado, "Abogado");
        a.activar();
        return respuesta(a);
    }

    private static AbogadoAdminResponse respuesta(Abogado a) {
        return new AbogadoAdminResponse(a.getId(), a.getUsuario().getNombre(), a.getUsuario().getCorreo(),
                a.getTarjetaProfesional(), a.getEspecialidad(), a.getCiudad(), a.getAniosExperiencia(), a.isActivo(),
                a.getUsuario().getFechaRegistro());
    }
}
