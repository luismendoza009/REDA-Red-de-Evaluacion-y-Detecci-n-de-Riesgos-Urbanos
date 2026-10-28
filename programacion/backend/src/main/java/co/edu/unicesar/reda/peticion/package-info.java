/**
 * Peticiones: los datos que el cliente envía a la API (un formulario por operación).
 * Son records de Java (clases inmutables) con las validaciones de formato de Bean Validation.
 *
 * Validación en tres niveles (cada uno atrapa lo que le corresponde):
 *   1. Formato    -> aquí: obligatorios, longitudes, rango de coordenadas, patrón de contraseña.
 *   2. Negocio    -> en el dominio (model/): transiciones de estado, atención médica solo con lesión...
 *   3. Integridad -> en PostgreSQL (CHECK, FK, UNIQUE), la última barrera.
 *
 * Por qué no se reciben las entidades directamente: una entidad tiene campos que el cliente nunca debe
 * poder escribir (id, password_hash, rol, estado). Recibir solo lo necesario evita que alguien se
 * asigne, por ejemplo, el rol ADMINISTRADOR desde el formulario de registro.
 */
package co.edu.unicesar.reda.peticion;
