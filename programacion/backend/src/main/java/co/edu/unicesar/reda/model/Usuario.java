package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Optional;

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

    /** Foto de perfil (migración 013): ruta generada en el almacenamiento y formato real. */
    @Column(name = "foto_perfil", length = 255)
    private String fotoPerfil;

    @Column(name = "foto_perfil_mime", length = 20)
    private String fotoPerfilMime;

    @Column(name = "fecha_foto_perfil")
    private OffsetDateTime fechaFotoPerfil;

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

    /** Pone una foto nueva y devuelve la ruta de la anterior (si había), para borrar ese archivo. */
    public Optional<String> cambiarFoto(String ruta, FormatoImagen formato) {
        Optional<String> anterior = Optional.ofNullable(fotoPerfil);
        this.fotoPerfil = ruta;
        this.fotoPerfilMime = formato.mime();
        this.fechaFotoPerfil = OffsetDateTime.now();
        return anterior;
    }

    /** Quita la foto y devuelve la ruta del archivo que ya no se usa. */
    public Optional<String> quitarFoto() {
        Optional<String> anterior = Optional.ofNullable(fotoPerfil);
        this.fotoPerfil = null;
        this.fotoPerfilMime = null;
        this.fechaFotoPerfil = null;
        return anterior;
    }

    public boolean tieneFoto() {
        return fotoPerfil != null;
    }

    /**
     * Experto: quién puede ver mi foto. Yo mismo y el personal, siempre. Si soy un abogado verificado,
     * cualquier usuario con sesión (es mi perfil profesional, el ciudadano la ve para elegirme).
     * La foto de un ciudadano no se muestra a otros ciudadanos ni a los abogados.
     */
    public boolean fotoVisiblePara(Usuario quienMira, boolean soyAbogadoVerificado) {
        return id.equals(quienMira.getId()) || quienMira.esPersonalDeRevision() || soyAbogadoVerificado;
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
    public String getFotoPerfil() { return fotoPerfil; }
    public String getFotoPerfilMime() { return fotoPerfilMime; }
    public OffsetDateTime getFechaFotoPerfil() { return fechaFotoPerfil; }
}
