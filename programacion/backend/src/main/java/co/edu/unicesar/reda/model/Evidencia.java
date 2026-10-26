package co.edu.unicesar.reda.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Foto de soporte de un reporte. Es EVIDENCIA PRIVADA: solo la ven el autor del reporte y el
 * personal de revisión (y, cuando exista, el abogado del caso). Solo la crea {@link Reporte#agregarEvidencia}.
 */
@Entity
@Table(name = "evidencias")
public class Evidencia {

    /** Tamaño máximo permitido (coincide con el CHECK ck_evidencias_tamano: 10 MB). */
    public static final int TAMANO_MAXIMO_BYTES = 10 * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reporte")
    private Reporte reporte;

    @Column(name = "ruta_imagen", nullable = false, length = 255)
    private String rutaImagen;

    @Column(name = "tipo_mime", nullable = false, length = 30)
    private String tipoMime;

    @Column(name = "tamano_bytes", nullable = false)
    private Integer tamanoBytes;

    /** CHAR(64) en la base: se declara el tipo para que la validación del esquema no lo confunda con VARCHAR. */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "fecha_captura")
    private OffsetDateTime fechaCaptura;

    @Column(name = "fecha_carga", nullable = false)
    private OffsetDateTime fechaCarga;

    protected Evidencia() {
    }

    static Evidencia de(Reporte reporte, String rutaImagen, FormatoImagen formato, int tamanoBytes, String hashSha256) {
        Evidencia e = new Evidencia();
        e.reporte = reporte;
        e.rutaImagen = rutaImagen;
        e.tipoMime = formato.mime();
        e.tamanoBytes = tamanoBytes;
        e.hashSha256 = hashSha256;
        e.fechaCarga = OffsetDateTime.now();
        return e;
    }

    public Integer getId() { return id; }
    public Reporte getReporte() { return reporte; }
    public String getRutaImagen() { return rutaImagen; }
    public String getTipoMime() { return tipoMime; }
    public Integer getTamanoBytes() { return tamanoBytes; }
    public String getHashSha256() { return hashSha256; }
    public OffsetDateTime getFechaCaptura() { return fechaCaptura; }
    public OffsetDateTime getFechaCarga() { return fechaCarga; }
}
