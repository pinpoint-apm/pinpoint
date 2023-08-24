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

import com.navercorp.pinpoint.grpc.log.LogGrpc;
import com.navercorp.pinpoint.grpc.log.PLogDemand;
import com.navercorp.pinpoint.grpc.log.PLogPile;
import com.navercorp.pinpoint.log.collector.grpc.context.LogAgentHeader;
import com.navercorp.pinpoint.log.collector.service.LogProviderService;
import com.navercorp.pinpoint.log.dto.LogDemand;
import com.navercorp.pinpoint.log.vo.FileKey;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.Disposable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author youngjin.kim2
 */
class LogGrpcService extends LogGrpc.LogImplBase {

    private final Logger logger = LogManager.getLogger(LogGrpcService.class);

    private final LogProviderService service;

    LogGrpcService(LogProviderService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public StreamObserver<PLogPile> connect(StreamObserver<PLogDemand> responseObserver0) {
        ServerCallStreamObserver<PLogDemand> responseObserver =
                (ServerCallStreamObserver<PLogDemand>) responseObserver0;
        try {
            FileKey fileKey = getFileKey();
            AtomicReference<Disposable> disposableRef = new AtomicReference<>();
            Disposable disposable = this.service.getDemands(fileKey)
                    .subscribe(getDemandHandler(responseObserver, fileKey, disposableRef));
            disposableRef.set(disposable);
            return new LogConnectionHandler(
                    pile -> this.service.provide(fileKey, pile),
                    disposable,
                    fileKey,
                    responseObserver
            );
        } catch (Exception e) {
            responseObserver.onError(e);
            return EmptyStreamObserver.create();
        }
    }

    private Consumer<LogDemand> getDemandHandler(
            ServerCallStreamObserver<PLogDemand> responseObserver,
            FileKey fileKey,
            AtomicReference<Disposable> disposableRef
    ) {
        return demand -> {
            try {
                responseObserver.onNext(PLogDemand.newBuilder()
                        .setDurationMillis(demand.getDurationMillis())
                        .build());
            } catch (Exception e) {
                responseObserver.onError(e);
                disposableRef.get().dispose();
                logger.error("Failed to send demand for {}", fileKey, e);
            }
        };
    }

    private FileKey getFileKey() {
        final LogAgentHeader header = LogAgentHeader.LOG_AGENT_HEADER_KEY.get();
        return header.getFileKey();
    }

}
