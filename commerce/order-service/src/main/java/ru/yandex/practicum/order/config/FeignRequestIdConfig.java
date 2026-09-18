package ru.yandex.practicum.order.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignRequestIdConfig {

    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> {
            String requestId = MDC.get("requestId");

            if (requestId != null) {
                template.header("X-Request-ID", requestId);
            }
        };
    }
}
