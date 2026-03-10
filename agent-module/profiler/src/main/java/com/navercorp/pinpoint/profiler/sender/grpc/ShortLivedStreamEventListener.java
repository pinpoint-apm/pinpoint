/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Future;

/**
 * StreamEventListener for short-lived streams.
 * On normal batch completion, triggers an immediate restart via {@code onBatchComplete}.
 * On error, uses exponential backoff reconnect via the {@code reconnector}.
 *
 * @author jaehong.kim
 */
public class ShortLivedStreamEventListener<ReqT> implements StreamEventListener<ReqT> {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Reconnector reconnector;
    private final StreamJob<ReqT> streamJob;
    private final Runnable onBatchComplete;
    private volatile Future<?> handle;

    public ShortLivedStreamEventListener(Reconnector reconnector,
                                         StreamJob<ReqT> streamJob,
                                         Runnable onBatchComplete) {
        this.reconnector = Objects.requireNonNull(reconnector, "reconnector");
        this.streamJob = Objects.requireNonNull(streamJob, "streamJob");
        this.onBatchComplete = Objects.requireNonNull(onBatchComplete, "onBatchComplete");
    }

    @Override
    public void start(final ClientCallStateStreamObserver<ReqT> requestStream) {
        this.handle = streamJob.start(requestStream);
        reconnector.reset();
    }

    @Override
    public void onError(Throwable t) {
        cancel();
        // Use exponential backoff reconnect on error
        reconnector.reconnect();
    }

    @Override
    public void onCompleted() {
        cancel();
        // Trigger immediate restart on normal batch completion
        onBatchComplete.run();
    }

    private void cancel() {
        final Future<?> handle = this.handle;
        if (handle != null) {
            handle.cancel(true);
        }
    }

    @Override
    public String toString() {
        return "ShortLivedStreamEventListener{" +
                streamJob +
                '}';
    }
}
