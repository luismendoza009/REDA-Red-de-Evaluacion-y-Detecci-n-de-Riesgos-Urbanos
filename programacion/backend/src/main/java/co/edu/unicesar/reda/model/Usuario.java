package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Locale;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_consentimiento", nullable = false)
    private OffsetDateTime fechaConsentimiento;

    protected Usuario() {
    }

    /**
     * Fábrica: un ciudadano que se registra acepta en ese momento la política de datos.
     * El correo se normaliza a minúsculas (la base lo exige con un CHECK).
     * Recibe el hash ya calculado: esta clase no sabe de algoritmos de cifrado.
     */
    public static Usuario registrar(String nombre, String correo, String passwordHash, Rol rol) {
        Usuario u = new Usuario();
        OffsetDateTime ahora = OffsetDateTime.now();
        u.nombre = nombre.trim();
        u.correo = normalizarCorreo(correo);
        u.passwordHash = passwordHash;
        u.rol = rol;
        u.fechaRegistro = ahora;
        u.fechaConsentimiento = ahora;
        u.activo = true;
        return u;
    }

    public static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    /** Recibe el hash ya calculado: esta clase no sabe de algoritmos de cifrado. */
    public void cambiarPassword(String nuevoHash) {
        this.passwordHash = nuevoHash;
    }

    public void cambiarNombre(String nuevoNombre) {
        this.nombre = nuevoNombre.trim();
    }

    public boolean tieneRol(NombreRol nombreRol) {
        return rol.getNombre() == nombreRol;
    }

    /** Operadores y administradores son el "personal" que valida reportes. */
    public boolean esPersonalDeRevision() {
        return tieneRol(NombreRol.OPERADOR) || tieneRol(NombreRol.ADMINISTRADOR);
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public OffsetDateTime getFechaRegistro() { return fechaRegistro; }
    public boolean isActivo() { return activo; }
}
