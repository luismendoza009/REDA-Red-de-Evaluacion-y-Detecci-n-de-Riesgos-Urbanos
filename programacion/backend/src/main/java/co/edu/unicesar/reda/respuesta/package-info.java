/**
 * Respuestas: lo que la API devuelve en JSON. Records de Java (clases inmutables) que construyen los
 * servicios a partir de las entidades (ver {@code ReporteMapper} y {@code CasoMapper}).
 *
 * Por qué no se devuelven las entidades directamente:
 *   - Privacidad: cada respuesta lleva solo lo que ese rol puede ver. El mapa no lleva autor ni datos
 *     del daño; el abogado no elegido recibe un resumen anónimo (CasoDisponibleResponse); nunca sale
 *     el password_hash.
 *   - Bajo acoplamiento (GRASP): cambiar una tabla o una entidad no cambia el JSON que usa la interfaz.
 *   - Las entidades JPA tienen relaciones de carga perezosa que fallarían al convertirlas a JSON
 *     fuera de la transacción.
 */
package co.edu.unicesar.reda.respuesta;
