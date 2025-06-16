package com.pinpoint.test.plugin;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Service
public class AsyncStorageService {
    private final Map<String, Sinks.One<Object>> responseMap = new ConcurrentHashMap<>();

    public <T> Mono<T> submitAsync(
            String userKey,
            boolean debugMode,
            long timeoutSec,
            Mono<T> exec
    ) {
        String key = userKey + "_" + UUID.randomUUID();
        Sinks.One<Object> sink = Sinks.one();
        responseMap.put(key, sink);

        System.out.println("AsyncStorageService.submitAsync() thread=" + Thread.currentThread().getName());
        return Mono.defer(() -> {
            System.out.println("Mono.defer() thread=" + Thread.currentThread().getName());
            exec.subscribeOn(Schedulers.boundedElastic())
                    .subscribe(System.out::println);

            return sink.asMono()
                    .timeout(Duration.ofSeconds(timeoutSec))
                    .map(value -> {
                        @SuppressWarnings("unchecked")
                        T castedValue = (T) value;
                        return castedValue;
                    })
                    .doOnError(TimeoutException.class, e -> responseMap.remove(key));
        });
    }
}
