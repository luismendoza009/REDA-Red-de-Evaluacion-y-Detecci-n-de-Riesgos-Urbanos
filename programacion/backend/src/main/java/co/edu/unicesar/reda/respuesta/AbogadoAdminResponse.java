package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record AbogadoAdminResponse(Integer id, String nombre, String correo, String tarjetaProfesional,
                                   String especialidad, String ciudad, short aniosExperiencia, boolean verificado,
                                   LocalDate fechaRegistro) {
}
