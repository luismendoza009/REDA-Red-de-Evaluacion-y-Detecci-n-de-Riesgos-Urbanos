package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.dto.EvidenciaDtos.ArchivoResponse;
import co.edu.unicesar.reda.dto.EvidenciaDtos.EvidenciaResponse;
import co.edu.unicesar.reda.exception.AccesoDenegadoException;
import co.edu.unicesar.reda.exception.ConflictoException;
import co.edu.unicesar.reda.exception.ReglaNegocioException;
import co.edu.unicesar.reda.model.Evidencia;
import co.edu.unicesar.reda.model.FormatoImagen;
import co.edu.unicesar.reda.model.Reporte;
import co.edu.unicesar.reda.model.Usuario;
import co.edu.unicesar.reda.repository.EvidenciaRepository;
import co.edu.unicesar.reda.repository.ReporteRepository;
import co.edu.unicesar.reda.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

/**
 * Casos de uso de las fotos de evidencia: subir, listar y ver.
 * Las fotos son PRIVADAS: solo las ven el autor del reporte y el personal de revisión
 * (la regla la decide Reporte.puedeSerVistoPor).
 */
@Service
public class EvidenciaService {

    private final ReporteRepository reportes;
    private final UsuarioRepository usuarios;
    private final EvidenciaRepository evidencias;
    private final AlmacenamientoArchivos almacenamiento;

    public EvidenciaService(ReporteRepository reportes, UsuarioRepository usuarios, EvidenciaRepository evidencias,
                            AlmacenamientoArchivos almacenamiento) {
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.evidencias = evidencias;
        this.almacenamiento = almacenamiento;
    }

    /**
     * El autor sube una foto a su reporte. Se valida el formato REAL del archivo (por sus bytes),
     * se calcula su huella SHA-256 (prueba de integridad) y se rechaza si la misma foto ya fue cargada.
     */
    @Transactional
    public EvidenciaResponse subir(Integer idReporte, byte[] contenido, Integer idUsuario) {
        Reporte reporte = Busqueda.porId(reportes, idReporte, "Reporte");
        Usuario autor = Busqueda.porId(usuarios, idUsuario, "Usuario");
        // 1. permiso y tamaño PRIMERO: a quien no es el autor no se le dice nada sobre la foto
        //    (si el hash se revisara antes, sabría que esa foto ya existe en el sistema)
        reporte.verificarPuedeRecibirEvidencia(autor, contenido.length);
        // 2. formato real del archivo, por sus bytes
        FormatoImagen formato = FormatoImagen.detectar(contenido).orElseThrow(() -> new ReglaNegocioException(
                "FORMATO_NO_PERMITIDO", "La evidencia debe ser una foto JPG, PNG o WEBP"));
        // 3. huella SHA-256: la misma foto no se carga dos veces
        String hash = sha256(contenido);
        if (evidencias.existsByHashSha256(hash)) {
            throw new ConflictoException("FOTO_DUPLICADA", "Esa misma foto ya fue cargada como evidencia");
        }
        // 4. solo si todo pasó se escribe el archivo (no quedan archivos huérfanos)
        String ruta = almacenamiento.guardar(contenido, formato.extension());
        Evidencia evidencia = reporte.agregarEvidencia(autor, ruta, formato, contenido.length, hash);
        reportes.flush();
        return respuesta(evidencia);
    }

    @Transactional(readOnly = true)
    public List<EvidenciaResponse> listar(Integer idReporte, Integer idUsuario) {
        Reporte reporte = Busqueda.porId(reportes, idReporte, "Reporte");
        verificarAcceso(reporte, idUsuario);
        return evidencias.findByReporteIdOrderByFechaCargaAsc(idReporte).stream().map(EvidenciaService::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ArchivoResponse archivo(Integer idEvidencia, Integer idUsuario) {
        Evidencia evidencia = Busqueda.porId(evidencias, idEvidencia, "Evidencia");
        Reporte reporte = evidencia.getReporte();
        verificarAcceso(reporte, idUsuario);
        return almacenamiento.leer(evidencia.getRutaImagen())
                .map(bytes -> new ArchivoResponse(bytes, evidencia.getTipoMime()))
                .orElseGet(() -> new ArchivoResponse(
                        ImagenSimulada.para(reporte.getCategoria().getNombre(), evidencia.getId()), ImagenSimulada.MIME));
    }

    private void verificarAcceso(Reporte reporte, Integer idUsuario) {
        if (!reporte.puedeSerVistoPor(Busqueda.porId(usuarios, idUsuario, "Usuario"))) {
            throw new AccesoDenegadoException("Las fotos son evidencia privada: solo las ven el autor y el personal de revisión");
        }
    }

    static String sha256(byte[] contenido) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(contenido));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible en esta JVM", e);
        }
    }

    private static EvidenciaResponse respuesta(Evidencia e) {
        return new EvidenciaResponse(e.getId(), e.getTipoMime(), e.getTamanoBytes(), e.getHashSha256(),
                e.getFechaCaptura(), e.getFechaCarga(), "/api/evidencias/" + e.getId() + "/archivo");
    }
}
