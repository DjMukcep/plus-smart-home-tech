import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.List;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@TestConfiguration
@EnableWebFlux
public class GatewaySecurityTestConfig {

    @Bean
    RouterFunction<ServerResponse> testRoutes() {
        return route(GET("/api/products"),
                request -> ServerResponse.ok().build())
                .andRoute(GET("/api/categories"),
                        request -> ServerResponse.ok().build())
                .andRoute(GET("/api/inventory"),
                        request -> ServerResponse.ok().build())
                .andRoute(GET("/api/orders"),
                        request -> ServerResponse.ok().build())
                .andRoute(GET("/api/orders/1"),
                        request -> ServerResponse.ok().build())
                .andRoute(POST("/api/orders"),
                        request -> ServerResponse.ok().build())
                .andRoute(GET("/api/orders/1"),
                        request -> ServerResponse.ok().build())
                .andRoute(PATCH("/api/products/10"),
                        request -> ServerResponse.ok().build())
                .andRoute(POST("/api/products"),
                        request -> ServerResponse.ok().build())
                .andRoute(GET("/api/products/1"),
                        request -> ServerResponse.ok().build());
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:8443"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setExposedHeaders(List.of("Location"));

        configuration.setAllowCredentials(true);

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}