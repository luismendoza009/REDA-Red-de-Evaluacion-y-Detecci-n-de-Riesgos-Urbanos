package co.edu.unicesar.reda.model;

import co.edu.unicesar.reda.exception.ReglaNegocioException;
import jakarta.persistence.*;

/**
 * Abogado aliado (especialización 1:1 de un usuario con rol ABOGADO).
 * Nace INACTIVO: solo puede ver y tomar casos cuando un administrador verifica su tarjeta profesional.
 */
@Entity
@Table(name = "abogados")
public class Abogado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abogado")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "tarjeta_profesional", nullable = false, length = 20)
    private String tarjetaProfesional;

    @Column(name = "especialidad", nullable = false, length = 80)
    private String especialidad;

    @Column(name = "ciudad", nullable = false, length = 80)
    private String ciudad;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "anios_experiencia", nullable = false)
    private Short aniosExperiencia;

    @Column(name = "presentacion", columnDefinition = "text")
    private String presentacion;

    protected Abogado() {
    }

    public static Abogado solicitarVinculacion(Usuario usuario, String tarjetaProfesional, String especialidad,
                                               String ciudad, short aniosExperiencia, String presentacion) {
        if (!usuario.tieneRol(NombreRol.ABOGADO)) {
            throw new IllegalArgumentException("El usuario debe tener rol ABOGADO");
        }
        Abogado a = new Abogado();
        a.usuario = usuario;
        a.tarjetaProfesional = tarjetaProfesional.trim();
        a.especialidad = especialidad.trim();
        a.ciudad = ciudad.trim();
        a.aniosExperiencia = aniosExperiencia;
        a.presentacion = presentacion == null || presentacion.isBlank() ? null : presentacion.trim();
        a.activo = false;
        return a;
    }

    /** El administrador verificó la tarjeta profesional. */
    public void activar() {
        this.activo = true;
    }

    /**
     * Regla del negocio: el perfil público (foto, especialidad, presentación) solo se arma cuando REDA
     * verificó la tarjeta profesional. Así nadie se presenta ante ciudadanos como abogado sin serlo.
     */
    public void verificarPuedeEditarPerfil() {
        if (!activo) {
            throw new ReglaNegocioException("ABOGADO_NO_VERIFICADO",
                    "Tu perfil de abogado se habilita cuando un administrador verifique tu tarjeta profesional");
        }
    }

    public void actualizarPerfil(String especialidad, String ciudad, short aniosExperiencia, String presentacion) {
        verificarPuedeEditarPerfil();
        this.especialidad = especialidad.trim();
        this.ciudad = ciudad.trim();
        this.aniosExperiencia = aniosExperiencia;
        this.presentacion = presentacion == null || presentacion.isBlank() ? null : presentacion.trim();
    }

    public boolean esUsuario(Usuario otro) {
        return usuario.getId().equals(otro.getId());
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getTarjetaProfesional() { return tarjetaProfesional; }
    public String getEspecialidad() { return especialidad; }
    public String getCiudad() { return ciudad; }
    public boolean isActivo() { return activo; }
    public Short getAniosExperiencia() { return aniosExperiencia; }
    public String getPresentacion() { return presentacion; }
}
