package co.edu.unicesar.reda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "bloqueos_login")
public class BloqueoLogin {

    @Id
    @Column(name = "correo", nullable = false, length = 150)
    private String correo;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "bloqueado_hasta")
    private LocalDate bloqueadoHasta;

    @Column(name = "bloqueado_permanente", nullable = false)
    private boolean bloqueadoPermanente;

    protected BloqueoLogin() {
    }

    public BloqueoLogin(String correo, int intentosFallidos, LocalDate bloqueadoHasta, boolean bloqueadoPermanente) {
        this.correo = correo;
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
        this.bloqueadoPermanente = bloqueadoPermanente;
    }

    public String getCorreo() { return correo; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public LocalDate getBloqueadoHasta() { return bloqueadoHasta; }
    public boolean isBloqueadoPermanente() { return bloqueadoPermanente; }
}
