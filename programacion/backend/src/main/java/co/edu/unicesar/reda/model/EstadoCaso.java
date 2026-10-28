package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.util.Arrays;

/** Fila del catálogo estados_caso. */
@Entity
@Table(name = "estados_caso")
public class EstadoCaso {

    /** Estados del caso legal, con su nombre tal como está en la tabla. */
    public enum Nombre {
        BORRADOR("Borrador"),
        EVALUADO("Evaluado"),
        /** Publicado a los abogados: recibe postulaciones. */
        REMITIDO("Remitido"),
        /** El ciudadano eligió a un abogado. */
        ACEPTADO("Aceptado por abogado"),
        /** Ningún abogado se interesó o el ciudadano desistió. */
        RECHAZADO("Rechazado"),
        CERRADO("Cerrado");

        private final String etiqueta;

        Nombre(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }

        static Nombre desdeEtiqueta(String etiqueta) {
            return Arrays.stream(values()).filter(n -> n.etiqueta.equals(etiqueta)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Estado de caso desconocido: " + etiqueta));
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_caso")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 40)
    private String nombre;

    protected EstadoCaso() {
    }

    /** Solo para pruebas unitarias del dominio. */
    static EstadoCaso de(Integer id, Nombre nombre) {
        EstadoCaso e = new EstadoCaso();
        e.id = id;
        e.nombre = nombre.etiqueta();
        return e;
    }

    public Nombre tipo() {
        return Nombre.desdeEtiqueta(nombre);
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
}
