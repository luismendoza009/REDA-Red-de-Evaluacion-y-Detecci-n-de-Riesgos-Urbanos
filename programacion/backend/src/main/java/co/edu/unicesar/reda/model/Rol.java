package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nombre", nullable = false, length = 30)
    private NombreRol nombre;

    protected Rol() {
    }

    /** Solo para pruebas unitarias del dominio (sin base de datos). */
    static Rol de(Integer id, NombreRol nombre) {
        Rol r = new Rol();
        r.id = id;
        r.nombre = nombre;
        return r;
    }

    public Integer getId() { return id; }
    public NombreRol getNombre() { return nombre; }
}
