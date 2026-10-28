/**
 * Acceso a datos. Los repositorios de Spring Data son interfaces y Spring genera su implementación.
 * GRASP "Fabricación pura": no son conceptos del negocio; existen para sacar el acceso a datos de
 * las entidades y mantenerlas cohesivas. Toda consulta SQL nativa usa parámetros (nunca concatena
 * texto del usuario), lo que previene la inyección SQL.
 */
package co.edu.unicesar.reda.repository;
