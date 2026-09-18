package ru.yandex.practicum.order.config;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;

public class MdcScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

    public MdcScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    private Runnable wrap(Runnable task) {
        Map<String, String> context = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();

            try {
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }

                task.run();
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }

    private <V> Callable<V> wrap(Callable<V> task) {
        Map<String, String> context = MDC.getCopyOfContextMap();

        return () -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();

            try {
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }

                return task.call();
            } finally {
                if (previousContext == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousContext);
                }
            }
        };
    }

    @Override
    public void execute(Runnable command) {
        super.execute(wrap(command));
    }

    @Override
    public ScheduledFuture<?> schedule(
            Runnable command,
            long delay,
            TimeUnit unit
    ) {
        return super.schedule(wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(
            Callable<V> callable,
            long delay,
            TimeUnit unit
    ) {
        return super.schedule(wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(
            Runnable command,
            long initialDelay,
            long period,
            TimeUnit unit
    ) {
        return super.scheduleAtFixedRate(
                wrap(command),
                initialDelay,
                period,
                unit
        );
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(
            Runnable command,
            long initialDelay,
            long delay,
            TimeUnit unit
    ) {
        return super.scheduleWithFixedDelay(
                wrap(command),
                initialDelay,
                delay,
                unit
        );
    }
}
