package ru.yandex.practicum.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;


@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain apiSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange

                        .pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**"
                        )
                        .permitAll()

                        .pathMatchers(HttpMethod.GET,
                                "/api/products/**", "/api/categories/**", "/api/inventory/**"
                        ).permitAll()

                        .pathMatchers(HttpMethod.POST, "/api/orders/**")
                        .hasRole("USER")

                        .pathMatchers(HttpMethod.GET, "/api/orders/by-email")
                        .hasRole("USER")

                        .pathMatchers(HttpMethod.GET, "/api/orders/{id}")
                        .hasRole("USER")

                        .pathMatchers(HttpMethod.GET, "/api/orders")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/products/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH, "/api/products/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/categories/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.POST, "/api/inventory/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PUT, "/api/inventory/**")
                        .hasRole("ADMIN")

                        .anyExchange().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ReactiveUserDetailsService userDetailsService(
            SecurityProperties securityProperties,
            PasswordEncoder passwordEncoder) {

        List<UserDetails> users = securityProperties.getUsers().stream()
                .map(user -> User.builder()
                        .username(user.getUsername())
                        .password(passwordEncoder.encode(user.getPassword()))
                        .roles(user.getRoles().toArray(String[]::new))
                        .build())
                .toList();

        return new MapReactiveUserDetailsService(users);
    }
}
