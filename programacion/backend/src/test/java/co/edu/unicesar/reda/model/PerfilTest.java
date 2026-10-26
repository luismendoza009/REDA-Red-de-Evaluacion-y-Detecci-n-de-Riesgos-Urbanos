package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas de la foto de perfil y del perfil profesional del abogado, sin Spring ni base de datos. */
class PerfilTest {

    private static Usuario usuario(int id, NombreRol rol) {
        Usuario u = Usuario.registrar("Persona " + id, "persona" + id + "@correo.test", "$2a$10$hash", Rol.de(id, rol));
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private final Usuario ciudadana = usuario(10, NombreRol.CIUDADANO);
    private final Usuario vecino = usuario(11, NombreRol.CIUDADANO);
    private final Usuario operador = usuario(2, NombreRol.OPERADOR);
    private final Usuario usuarioAbogado = usuario(20, NombreRol.ABOGADO);

    @Test
    void cambiarLaFotoDevuelveLaAnteriorParaBorrarla() {
        assertTrue(ciudadana.cambiarFoto("perfiles/2026/10/a.jpg", FormatoImagen.JPEG).isEmpty(), "no había foto");
        assertEquals("perfiles/2026/10/a.jpg", ciudadana.cambiarFoto("perfiles/2026/10/b.png", FormatoImagen.PNG).orElseThrow());
        assertEquals("image/png", ciudadana.getFotoPerfilMime());
        assertNotNull(ciudadana.getFechaFotoPerfil());

        assertEquals("perfiles/2026/10/b.png", ciudadana.quitarFoto().orElseThrow());
        assertFalse(ciudadana.tieneFoto());
        assertNull(ciudadana.getFotoPerfilMime());
        assertNull(ciudadana.getFechaFotoPerfil(), "sin foto no queda fecha (CHECK ck_usuarios_foto_completa)");
    }

    @Test
    void laFotoDeUnCiudadanoSoloLaVenElYElPersonal() {
        assertTrue(ciudadana.fotoVisiblePara(ciudadana, false));
        assertTrue(ciudadana.fotoVisiblePara(operador, false));
        assertFalse(ciudadana.fotoVisiblePara(vecino, false), "otro ciudadano no ve mi foto");
        assertFalse(ciudadana.fotoVisiblePara(usuarioAbogado, false), "un abogado no ve la foto de un ciudadano");
    }

    @Test
    void laFotoDeUnAbogadoVerificadoEsParteDeSuPerfilProfesional() {
        assertTrue(usuarioAbogado.fotoVisiblePara(vecino, true));
        assertFalse(usuarioAbogado.fotoVisiblePara(vecino, false), "sin verificar no tiene perfil público");
    }

    @Test
    void unAbogadoSinVerificarNoArmaSuPerfil() {
        Abogado a = Abogado.solicitarVinculacion(usuarioAbogado, "123456", "Derecho administrativo", "Valledupar", (short) 3, null);
        assertEquals("ABOGADO_NO_VERIFICADO", assertThrows(ReglaNegocioException.class,
                a::verificarPuedeEditarPerfil).getCodigo());
        assertEquals("ABOGADO_NO_VERIFICADO", assertThrows(ReglaNegocioException.class,
                () -> a.actualizarPerfil("Responsabilidad del Estado", "Valledupar", (short) 4, "Hola")).getCodigo());
        assertEquals("Derecho administrativo", a.getEspecialidad(), "no cambió nada");

        a.activar();
        a.actualizarPerfil("  Responsabilidad del Estado ", "Valledupar", (short) 4, "   ");
        assertEquals("Responsabilidad del Estado", a.getEspecialidad());
        assertEquals((short) 4, a.getAniosExperiencia());
        assertNull(a.getPresentacion(), "una presentación en blanco se guarda como vacía");
    }
}
