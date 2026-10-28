package co.edu.unicesar.reda.security;

import co.edu.unicesar.reda.model.NombreRol;

/** Quién hace la petición, tal como viene en el token. Se inyecta en los controladores con @AuthenticationPrincipal. */
public record UsuarioAutenticado(Integer id, String nombre, NombreRol rol) {
}
