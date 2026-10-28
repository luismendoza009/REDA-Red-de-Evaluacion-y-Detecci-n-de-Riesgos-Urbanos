package co.edu.unicesar.reda.respuesta;

public record PerfilAbogado(String tarjetaProfesional, String especialidad, String ciudad, short aniosExperiencia,
                            String presentacion, boolean verificado, Double calificacionPromedio, long numCalificaciones) {
}
