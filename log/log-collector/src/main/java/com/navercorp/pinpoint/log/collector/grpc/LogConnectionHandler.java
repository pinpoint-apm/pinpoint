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
package com.navercorp.pinpoint.log.collector.grpc;

import com.navercorp.pinpoint.grpc.log.PLogDemand;
import com.navercorp.pinpoint.grpc.log.PLogPile;
import com.navercorp.pinpoint.grpc.log.PLogRecord;
import com.navercorp.pinpoint.log.dto.LogDemand;
import com.navercorp.pinpoint.log.vo.FileKey;
import com.navercorp.pinpoint.log.vo.Log;
import com.navercorp.pinpoint.log.vo.LogPile;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.Disposable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
class LogConnectionHandler implements StreamObserver<PLogPile>, Consumer<LogDemand> {

    private final Logger logger = LogManager.getLogger(LogConnectionHandler.class);

    private final Consumer<LogPile> pileConsumer;
    private final Disposable disposable;
    private final FileKey fileKey;
    private final ServerCallStreamObserver<PLogDemand> responseObserver;

    LogConnectionHandler(
            Consumer<LogPile> pileConsumer,
            Disposable disposable,
            FileKey fileKey,
            ServerCallStreamObserver<PLogDemand> responseObserver
    ) {
        this.pileConsumer = Objects.requireNonNull(pileConsumer, "pileConsumer");
        this.disposable = Objects.requireNonNull(disposable, "disposable");
        this.fileKey = Objects.requireNonNull(fileKey, "fileKey");
        this.responseObserver = Objects.requireNonNull(responseObserver, "responseObserver");
    }

    @Override
    public void onNext(PLogPile pLogPile) {
        if (logger.isTraceEnabled()) {
            for (final PLogRecord record: pLogPile.getRecordsList()) {
                logger.trace("log[{}-{}]: {}", pLogPile.getSeq(), record.getSeq(), record.getMessage());
            }
        }
        final List<Log> logs = pLogPile.getRecordsList().stream()
                .map(el -> new Log(el.getSeq(), el.getTimestamp(), el.getMessage()))
                .toList();
        this.pileConsumer.accept(new LogPile(pLogPile.getSeq(), logs));
    }

    @Override
    public void onError(Throwable throwable) {
        logger.error("Error on log {}: {}", this.fileKey, throwable.getMessage());
        this.disposable.dispose();
    }

    @Override
    public void onCompleted() {
        logger.info("Completed on {}", this.fileKey);
        this.disposable.dispose();
        this.responseObserver.onCompleted();
    }

    @Override
    public void accept(LogDemand logDemand) {
        try {
            this.responseObserver.onNext(PLogDemand.newBuilder()
                    .setDurationMillis(logDemand.getDurationMillis())
                    .build());
        } catch (Exception e) {
            this.responseObserver.onError(e);
            this.disposable.dispose();
            logger.error("Failed to send demand for {}", this.fileKey, e);
        }
    }

}
