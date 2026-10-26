package co.edu.unicesar.reda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integración del mapa público y de las fotos privadas contra PostgreSQL real.
 * Requiere la base con la semilla (base-de-datos/scripts/migrar.ps1 -Recrear -ConSemilla).
 * Las fotos se escriben en una carpeta temporal que JUnit borra al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MapaYEvidenciasIT {

    /** Autor del incidente demo (reporte 137) en la semilla determinista. */
    private static final String AUTOR_DEMO = "lorena.cuello53@correo.test";
    private static final int REPORTE_DEMO = 137;

    @TempDir
    static Path carpetaFotos;

    @DynamicPropertySource
    static void configuracion(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv().getOrDefault("REDA_DB_URL", "jdbc:postgresql://localhost:5432/reda"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("REDA_DB_CLAVE", ""));
        r.add("reda.jwt.secreto", () -> UUID.randomUUID() + "-" + UUID.randomUUID());
        r.add("reda.almacenamiento.directorio", () -> carpetaFotos.toString());
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    private String sesion;

    @org.junit.jupiter.api.BeforeEach
    void iniciarSesion() throws Exception {
        sesion = token(AUTOR_DEMO);
    }

    /** El mapa exige sesión: toda consulta lleva el token. */
    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder conSesion(String ruta) {
        return get(ruta).header("Authorization", "Bearer " + sesion);
    }

    @Test
    void elPuntoDelCentroAgrupaLosCincoReportesDeLaAlcantarilla() throws Exception {
        JsonNode puntos = json.readTree(mvc.perform(conSesion("/api/mapa/puntos")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        JsonNode centro = null;
        for (JsonNode p : puntos) {
            for (JsonNode r : p.path("reportes")) {
                if (r.path("id").asInt() == REPORTE_DEMO) centro = p;
            }
        }
        assertNotNull(centro, "el incidente demo debe estar en algún punto");
        assertEquals(5, centro.path("totalReportes").asInt());
        assertEquals("CRITICA", centro.path("frecuencia").asText());
        assertEquals(1, centro.path("incidentes").asInt());
        // lista ordenada del más reciente al más antiguo
        assertEquals(REPORTE_DEMO, centro.path("reportes").get(0).path("id").asInt());
    }

    @Test
    void elMapaPublicoNoExponeDatosPrivados() throws Exception {
        String puntos = mvc.perform(conSesion("/api/mapa/puntos")).andReturn().getResponse().getContentAsString();
        String detalle = mvc.perform(conSesion("/api/mapa/reportes/" + REPORTE_DEMO)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // comparación exacta contra los datos sensibles reales (buscar palabras sueltas daba falsos
        // positivos: "pavimento fracturado" no es un dato de salud)
        var danos = jdbc.queryForList("SELECT descripcion_dano FROM incidentes", String.class);
        var nombres = jdbc.queryForList("SELECT nombre FROM usuarios", String.class);
        assertFalse(danos.isEmpty());
        for (String cuerpo : new String[]{puntos, detalle}) {
            assertFalse(cuerpo.contains("@correo.test"), "sin correos");
            assertFalse(cuerpo.contains("descripcionDano"), "sin el campo del daño");
            danos.forEach(d -> assertFalse(cuerpo.contains(d), "se filtró un dato de salud: " + d));
            nombres.forEach(n -> assertFalse(cuerpo.contains(n), "se filtró el nombre de una persona: " + n));
            assertFalse(cuerpo.contains("/api/evidencias/"), "sin enlaces a fotos");
        }
    }

    @Test
    void unReporteRechazadoNoTieneDetallePublico() throws Exception {
        Integer rechazado = jdbc.queryForObject("""
                SELECT min(r.id_reporte) FROM reportes r JOIN estados e USING (id_estado) WHERE e.nombre = 'Rechazado'
                """, Integer.class);
        assertNotNull(rechazado, "la semilla debe tener al menos un reporte rechazado");
        mvc.perform(conSesion("/api/mapa/reportes/" + rechazado)).andExpect(status().isNotFound());
        mvc.perform(conSesion("/api/mapa/reportes/999999")).andExpect(status().isNotFound());
    }

    @Test
    void elAutorSubeYVeSusFotosYNadieMasLasVe() throws Exception {
        String autor = token(AUTOR_DEMO);
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 9, 8, 7, (byte) UUID.randomUUID().hashCode()};

        String creada = mvc.perform(multipart("/api/reportes/" + REPORTE_DEMO + "/evidencias")
                        .file(new MockMultipartFile("archivo", "mi-foto.jpg", "image/jpeg", jpeg))
                        .header("Authorization", "Bearer " + autor))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMime").value("image/jpeg"))
                .andReturn().getResponse().getContentAsString();
        String url = json.readTree(creada).path("urlArchivo").asText();

        byte[] bajada = mvc.perform(get(url).header("Authorization", "Bearer " + autor))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsByteArray();
        assertArrayEquals(jpeg, bajada, "la foto debe volver idéntica");

        mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(get(url).header("Authorization", "Bearer " + registrarOtroCiudadano()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    private String token(String correo) throws Exception {
        String r = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("correo", correo, "password", "Reda2026*"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).path("token").asText();
    }

    private String registrarOtroCiudadano() throws Exception {
        String r = mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("nombre", "Vecino", "correo", "it-" + UUID.randomUUID() + "@correo.test",
                                "password", "Segura2026*", "aceptaTratamientoDatos", true))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return json.readTree(r).path("token").asText();
    }
}
