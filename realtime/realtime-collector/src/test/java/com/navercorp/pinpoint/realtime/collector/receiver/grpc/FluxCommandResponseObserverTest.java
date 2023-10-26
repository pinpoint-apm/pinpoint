/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class FluxCommandResponseObserverTest {

    private static final long SINK_ID = 1234;

    @Mock ServerCallStreamObserver<Empty> connectionObserver;
    @Mock SinkRepository<FluxSink<Integer>> sinkRepository;
    @Test
    public void testSuccessCase() {
        AtomicInteger latest = new AtomicInteger(-1);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicReference<FluxSink<Integer>> sinkRef = new AtomicReference<>();

        Disposable disposable = Flux.<Integer>create(sinkRef::set).subscribe(v -> {
            latest.set(v);
            counter.getAndIncrement();
        });

        doAnswer(inv -> sinkRef.get()).when(sinkRepository).get(eq(SINK_ID));
        doNothing().when(connectionObserver).onNext(any());
        doNothing().when(connectionObserver).onCompleted();

        StreamObserver<Integer> observer = getIntegerStreamObserver();

        observer.onNext(1);

        assertThat(Integer.valueOf(counter.get())).isEqualTo(0);
        assertThat(Integer.valueOf(latest.get())).isEqualTo(-1);
        verify(connectionObserver, times(1)).onNext(any());

        observer.onNext(2);
        observer.onNext(3);
        observer.onNext(4);

        assertThat(Integer.valueOf(counter.get())).isEqualTo(3);
        assertThat(Integer.valueOf(latest.get())).isEqualTo(4);

        observer.onCompleted();

        assertThat(disposable.isDisposed()).isTrue();
        verify(connectionObserver, times(1)).onCompleted();
    }

    @Test
    public void testErrorAfterData() {
        AtomicReference<FluxSink<Integer>> sinkRef = new AtomicReference<>();
        Disposable disposable = Flux.<Integer>create(sinkRef::set).subscribe();

        doAnswer(inv -> sinkRef.get()).when(sinkRepository).get(eq(SINK_ID));
        doNothing().when(connectionObserver).onNext(any());
        doNothing().when(connectionObserver).onCompleted();

        StreamObserver<Integer> observer = getIntegerStreamObserver();

        observer.onNext(1);
        observer.onNext(2);
        observer.onError(new RuntimeException("CANCELLED"));

        verify(connectionObserver, times(1)).onCompleted();
        assertThat(disposable.isDisposed()).isTrue();
    }

    @Test
    public void testErrorAfterHello() {
        AtomicReference<FluxSink<Integer>> sinkRef = new AtomicReference<>();
        Disposable disposable = Flux.<Integer>create(sinkRef::set).subscribe();

        doAnswer(inv -> sinkRef.get()).when(sinkRepository).get(eq(SINK_ID));
        doNothing().when(connectionObserver).onCompleted();

        StreamObserver<Integer> observer = getIntegerStreamObserver();

        observer.onNext(1);
        observer.onError(new RuntimeException("CANCELLED"));

        verify(connectionObserver, times(1)).onCompleted();
        assertThat(disposable.isDisposed()).isTrue();
    }

    @Test
    public void testErrorAtVeryFirst() {
        AtomicReference<FluxSink<Integer>> sinkRef = new AtomicReference<>();
        Disposable disposable = Flux.<Integer>create(sinkRef::set).subscribe();

        doNothing().when(connectionObserver).onCompleted();

        StreamObserver<Integer> observer = getIntegerStreamObserver();

        observer.onError(new RuntimeException("CANCELLED"));

        verify(connectionObserver, times(1)).onCompleted();
        assertThat(disposable.isDisposed()).isFalse();
    }

    @Test
    public void testSinkNotFound() {
        doAnswer(inv -> null).when(sinkRepository).get(eq(SINK_ID));
        doNothing().when(connectionObserver).onError(any());

        StreamObserver<Integer> observer = getIntegerStreamObserver();

        observer.onNext(1);
        verify(connectionObserver, times(1)).onError(any());
    }

    private StreamObserver<Integer> getIntegerStreamObserver() {
        return new FluxCommandResponseObserver<>(connectionObserver, sinkRepository) {
            @Override
            protected long extractSinkId(Integer response) {
                return SINK_ID;
            }

            @Override
            protected int extractSequence(Integer response) {
                return response;
            }
        };
    }

}
