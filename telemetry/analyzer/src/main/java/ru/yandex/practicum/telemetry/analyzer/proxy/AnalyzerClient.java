package ru.yandex.practicum.telemetry.analyzer.service;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.telemetry.analyzer.repository.action.Action;

import static ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;

@Service
@Slf4j
public class AnalyzerClient {
    private final HubRouterControllerBlockingStub hubRouterClient;

    public AnalyzerClient(@GrpcClient("hub-router") HubRouterControllerBlockingStub blockingStub) {
        this.hubRouterClient = blockingStub;
    }

    public void sendActionToHub(String hubId, String scenarioName, String sensorId, Action action) {
        DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                .setSensorId(sensorId)
                .setType(ActionTypeProto.valueOf(action.getType().name()))
                .setValue(action.getValue() == null ? 0 : action.getValue())
                .build();

        DeviceActionRequest request = DeviceActionRequest.newBuilder()
                .setHubId(hubId)
                .setScenarioName(scenarioName)
                .setAction(actionProto)
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .build())
                .build();
        try {
            hubRouterClient.handleDeviceAction(request);
            log.info("Команда успешно отправлена на хаб {} для сценария {}", hubId, scenarioName);
        } catch (StatusRuntimeException e) {
            log.error("Ошибка gRPC при отправке команды на хаб {}: {} (Код статуса: {})",
                    hubId, e.getMessage(), e.getStatus().getCode());
        }

    }

}
