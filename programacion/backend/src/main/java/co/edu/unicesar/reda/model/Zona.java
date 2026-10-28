package co.edu.unicesar.reda.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalDouble;

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

    /** Punto de referencia real dentro del barrio (OpenStreetMap, migración 012). Puede faltar. */
    @Column(name = "latitud_referencia", precision = 9, scale = 6)
    private BigDecimal latitudReferencia;

    @Column(name = "longitud_referencia", precision = 9, scale = 6)
    private BigDecimal longitudReferencia;

    @Column(name = "fuente_ubicacion", length = 200)
    private String fuenteUbicacion;

    protected Zona() {
    }

    /** Solo para pruebas del dominio. */
    Zona(Integer id, String nombre, BigDecimal latitud, BigDecimal longitud) {
        this.id = id;
        this.nombre = nombre;
        this.latitudReferencia = latitud;
        this.longitudReferencia = longitud;
    }

    /** Experto: la zona conoce su ubicación, así que sabe a qué distancia queda un punto. Vacío si no está ubicada. */
    public OptionalDouble distanciaA(double latitud, double longitud) {
        if (latitudReferencia == null || longitudReferencia == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(Distancia.metros(latitudReferencia.doubleValue(), longitudReferencia.doubleValue(),
                latitud, longitud));
    }

    /**
     * La zona ubicada más cercana al punto, si está a no más de {@code radioMaximoMetros}.
     * Es una sugerencia: el punto de referencia no es el límite del barrio, por eso el ciudadano puede cambiarla.
     */
    public static Optional<Zona> masCercana(Collection<Zona> zonas, double latitud, double longitud, double radioMaximoMetros) {
        return zonas.stream()
                .filter(z -> z.distanciaA(latitud, longitud).isPresent())
                .min(Comparator.comparingDouble(z -> z.distanciaA(latitud, longitud).getAsDouble()))
                .filter(z -> z.distanciaA(latitud, longitud).getAsDouble() <= radioMaximoMetros);
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public String getCiudad() { return ciudad; }
    public Short getNivelTrafico() { return nivelTrafico; }
    public Short getCentrosEducativos() { return centrosEducativos; }
    public Short getCentrosSalud() { return centrosSalud; }
    public BigDecimal getLatitudReferencia() { return latitudReferencia; }
    public BigDecimal getLongitudReferencia() { return longitudReferencia; }
    public String getFuenteUbicacion() { return fuenteUbicacion; }
}
