package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.AccesoDenegadoException;
import co.edu.unicesar.reda.exception.RecursoNoEncontradoException;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Abogado;
import co.edu.unicesar.reda.model.FormatoImagen;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.AbogadoRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import co.edu.unicesar.reda.respuesta.ArchivoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Casos de uso de la foto de perfil: cambiarla, quitarla y verla.
 * Alta cohesión: separado de PerfilService (datos de la cuenta) y de EvidenciaService (fotos de evidencia),
 * aunque reutiliza las mismas piezas: FormatoImagen (formato real por los bytes) y AlmacenamientoArchivos.
 */
@Service
public class FotoPerfilService {

    /** Una foto de perfil no necesita la resolución de una evidencia. */
    public static final int MAX_BYTES = 5 * 1024 * 1024;

    private final UsuarioRepository usuarios;
    private final AbogadoRepository abogados;
    private final AlmacenamientoArchivos almacenamiento;

    public FotoPerfilService(UsuarioRepository usuarios, AbogadoRepository abogados, AlmacenamientoArchivos almacenamiento) {
        this.usuarios = usuarios;
        this.abogados = abogados;
        this.almacenamiento = almacenamiento;
    }

    /** Ruta para descargar la foto (la versión cambia con cada foto nueva, así el navegador no muestra la vieja). */
    public static String ruta(Usuario u) {
        return u.tieneFoto() ? "/api/usuarios/" + u.getId() + "/foto?v="
                + new BigInteger(1, u.getFotoPerfil().getBytes(StandardCharsets.UTF_8)) : null;
    }

    @Transactional
    public String cambiar(Integer idUsuario, byte[] contenido) {
        Usuario u = Busqueda.porId(usuarios, idUsuario, "Usuario");
        // un abogado sin verificar todavía no tiene perfil público
        abogados.findByUsuarioId(idUsuario).ifPresent(Abogado::verificarPuedeEditarPerfil);
        if (contenido.length == 0) {
            throw new ReglaNegocioException("ARCHIVO_VACIO", "No se recibió ninguna foto");
        }
        if (contenido.length > MAX_BYTES) {
            throw new ReglaNegocioException("FOTO_MUY_GRANDE", "La foto de perfil no puede pesar más de 5 MB");
        }
        FormatoImagen formato = FormatoImagen.detectar(contenido).orElseThrow(() -> new ReglaNegocioException(
                "FORMATO_NO_PERMITIDO", "La foto debe ser JPG, PNG o WEBP"));
        String nueva = almacenamiento.guardar("perfiles", contenido, formato.extension());
        u.cambiarFoto(nueva, formato).ifPresent(this::borrarAlConfirmar);
        return ruta(u);
    }

    @Transactional
    public void quitar(Integer idUsuario) {
        Busqueda.porId(usuarios, idUsuario, "Usuario").quitarFoto().ifPresent(this::borrarAlConfirmar);
    }

    /** El archivo viejo se borra solo si la base confirmó el cambio; si no, la base seguiría apuntando a él. */
    private void borrarAlConfirmar(String ruta) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            almacenamiento.borrar(ruta);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                almacenamiento.borrar(ruta);
            }
        });
    }

    @Transactional(readOnly = true)
    public ArchivoResponse ver(Integer idDueno, Integer idQuienMira) {
        Usuario dueno = Busqueda.porId(usuarios, idDueno, "Usuario");
        Usuario quienMira = Busqueda.porId(usuarios, idQuienMira, "Usuario");
        boolean abogadoVerificado = abogados.findByUsuarioId(idDueno).map(Abogado::isActivo).orElse(false);
        if (!dueno.fotoVisiblePara(quienMira, abogadoVerificado)) {
            throw new AccesoDenegadoException("Esta foto de perfil es privada");
        }
        return Optional.ofNullable(dueno.getFotoPerfil())
                .flatMap(almacenamiento::leer)
                .map(bytes -> new ArchivoResponse(bytes, dueno.getFotoPerfilMime()))
                .orElseThrow(() -> new RecursoNoEncontradoException("Foto de perfil", idDueno));
    }
}
