package co.edu.unicesar.reda.respuesta;

import java.time.OffsetDateTime;

public record IncidenteResponse(Integer id, String tipoDano, String descripcionDano, OffsetDateTime fechaHoraHecho,
                                boolean huboAtencionMedica, String entidadResponsable) {
}
