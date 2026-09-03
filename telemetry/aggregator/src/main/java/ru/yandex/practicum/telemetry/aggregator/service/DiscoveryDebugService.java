package ru.yandex.practicum.telemetry.aggregator.service;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DiscoveryDebugService {
    private final DiscoveryClient discoveryClient;

    public DiscoveryDebugService(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    public List<ServiceInstanceInfo> findInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId).stream()
                .map(this::toInfo)
                .toList();
    }

    private ServiceInstanceInfo toInfo(ServiceInstance instance) {
        return new ServiceInstanceInfo(
                instance.getServiceId(),
                instance.getHost(),
                instance.getPort(),
                instance.getUri().toString(),
                instance.getMetadata()
        );
    }

    public record ServiceInstanceInfo(
            String serviceId, String host, int port, String uri, Map<String, String> metadata) {
    }
}
