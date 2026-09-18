package ru.yandex.practicum.order.config;

import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> circuitBreakerCustomizer() {
        return factory -> {
            MdcScheduledThreadPoolExecutor executor =
                    new MdcScheduledThreadPoolExecutor(5);

            factory.configureExecutorService(executor);
        };
    }
}
