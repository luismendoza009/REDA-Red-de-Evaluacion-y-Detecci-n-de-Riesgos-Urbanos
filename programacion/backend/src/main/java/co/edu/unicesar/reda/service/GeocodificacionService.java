package co.edu.unicesar.reda.service;

import co.edu.unicesar.reda.exception.LimiteGeocodificacionException;
import co.edu.unicesar.reda.exception.ServicioGeocodificacionNoDisponibleException;
import co.edu.unicesar.reda.respuesta.LugarGeocodificadoResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class GeocodificacionService {

    private static final Logger log = LoggerFactory.getLogger(GeocodificacionService.class);
    private static final long INTERVALO_MINIMO_NANOS = Duration.ofSeconds(1).toNanos();
    private static final Duration DURACION_CACHE = Duration.ofHours(24);
    private static final int MAXIMO_RESULTADOS = 5;

    private final RestClient cliente;
    private final String endpoint;
    private final Map<String, EntradaCache> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, EntradaCache> eldest) {
            return size() > 256;
        }
    };
    private long ultimaConsultaNanos = Long.MIN_VALUE;

    @Autowired
    public GeocodificacionService(RestClient.Builder builder,
                                  @Value("${reda.geocodificacion.endpoint:https://nominatim.openstreetmap.org/search}")
                                  String endpoint) {
        SimpleClientHttpRequestFactory peticiones = new SimpleClientHttpRequestFactory();
        peticiones.setConnectTimeout(Duration.ofSeconds(3));
        peticiones.setReadTimeout(Duration.ofSeconds(5));
        this.cliente = builder.clone()
                .requestFactory(peticiones)
                .defaultHeader(HttpHeaders.USER_AGENT,
                        "REDA/0.1.0 (+https://github.com/luismendoza009/REDA-Red-de-Evaluacion-y-Detecci-n-de-Riesgos-Urbanos)")
                .build();
        this.endpoint = endpoint;
    }

    GeocodificacionService(RestClient cliente, String endpoint) {
        this.cliente = cliente;
        this.endpoint = endpoint;
    }

    public List<LugarGeocodificadoResponse> buscar(String consulta) {
        String termino = consulta.trim().replaceAll("\\s+", " ");
        String claveCache = termino.toLowerCase(Locale.ROOT);
        List<LugarGeocodificadoResponse> resultadoCacheado = buscarCache(claveCache);
        if (resultadoCacheado != null) return resultadoCacheado;

        respetarLimite();
        URI uri = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("q", termino)
                .queryParam("format", "jsonv2")
                .queryParam("countrycodes", "co")
                .queryParam("limit", MAXIMO_RESULTADOS)
                .build()
                .encode()
                .toUri();

        try {
            JsonNode respuesta = cliente.get().uri(uri).retrieve().body(JsonNode.class);
            List<LugarGeocodificadoResponse> lugares = convertir(respuesta);
            guardarCache(claveCache, lugares);
            return lugares;
        } catch (RestClientException e) {
            log.warn("No se pudo consultar el proveedor de geocodificación ({})", e.getClass().getSimpleName());
            throw new ServicioGeocodificacionNoDisponibleException();
        }
    }

    private synchronized void respetarLimite() {
        long ahora = System.nanoTime();
        if (ultimaConsultaNanos != Long.MIN_VALUE && ahora - ultimaConsultaNanos < INTERVALO_MINIMO_NANOS) {
            throw new LimiteGeocodificacionException();
        }
        ultimaConsultaNanos = ahora;
    }

    private List<LugarGeocodificadoResponse> buscarCache(String clave) {
        Instant ahora = Instant.now();
        synchronized (cache) {
            cache.entrySet().removeIf(entrada ->
                    !ahora.isBefore(entrada.getValue().guardadoEn().plus(DURACION_CACHE)));
            EntradaCache entrada = cache.get(clave);
            return entrada == null ? null : entrada.lugares();
        }
    }

    private void guardarCache(String clave, List<LugarGeocodificadoResponse> lugares) {
        synchronized (cache) {
            cache.put(clave, new EntradaCache(Instant.now(), List.copyOf(lugares)));
        }
    }

    private static List<LugarGeocodificadoResponse> convertir(JsonNode respuesta) {
        if (respuesta == null || !respuesta.isArray()) {
            throw new ServicioGeocodificacionNoDisponibleException();
        }
        List<LugarGeocodificadoResponse> lugares = new ArrayList<>();
        for (JsonNode resultado : respuesta) {
            String nombre = resultado.path("display_name").asText("").trim();
            double latitud = resultado.path("lat").asDouble(Double.NaN);
            double longitud = resultado.path("lon").asDouble(Double.NaN);
            if (nombre.isEmpty() || !Double.isFinite(latitud) || !Double.isFinite(longitud)
                    || latitud < -90 || latitud > 90 || longitud < -180 || longitud > 180) {
                continue;
            }
            lugares.add(new LugarGeocodificadoResponse(nombre, latitud, longitud));
        }
        return List.copyOf(lugares);
    }

    private record EntradaCache(Instant guardadoEn, List<LugarGeocodificadoResponse> lugares) {
    }
}
