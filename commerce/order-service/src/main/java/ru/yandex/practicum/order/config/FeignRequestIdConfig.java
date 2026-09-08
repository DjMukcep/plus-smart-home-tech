package ru.yandex.practicum.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Configuration
public class FeignRequestIdConfig {

    @Bean
    public RequestInterceptor requestIdInterceptor() {
        return template -> {
            String requestId = getCurrentRequestId();
            template.header("X-Request-ID", requestId);
        };
    }

    private String getCurrentRequestId() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            return UUID.randomUUID().toString();
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String requestId = request.getHeader("X-Request-ID");

        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return requestId;
    }
}
