package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.LimiteGeocodificacionException;
import co.edu.unicesar.reda.exception.ServicioGeocodificacionNoDisponibleException;
import co.edu.unicesar.reda.respuesta.LugarGeocodificadoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GeocodificacionServiceTest {

    private static final String ENDPOINT = "https://nominatim.test/search";
    private static final String USER_AGENT = "REDA-test";

    @Test
    void buscaDireccionesConAtribucionDeAplicacionYFiltraCoordenadasInvalidas() {
        RestClient.Builder builder = RestClient.builder().defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT);
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(containsString("/search?")))
                .andExpect(queryParam("q", "Calle%2016%20%23%209-01"))
                .andExpect(queryParam("countrycodes", "co"))
                .andExpect(queryParam("limit", "5"))
                .andExpect(header(HttpHeaders.USER_AGENT, USER_AGENT))
                .andRespond(withSuccess("""
                        [
                          {"display_name":"Calle 16, Valledupar, Colombia","lat":"10.47","lon":"-73.25"},
                          {"display_name":"Resultado inválido","lat":"190","lon":"-73.25"}
                        ]
                        """, MediaType.APPLICATION_JSON));
        GeocodificacionService servicio = new GeocodificacionService(builder.build(), ENDPOINT);

        List<LugarGeocodificadoResponse> lugares = servicio.buscar(" Calle 16 # 9-01 ");

        assertEquals(List.of(new LugarGeocodificadoResponse("Calle 16, Valledupar, Colombia", 10.47, -73.25)),
                lugares);
        servidor.verify();
    }

    @Test
    void reutilizaLaCacheYLimitaLasConsultasExternasAUnaPorSegundo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(containsString("/search?")))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));
        GeocodificacionService servicio = new GeocodificacionService(builder.build(), ENDPOINT);

        assertEquals(List.of(), servicio.buscar("Valledupar"));
        assertEquals(List.of(), servicio.buscar(" valledupar "));
        assertThrows(LimiteGeocodificacionException.class, () -> servicio.buscar("Aguachica"));

        servidor.verify();
    }

    @Test
    void informaCuandoElProveedorNoEstaDisponible() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        servidor.expect(requestTo(containsString("/search?"))).andRespond(withServerError());
        GeocodificacionService servicio = new GeocodificacionService(builder.build(), ENDPOINT);

        assertThrows(ServicioGeocodificacionNoDisponibleException.class,
                () -> servicio.buscar("Valledupar"));

        servidor.verify();
    }
}
