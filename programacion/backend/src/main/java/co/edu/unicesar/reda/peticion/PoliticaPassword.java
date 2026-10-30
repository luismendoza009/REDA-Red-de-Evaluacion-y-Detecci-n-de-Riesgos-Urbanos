package co.edu.unicesar.reda.peticion;

/**
 * Política de contraseñas, definida una sola vez (código limpio: sin duplicar la regla en cada formulario).
 * 8 a 72 caracteres, con al menos una letra y un número. BCrypt solo usa los primeros 72 bytes.
 */
public final class PoliticaPassword {

    public static final String PATRON = "^(?=.*\\p{L})(?=.*\\d).{8,72}$";
    public static final String MENSAJE = "debe tener entre 8 y 72 caracteres, con al menos una letra y un número";

    private PoliticaPassword() {
    }
}
