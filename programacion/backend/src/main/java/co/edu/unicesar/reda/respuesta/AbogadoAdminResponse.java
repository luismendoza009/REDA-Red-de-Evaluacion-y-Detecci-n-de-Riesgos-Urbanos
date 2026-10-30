package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record AbogadoAdminResponse(Integer id, String nombre, String correo, String tarjetaProfesional,
                                   String especialidad, String ciudad, short aniosExperiencia, boolean verificado,
                                   OffsetDateTime fechaRegistro) {
}
