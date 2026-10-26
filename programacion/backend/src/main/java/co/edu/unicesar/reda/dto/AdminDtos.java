package co.edu.unicesar.reda.dto;

import java.time.OffsetDateTime;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record AbogadoAdminResponse(Integer id, String nombre, String correo, String tarjetaProfesional,
                                       String especialidad, String ciudad, short aniosExperiencia, boolean verificado,
                                       OffsetDateTime fechaRegistro) {
    }
}
