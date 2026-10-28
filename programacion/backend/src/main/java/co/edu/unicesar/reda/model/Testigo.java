package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

/** Persona que presenció el incidente. Dato PERSONAL: solo lo ve el abogado asignado y el personal de revisión. */
@Entity
@Table(name = "testigos")
public class Testigo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_testigo")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_incidente")
    private Incidente incidente;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "contacto", nullable = false, length = 150)
    private String contacto;

    protected Testigo() {
    }

    static Testigo de(Incidente incidente, String nombre, String contacto) {
        Testigo t = new Testigo();
        t.incidente = incidente;
        t.nombre = nombre.trim();
        t.contacto = contacto.trim();
        return t;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getContacto() { return contacto; }
}
