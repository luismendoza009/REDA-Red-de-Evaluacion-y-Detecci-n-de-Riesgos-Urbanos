package co.edu.unicesar.reda.repository;

import co.edu.unicesar.reda.service.EnvioCorreo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * "Envía" el correo escribiéndolo en la consola del servidor. Es la implementación de DESARROLLO:
 * no hay servidor de correo en el entorno del proyecto. Para producción se agrega otra
 * implementación de {@link EnvioCorreo} (SMTP) y se elimina o desactiva esta.
 */
@Component
public class CorreoConsola implements EnvioCorreo {

    private static final Logger log = LoggerFactory.getLogger(CorreoConsola.class);

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        log.warn("""

                ===================== CORREO SIMULADO (entorno de desarrollo) =====================
                Para:    {}
                Asunto:  {}
                {}
                ===================================================================================""",
                destinatario, asunto, cuerpo);
    }
}
