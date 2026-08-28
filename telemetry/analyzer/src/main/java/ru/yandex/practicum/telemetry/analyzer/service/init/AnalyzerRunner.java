package ru.yandex.practicum.telemetry.analyzer.service.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventProcessor;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotProcessor;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final HubEventProcessor hubEventProcessor;
    final SnapshotProcessor snapshotProcessor;

    @Override
    public void run(String... args) {
        Thread hubEventProcessorThread = new Thread(hubEventProcessor);
        hubEventProcessorThread.setName("Hub event processor");
        hubEventProcessorThread.start();
        snapshotProcessor.start();
    }
}
