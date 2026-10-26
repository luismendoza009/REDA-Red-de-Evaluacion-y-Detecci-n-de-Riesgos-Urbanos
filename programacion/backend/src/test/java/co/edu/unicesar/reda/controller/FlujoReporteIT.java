package co.edu.unicesar.reda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de INTEGRACIÓN: controlador + servicio + dominio + JPA + PostgreSQL real, a través de HTTP.
 * Recorre el flujo de la demo: un ciudadano reporta un incidente en el punto crítico del Centro,
 * el sistema encuentra los 4 antecedentes y un operador lo revisa y valida.
 *
 * Requisito: base 'reda' construida con  base-de-datos/scripts/migrar.ps1 -Recrear -ConSemilla
 * Conexión: REDA_DB_URL (por defecto localhost:5432/reda), REDA_DB_USUARIO, REDA_DB_CLAVE.
 * Ejecutar: mvn verify -Pintegracion
 *
 * @Transactional: cada prueba se deshace al terminar, así no deja datos en la base.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FlujoReporteIT {

    /** Punto del incidente demo de la semilla (alcantarilla sin tapa, barrio Centro). */
    private static final double LAT_DEMO = 10.477120;
    private static final double LON_DEMO = -73.244850;
    private static final String OPERADOR_SEMILLA = "lorena.arzuaga2@correo.test";

    @DynamicPropertySource
    static void configuracion(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv().getOrDefault("REDA_DB_URL", "jdbc:postgresql://localhost:5432/reda"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("REDA_DB_CLAVE", ""));
        // secreto aleatorio por ejecución: ningún secreto queda escrito en el repositorio
        r.add("reda.jwt.secreto", () -> UUID.randomUUID() + "-" + UUID.randomUUID());
    }

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private String tokenCiudadano;
    private String tokenOperador;

    @BeforeEach
    void iniciarSesiones() throws Exception {
        tokenCiudadano = registrarCiudadano();
        tokenOperador = leer(enviar(HttpMethod.POST, "/api/auth/login",
                Map.of("correo", OPERADOR_SEMILLA, "password", "Reda2026*"), null)
                .andExpect(status().isOk())).path("token").asText();
    }

    @Test
    void elIncidenteEncuentraLosAntecedentesYElOperadorLoValida() throws Exception {
        JsonNode creado = leer(enviar(HttpMethod.POST, "/api/reportes/incidentes", incidenteDemo(), tokenCiudadano)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.estado").value("Pendiente"))
                .andExpect(jsonPath("$.antecedentes.length()").value(4))
                .andExpect(jsonPath("$.antecedentes[0].fechaReporte").value(startsWith("2026-05-19"))));
        String estado = "/api/reportes/" + creado.path("reporte").path("id").asInt() + "/estado";

        // saltarse la revisión no está permitido
        enviar(HttpMethod.PATCH, estado, Map.of("nuevoEstado", "VALIDADO"), tokenOperador)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));

        enviar(HttpMethod.PATCH, estado, Map.of("nuevoEstado", "EN_REVISION", "observacion", "Se revisa la foto"), tokenOperador)
                .andExpect(status().isOk());
        enviar(HttpMethod.PATCH, estado, Map.of("nuevoEstado", "VALIDADO", "observacion", "Evidencia verificada"), tokenOperador)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoAnterior").value("En revisión"))
                .andExpect(jsonPath("$.estadoNuevo").value("Validado"));

        enviar(HttpMethod.GET, "/api/reportes/" + creado.path("reporte").path("id").asInt(), null, tokenCiudadano)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Validado"))
                .andExpect(jsonPath("$.historial.length()").value(2))
                .andExpect(jsonPath("$.incidente.entidadResponsable").value("EMDUPAR S.A. E.S.P."));
    }

    @Test
    void lasReglasDeAccesoSeRespetan() throws Exception {
        enviar(HttpMethod.GET, "/api/reportes/mios", null, null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NO_AUTENTICADO"));

        int id = leer(enviar(HttpMethod.POST, "/api/reportes/incidentes", incidenteDemo(), tokenCiudadano))
                .path("reporte").path("id").asInt();

        // un ciudadano no cambia estados (lo impide su rol)
        enviar(HttpMethod.PATCH, "/api/reportes/" + id + "/estado", Map.of("nuevoEstado", "EN_REVISION"), tokenCiudadano)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ROL_SIN_PERMISO"));

        // otro ciudadano no ve el detalle (tiene datos de salud)
        enviar(HttpMethod.GET, "/api/reportes/" + id, null, registrarCiudadano())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void losErroresDeValidacionDicenQueCampoFallo() throws Exception {
        enviar(HttpMethod.POST, "/api/reportes/condiciones", Map.of("idCategoria", 1, "idZona", 1,
                "descripcion", "corto", "latitud", 95, "longitud", -73.2), tokenCiudadano)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.campos.descripcion").exists())
                .andExpect(jsonPath("$.campos.latitud").exists());
    }

    @Test
    void elMapaPublicoNoExponeDatosPersonales() throws Exception {
        String cuerpo = enviar(HttpMethod.GET, "/api/mapa/mapa", null, tokenCiudadano)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(cuerpo.contains("@correo.test") || cuerpo.contains("descripcionDano"),
                "el mapa público no debe llevar autor ni datos del daño");
    }

    // ------------------------------------------------------------------ utilidades

    private String registrarCiudadano() throws Exception {
        Map<String, Object> datos = Map.of("nombre", "Ciudadana de Prueba",
                "correo", "it-" + UUID.randomUUID() + "@correo.test",
                "password", "Segura2026*", "aceptaTratamientoDatos", true);
        return leer(enviar(HttpMethod.POST, "/api/auth/registro", datos, null)
                .andExpect(status().isCreated())).path("token").asText();
    }

    private Map<String, Object> incidenteDemo() {
        return Map.of(
                "falla", Map.of("idCategoria", 2, "idZona", 1, "descripcion", "Mi hijo cayó en la alcantarilla sin tapa",
                        "latitud", LAT_DEMO, "longitud", LON_DEMO, "nivelGravedad", 5),
                "tipoDano", "LESION",
                "descripcionDano", "Esguince y herida en la pierna, atendido en urgencias",
                "fechaHoraHecho", OffsetDateTime.now().minusHours(3).toString(),
                "huboAtencionMedica", true,
                "idEntidad", 5);
    }

    /** Envía una petición con cuerpo JSON opcional y token opcional. */
    private ResultActions enviar(HttpMethod metodo, String ruta, Object cuerpo, String token) throws Exception {
        MockHttpServletRequestBuilder peticion = MockMvcRequestBuilders.request(metodo, ruta);
        if (cuerpo != null) {
            peticion.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
        }
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return mvc.perform(peticion);
    }

    private JsonNode leer(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString());
    }
}
