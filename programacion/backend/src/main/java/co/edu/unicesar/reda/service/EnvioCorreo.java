package co.edu.unicesar.reda.service;

/**
 * Envío de correos (recuperación de contraseña, avisos).
 * DIP: en desarrollo la implementación escribe el correo en la consola del servidor; en producción
 * se agrega una implementación SMTP o de un proveedor, sin tocar los servicios.
 */
public interface EnvioCorreo {

    void enviar(String destinatario, String asunto, String cuerpo);
}
