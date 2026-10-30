package co.edu.unicesar.reda.respuesta;

import java.time.LocalDate;

public record IncidenteResponse(Integer id, String tipoDano, String descripcionDano, LocalDate fechaHecho,
                                boolean huboAtencionMedica, String entidadResponsable) {
}
