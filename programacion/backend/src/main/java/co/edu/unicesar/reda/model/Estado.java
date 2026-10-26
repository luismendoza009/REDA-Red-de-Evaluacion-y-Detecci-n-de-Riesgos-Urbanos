package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

/** Fila del catálogo estados. Delega las reglas de transición en {@link NombreEstado}. */
@Entity
@Table(name = "estados")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    protected Estado() {
    }

    /** Solo para pruebas unitarias del dominio (sin base de datos). */
    static Estado de(Integer id, NombreEstado nombre) {
        Estado e = new Estado();
        e.id = id;
        e.nombre = nombre.etiqueta();
        return e;
    }

    public NombreEstado tipo() {
        return NombreEstado.desdeEtiqueta(nombre);
    }

    public boolean puedePasarA(Estado destino) {
        return tipo().puedePasarA(destino.tipo());
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
