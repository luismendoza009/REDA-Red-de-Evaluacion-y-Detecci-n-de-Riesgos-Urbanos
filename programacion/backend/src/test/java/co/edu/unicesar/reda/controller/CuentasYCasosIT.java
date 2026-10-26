package co.edu.unicesar.reda.controller;

import co.edu.unicesar.reda.service.EnvioCorreo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración de las cuentas (registro, recuperación de contraseña, límite de intentos) y del flujo
 * "varios abogados se postulan y el ciudadano elige", contra PostgreSQL real con la semilla.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CuentasYCasosIT {

    @TempDir
    static Path carpetaFotos;

    @DynamicPropertySource
    static void configuracion(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv().getOrDefault("REDA_DB_URL", "jdbc:postgresql://localhost:5432/reda"));
        r.add("spring.datasource.password", () -> System.getenv().getOrDefault("REDA_DB_CLAVE", ""));
        r.add("reda.jwt.secreto", () -> UUID.randomUUID() + "-" + UUID.randomUUID());
        r.add("reda.almacenamiento.directorio", () -> carpetaFotos.toString());
    }

    /** Reemplaza el correo de consola por un buzón en memoria, para leer el enlace de recuperación. */
    @TestConfiguration
    static class Buzon {
        static final List<String> CORREOS = new ArrayList<>();

        @Bean
        @Primary
        EnvioCorreo correoDePrueba() {
            return (destino, asunto, cuerpo) -> CORREOS.add(cuerpo);
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @Test
    void sinSesionNoSeVeElMapaNiLosCatalogos() throws Exception {
        enviar(HttpMethod.GET, "/api/mapa/puntos", null, null).andExpect(status().isUnauthorized());
        enviar(HttpMethod.GET, "/api/catalogos/zonas", null, null).andExpect(status().isUnauthorized());
        enviar(HttpMethod.GET, "/", null, null).andExpect(status().isOk()); // la página de entrada sí carga
    }

    @Test
    void recuperarLaContrasenaConUnEnlaceDeUnSoloUso() throws Exception {
        String correo = "it-" + UUID.randomUUID() + "@correo.test";
        registrar(correo, "Original2026");

        // la respuesta es la misma exista o no el correo (no revela quién tiene cuenta)
        String paraExistente = cuerpo(enviar(HttpMethod.POST, "/api/auth/recuperar", Map.of("correo", correo), null)
                .andExpect(status().isAccepted()));
        String paraInexistente = cuerpo(enviar(HttpMethod.POST, "/api/auth/recuperar",
                Map.of("correo", "nadie-" + UUID.randomUUID() + "@correo.test"), null).andExpect(status().isAccepted()));
        assertEquals(paraExistente, paraInexistente);

        Matcher m = Pattern.compile("#/restablecer/([A-Za-z0-9_-]+)").matcher(Buzon.CORREOS.get(Buzon.CORREOS.size() - 1));
        assertTrue(m.find(), "el correo debe traer el enlace");
        String token = m.group(1);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM tokens_recuperacion WHERE hash_token LIKE ?", Integer.class,
                "%" + token + "%"), "en la base se guarda el hash, no el token");

        enviar(HttpMethod.POST, "/api/auth/restablecer", Map.of("token", token, "nuevaPassword", "Nueva2026"), null)
                .andExpect(status().isOk());
        enviar(HttpMethod.POST, "/api/auth/restablecer", Map.of("token", token, "nuevaPassword", "Otra2026x"), null)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("ENLACE_INVALIDO"));
        login(correo, "Nueva2026");
        enviar(HttpMethod.POST, "/api/auth/login", Map.of("correo", correo, "password", "Original2026"), null)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void demasiadosIntentosDeLoginSeBloquean() throws Exception {
        String correo = "it-" + UUID.randomUUID() + "@correo.test";
        for (int i = 0; i < 8; i++) {
            enviar(HttpMethod.POST, "/api/auth/login", Map.of("correo", correo, "password", "mala"), null)
                    .andExpect(status().isUnauthorized());
        }
        enviar(HttpMethod.POST, "/api/auth/login", Map.of("correo", correo, "password", "mala"), null)
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.codigo").value("DEMASIADOS_INTENTOS"));
    }

    @Test
    void laContrasenaDebeTenerLetrasYNumeros() throws Exception {
        enviar(HttpMethod.POST, "/api/auth/registro", Map.of("nombre", "Ana", "correo", "it-" + UUID.randomUUID() + "@correo.test",
                "password", "solamenteletras", "aceptaTratamientoDatos", true), null)
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.password").exists());
    }

    @Test
    void unAbogadoRecienRegistradoNoVeCasosHastaSerVerificado() throws Exception {
        String r = cuerpo(enviar(HttpMethod.POST, "/api/auth/registro-abogado", Map.of(
                "nombre", "Abogada Nueva", "correo", "it-" + UUID.randomUUID() + "@correo.test", "password", "Segura2026",
                "tarjetaProfesional", String.valueOf(10_000_000 + (int) (Math.random() * 80_000_000)),
                "especialidad", "Responsabilidad del Estado", "ciudad", "Valledupar", "aniosExperiencia", 3,
                "aceptaTratamientoDatos", true), null).andExpect(status().isCreated()));
        String token = json.readTree(r).path("token").asText();
        enviar(HttpMethod.GET, "/api/abogado/casos-disponibles", null, token)
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("verificación")));
    }

    @Test
    void variosAbogadosSePostulanYElCiudadanoElige() throws Exception {
        // 1. el ciudadano reporta un incidente
        String correoCiudadana = "it-" + UUID.randomUUID() + "@correo.test";
        String ciudadana = registrar(correoCiudadana, "Segura2026");
        int idReporte = json.readTree(cuerpo(enviar(HttpMethod.POST, "/api/reportes/incidentes", Map.of(
                "falla", Map.of("idCategoria", 1, "idZona", 3, "descripcion", "Hueco enorme frente a la tienda de la esquina",
                        "latitud", 10.4805, "longitud", -73.2420, "nivelGravedad", 4),
                "tipoDano", "AMBOS", "descripcionDano", "Caída en moto: rodilla lesionada y moto con daños",
                "fechaHoraHecho", OffsetDateTime.now().minusHours(5).toString(), "huboAtencionMedica", true,
                "testigos", List.of(Map.of("nombre", "Testigo Uno", "contacto", "3000000001"))), ciudadana)
                .andExpect(status().isCreated()))).path("reporte").path("id").asInt();

        // 2. sin validar no se puede buscar abogado
        enviar(HttpMethod.POST, "/api/casos", Map.of("idReporte", idReporte, "autorizaCompartirResumen", true), ciudadana)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("INCIDENTE_NO_VALIDADO"));

        // 3. un operador lo revisa y lo valida (aparece en su bandeja)
        String operador = login("lorena.arzuaga2@correo.test", "Reda2026*");
        assertTrue(cuerpo(enviar(HttpMethod.GET, "/api/revision/bandeja", null, operador)).contains("\"id\":" + idReporte));
        enviar(HttpMethod.PATCH, "/api/reportes/" + idReporte + "/estado", Map.of("nuevoEstado", "EN_REVISION"), operador).andExpect(status().isOk());
        enviar(HttpMethod.PATCH, "/api/reportes/" + idReporte + "/estado", Map.of("nuevoEstado", "VALIDADO", "observacion", "Ok"), operador).andExpect(status().isOk());

        // 4. el ciudadano pide abogado
        int idCaso = json.readTree(cuerpo(enviar(HttpMethod.POST, "/api/casos",
                Map.of("idReporte", idReporte, "autorizaCompartirResumen", true), ciudadana)
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("Remitido")))).path("id").asInt();

        // 5. dos abogados verificados ven el resumen ANÓNIMO y se postulan
        List<String> correosAbogados = jdbc.queryForList("""
                SELECT u.correo FROM abogados a JOIN usuarios u USING (id_usuario) WHERE a.activo ORDER BY a.id_abogado LIMIT 2
                """, String.class);
        String abogadaA = login(correosAbogados.get(0), "Reda2026*");
        String abogadoB = login(correosAbogados.get(1), "Reda2026*");
        String disponibles = cuerpo(enviar(HttpMethod.GET, "/api/abogado/casos-disponibles", null, abogadaA).andExpect(status().isOk()));
        assertTrue(disponibles.contains("\"id\":" + idCaso));
        assertFalse(disponibles.contains(correoCiudadana) || disponibles.contains("rodilla") || disponibles.contains("Testigo Uno"),
                "el resumen para abogados no identifica al ciudadano ni trae datos de salud o testigos");
        enviar(HttpMethod.POST, "/api/abogado/casos/" + idCaso + "/postulacion",
                Map.of("mensaje", "Tengo experiencia en casos de vías en mal estado"), abogadaA).andExpect(status().isCreated());
        enviar(HttpMethod.POST, "/api/abogado/casos/" + idCaso + "/postulacion",
                Map.of("mensaje", "Puedo atenderla esta misma semana sin costo inicial"), abogadoB).andExpect(status().isCreated());

        // 6. el ciudadano compara y ELIGE al segundo
        JsonNode miCaso = null;
        for (JsonNode c : json.readTree(cuerpo(enviar(HttpMethod.GET, "/api/casos/mios", null, ciudadana)))) {
            if (c.path("id").asInt() == idCaso) miCaso = c;
        }
        assertNotNull(miCaso);
        assertEquals(2, miCaso.path("postulaciones").size());
        int idPostulacionB = miCaso.path("postulaciones").get(1).path("id").asInt();
        enviar(HttpMethod.POST, "/api/casos/" + idCaso + "/eleccion", Map.of("idPostulacion", idPostulacionB), ciudadana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Aceptado por abogado"))
                .andExpect(jsonPath("$.postulaciones[0].estado").value("NO_ELEGIDA"))
                .andExpect(jsonPath("$.postulaciones[1].estado").value("ELEGIDA"));

        // 7. el elegido ve el expediente completo; la otra abogada no ve ni el detalle ni las fotos
        String expedientes = cuerpo(enviar(HttpMethod.GET, "/api/abogado/mis-casos", null, abogadoB).andExpect(status().isOk()));
        assertTrue(expedientes.contains(correoCiudadana) && expedientes.contains("Testigo Uno") && expedientes.contains("rodilla"));
        enviar(HttpMethod.GET, "/api/reportes/" + idReporte, null, abogadoB).andExpect(status().isOk());
        enviar(HttpMethod.GET, "/api/reportes/" + idReporte, null, abogadaA).andExpect(status().isForbidden());
        enviar(HttpMethod.GET, "/api/reportes/" + idReporte + "/evidencias", null, abogadaA).andExpect(status().isForbidden());

        // 8. quedó una sola remisión facturable, por la tarifa con lesión
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM remisiones WHERE id_caso = ? AND estado_remision = 'ACEPTADA'",
                Integer.class, idCaso));
        assertEquals(0, new java.math.BigDecimal("200000.00").compareTo(jdbc.queryForObject(
                "SELECT valor_cobrado FROM remisiones WHERE id_caso = ?", java.math.BigDecimal.class, idCaso)));

        // 9. antes de cerrar no se puede calificar; el abogado cierra y el ciudadano lo califica UNA vez
        Map<String, Object> calificacion = Map.of("puntaje", 5, "comentario", "Muy atento y claro con cada paso");
        enviar(HttpMethod.POST, "/api/casos/" + idCaso + "/calificacion", calificacion, ciudadana)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("CASO_NO_CERRADO"));
        enviar(HttpMethod.POST, "/api/abogado/casos/" + idCaso + "/cierre",
                Map.of("observacion", "Conciliación con la entidad responsable"), abogadoB).andExpect(status().isOk());
        long antes = jdbc.queryForObject("SELECT count(*) FROM calificaciones WHERE id_abogado = (SELECT id_abogado FROM remisiones WHERE id_caso = ?)",
                Long.class, idCaso);
        enviar(HttpMethod.POST, "/api/casos/" + idCaso + "/calificacion", calificacion, ciudadana)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.miCalificacion").value(5))
                .andExpect(jsonPath("$.puedeCalificar").value(false))
                .andExpect(jsonPath("$.abogadoElegido.numCalificaciones").value(antes + 1))
                .andExpect(jsonPath("$.abogadoElegido.comentarios[0].comentario").value("Muy atento y claro con cada paso"));
        enviar(HttpMethod.POST, "/api/casos/" + idCaso + "/calificacion", Map.of("puntaje", 1), ciudadana)
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.codigo").value("YA_CALIFICADO"));
        enviar(HttpMethod.POST, "/api/casos/" + idCaso + "/calificacion", Map.of("puntaje", 9), ciudadana)
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ utilidades
    private String registrar(String correo, String password) throws Exception {
        return json.readTree(cuerpo(enviar(HttpMethod.POST, "/api/auth/registro", Map.of("nombre", "Ciudadana de Prueba",
                "correo", correo, "password", password, "aceptaTratamientoDatos", true), null)
                .andExpect(status().isCreated()))).path("token").asText();
    }

    private String login(String correo, String password) throws Exception {
        return json.readTree(cuerpo(enviar(HttpMethod.POST, "/api/auth/login", Map.of("correo", correo, "password", password), null)
                .andExpect(status().isOk()))).path("token").asText();
    }

    private ResultActions enviar(HttpMethod metodo, String ruta, Object cuerpo, String token) throws Exception {
        MockHttpServletRequestBuilder p = MockMvcRequestBuilders.request(metodo, ruta);
        if (cuerpo != null) p.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
        if (token != null) p.header("Authorization", "Bearer " + token);
        return mvc.perform(p);
    }

    private static String cuerpo(ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString();
    }
}
