package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "entidades_publicas")
public class EntidadPublica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entidad")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "ciudad", nullable = false, length = 80)
    private String ciudad;

    protected EntidadPublica() {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCiudad() { return ciudad; }
}
