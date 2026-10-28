package co.edu.unicesar.reda.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger (http://localhost:8080/swagger-ui.html) con el botón "Authorize" para el token. */
@Configuration
@EnableConfigurationProperties(PropiedadesReda.class)
@OpenAPIDefinition(info = @Info(title = "REDA API", version = "0.1.0",
        description = "Documentación probatoria de fallas en infraestructura pública. "
                + "REDA no presta asesoría jurídica ni garantiza el resultado de un caso."))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
