package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

@Entity
@Table(name = "zonas")
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "ciudad", nullable = false, length = 80)
    private String ciudad;

    @Column(name = "nivel_trafico", nullable = false)
    private Short nivelTrafico;

    @Column(name = "centros_educativos", nullable = false)
    private Short centrosEducativos;

    @Column(name = "centros_salud", nullable = false)
    private Short centrosSalud;

    protected Zona() {
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCiudad() { return ciudad; }
    public Short getNivelTrafico() { return nivelTrafico; }
    public Short getCentrosEducativos() { return centrosEducativos; }
    public Short getCentrosSalud() { return centrosSalud; }
}
