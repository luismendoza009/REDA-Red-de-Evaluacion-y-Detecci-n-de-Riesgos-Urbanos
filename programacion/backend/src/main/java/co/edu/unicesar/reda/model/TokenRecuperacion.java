package co.edu.unicesar.reda.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Enlace de un solo uso para restablecer la contraseña. Se guarda el HASH del token (nunca el token):
 * si alguien lee la tabla, no puede usar los enlaces.
 */
@Entity
@Table(name = "tokens_recuperacion")
public class TokenRecuperacion {

    public static final Duration VIGENCIA = Duration.ofMinutes(30);

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
    private OffsetDateTime fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "fecha_uso")
    private OffsetDateTime fechaUso;

    protected TokenRecuperacion() {
    }

    public static TokenRecuperacion para(Usuario usuario, String hashToken) {
        TokenRecuperacion t = new TokenRecuperacion();
        t.usuario = usuario;
        t.hashToken = hashToken;
        t.fechaCreacion = OffsetDateTime.now();
        t.fechaExpiracion = t.fechaCreacion.plus(VIGENCIA);
        return t;
    }

    public boolean sirveEn(OffsetDateTime momento) {
        return fechaUso == null && momento.isBefore(fechaExpiracion);
    }

    /** Marca el token como usado: un enlace no sirve dos veces. */
    public void usar(OffsetDateTime momento) {
        if (!sirveEn(momento)) {
            throw new IllegalStateException("El token ya no es válido");
        }
        this.fechaUso = momento;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public OffsetDateTime getFechaExpiracion() { return fechaExpiracion; }
}
