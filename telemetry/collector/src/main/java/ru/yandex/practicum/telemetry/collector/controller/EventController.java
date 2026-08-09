package ru.yandex.practicum.telemetry.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;

import io.grpc.stub.StreamObserver;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.config.KafkaTopicsProperties;
import ru.yandex.practicum.telemetry.collector.service.strategy.hub.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.strategy.sensor.SensorEventHandler;

import java.util.Map;
import java.util.function.BiConsumer;

import static ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase;

@RequiredArgsConstructor
@GrpcService
public class EventController extends CollectorControllerImplBase {

    private final Map<String, SensorEventHandler> sensorEventHandlers;
    private final Map<String, HubEventHandler> hubEventHandlers;
    private final KafkaTopicsProperties topics;

    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        processEvent(
                request.getPayloadCase().name(),
                topics.sensors(),
                responseObserver,
                (key, topic) -> sensorEventHandlers.get(key).handle(request, topic)
        );
    }

    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        processEvent(
                request.getPayloadCase().name(),
                topics.hubs(),
                responseObserver,
                (key, topic) -> hubEventHandlers.get(key).handle(request, topic)
        );
    }

    private void processEvent(
            String eventKey,
            String topic,
            StreamObserver<Empty> responseObserver,
            BiConsumer<String, String> handlerExecutor) {
        try {
            System.out.println("Processing event " + eventKey);
            handlerExecutor.accept(eventKey, topic);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (NullPointerException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Не могу найти обработчик для события " + eventKey)
                    .withCause(e)
                    .asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getLocalizedMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}
