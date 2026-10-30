package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.model.FormatoImagen;
import co.edu.unicesar.reda.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FotoPerfilServiceTest {

    @Test
    void cambiaLaVersionDeLaRutaCuandoSeReemplazaLaFotoElMismoDia() {
        Usuario usuario = Usuario.registrar("Usuario Prueba", "prueba@correo.test", "hash", null);
        usuario.cambiarFoto("perfiles/primera.png", FormatoImagen.PNG);
        String rutaAnterior = FotoPerfilService.ruta(usuario);

        usuario.cambiarFoto("perfiles/segunda.png", FormatoImagen.PNG);

        assertNotEquals(rutaAnterior, FotoPerfilService.ruta(usuario));
    }
}
