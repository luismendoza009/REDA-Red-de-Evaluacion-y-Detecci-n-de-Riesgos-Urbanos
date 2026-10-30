package co.edu.unicesar.reda.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Enlace de un solo uso para restablecer la contraseña. Se guarda el HASH del token (nunca el token):
 * si alguien lee la tabla, no puede usar los enlaces.
 */
@Entity
@Table(name = "tokens_recuperacion")
public class TokenRecuperacion {

    public static final long VIGENCIA_DIAS = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "hash_token", nullable = false, length = 64)
    private String hashToken;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDate fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDate fechaExpiracion;

    @Column(name = "fecha_uso")
    private LocalDate fechaUso;

    protected TokenRecuperacion() {
    }

    public static TokenRecuperacion para(Usuario usuario, String hashToken) {
        TokenRecuperacion t = new TokenRecuperacion();
        t.usuario = usuario;
        t.hashToken = hashToken;
        t.fechaCreacion = LocalDate.now(ZoneId.of("America/Bogota"));
        t.fechaExpiracion = t.fechaCreacion.plusDays(VIGENCIA_DIAS);
        return t;
    }

    public boolean sirveEn(LocalDate momento) {
        return fechaUso == null && momento.isBefore(fechaExpiracion);
    }

    /** Marca el token como usado: un enlace no sirve dos veces. */
    public void usar(LocalDate momento) {
        if (!sirveEn(momento)) {
            throw new IllegalStateException("El token ya no es válido");
        }
        this.fechaUso = momento;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public LocalDate getFechaExpiracion() { return fechaExpiracion; }
}
