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

package com.navercorp.pinpoint.collector.sampling.tail;

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.io.ServerHeader;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TailSampler {

    private final Logger logger = LogManager.getLogger(getClass());

    private final TraceService[] alwaysServices;
    private final TraceService[] sampledServices;
    private final TailSamplingRepository repository;
    private final TailSamplingProperties properties;
    private final BufferedSpanCodec codec;
    private final GrpcSpanFactory spanFactory;

    private final Counter keptCounter;
    private final Counter droppedCounter;
    private final Counter bufferedCounter;
    private final Counter redisErrorCounter;
    private final Counter flushTimeoutCounter;
    private final Counter errorKeptCounter;

    public TailSampler(TraceService[] traceServices,
                       TailSamplingRepository repository,
                       TailSamplingProperties properties,
                       BufferedSpanCodec codec,
                       GrpcSpanFactory spanFactory,
                       MeterRegistry meterRegistry) {
        Objects.requireNonNull(traceServices, "traceServices");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.spanFactory = Objects.requireNonNull(spanFactory, "spanFactory");

        List<TraceService> always = new ArrayList<>();
        List<TraceService> sampled = new ArrayList<>();
        for (TraceService ts : traceServices) {
            if (ts instanceof StatisticsTraceService) {
                always.add(ts);
            } else {
                sampled.add(ts);
            }
        }
        this.alwaysServices = always.toArray(new TraceService[0]);
        this.sampledServices = sampled.toArray(new TraceService[0]);

        this.keptCounter = meterRegistry.counter("collector.tail.sampling", "result", "kept");
        this.droppedCounter = meterRegistry.counter("collector.tail.sampling", "result", "dropped");
        this.bufferedCounter = meterRegistry.counter("collector.tail.sampling", "result", "buffered");
        this.redisErrorCounter = meterRegistry.counter("collector.tail.sampling", "result", "redis-error");
        this.flushTimeoutCounter = meterRegistry.counter("collector.tail.sampling", "result", "flush-timeout");
        this.errorKeptCounter = meterRegistry.counter("collector.tail.sampling", "result", "kept-error");
    }

    public void acceptSpan(SpanBo spanBo, byte[] protoBytes) {
        for (TraceService ts : alwaysServices) {
            ts.insertSpan(spanBo);
        }

        final String txid = spanBo.getTransactionId().toString();
        try {
            boolean error = properties.isKeepOnError() && TraceErrors.hasError(spanBo);
            byte[] envelope = codec.encode(new BufferedSpan(BufferedSpan.Type.SPAN,
                    spanBo.getAgentId(), spanBo.getAgentName(), spanBo.getApplicationName(),
                    spanBo.getAgentStartTime(), spanBo.getCollectorAcceptTime(), protoBytes));
            String decision = repository.accept(txid, envelope, System.currentTimeMillis(), error);

            if ("keep".equals(decision)) {
                keptCounter.increment();
                insertSampledSpanLive(spanBo);
            } else if ("drop".equals(decision)) {
                droppedCounter.increment();
            } else {
                bufferedCounter.increment();
                if (spanBo.isRoot()) {
                    decideRoot(txid, spanBo.getElapsed(), error);
                }
            }
        } catch (Exception e) {
            redisErrorCounter.increment();
            logger.warn("tail sampling redis error, fail-open write-through. txid={}", txid, e);
            insertSampledSpanLive(spanBo);
        }
    }

    public void acceptSpanChunk(SpanChunkBo spanChunkBo, byte[] protoBytes) {
        for (TraceService ts : alwaysServices) {
            ts.insertSpanChunk(spanChunkBo);
        }

        final String txid = spanChunkBo.getTransactionId().toString();
        try {
            boolean error = properties.isKeepOnError() && TraceErrors.hasError(spanChunkBo);
            byte[] envelope = codec.encode(new BufferedSpan(BufferedSpan.Type.SPAN_CHUNK,
                    spanChunkBo.getAgentId(), null, spanChunkBo.getApplicationName(),
                    spanChunkBo.getAgentStartTime(), spanChunkBo.getCollectorAcceptTime(), protoBytes));
            String decision = repository.accept(txid, envelope, System.currentTimeMillis(), error);

            if ("keep".equals(decision)) {
                keptCounter.increment();
                insertSampledSpanChunkLive(spanChunkBo);
            } else if ("drop".equals(decision)) {
                droppedCounter.increment();
            } else {
                bufferedCounter.increment();
            }
            // chunk is never a decision trigger (not root); buffered -> wait
        } catch (Exception e) {
            redisErrorCounter.increment();
            logger.warn("tail sampling redis error (chunk), fail-open. txid={}", txid, e);
            insertSampledSpanChunkLive(spanChunkBo);
        }
    }

    /**
     * Root span arrived. If the band keeps, or the trace already errored, decide now.
     * Otherwise (band would drop and no error yet) defer the decision for the grace window so a
     * late-arriving errored span can still flip it to keep.
     */
    private void decideRoot(String txid, int elapsedMillis, boolean selfError) {
        final boolean bandKeep = TailDecisions.keep(txid, properties.rateFor(elapsedMillis));
        // defer only when keep-on-error is on and this band-drop trace has no error yet
        final boolean deferForError = properties.isKeepOnError()
                && !bandKeep
                && !selfError
                && !repository.isErrorFlagged(txid);
        if (deferForError) {
            repository.defer(txid, System.currentTimeMillis());
            return;
        }
        decideAndFlush(txid, bandKeep);
    }

    private void decideAndFlush(String txid, boolean proposed) {
        // The script may upgrade a proposed drop to keep when the trace's error flag is set,
        // so we act on the returned list, not on `proposed`.
        List<byte[]> won = repository.decide(txid, proposed);
        if (won == null) {
            return; // another node already decided
        }
        if (won.isEmpty()) {
            droppedCounter.increment();
            return;
        }
        keptCounter.increment();
        if (!proposed) {
            errorKeptCounter.increment(); // band would have dropped; kept because the trace errored
        }
        replay(won);
    }

    /** Finalizes a deferred trace once its grace window elapsed (band drop, unless an error flipped it). */
    public void finalizeDeferred(String txid) {
        List<byte[]> won = repository.decide(txid, false);
        repository.removeDeferred(txid);
        if (won == null) {
            return;
        }
        if (won.isEmpty()) {
            droppedCounter.increment();
            return;
        }
        keptCounter.increment();
        errorKeptCounter.increment();
        replay(won);
    }

    /** Replay a sweeper-flushed (orphaned, default-keep) trace: counts it as kept + flush-timeout. */
    public void replaySwept(List<byte[]> encodedSpans) {
        flushTimeoutCounter.increment();
        keptCounter.increment();
        replay(encodedSpans);
    }

    /** Rebuild buffered envelopes and write them to the sampled services. */
    void replay(List<byte[]> encodedSpans) {
        for (byte[] encoded : encodedSpans) {
            BufferedSpan buffered = codec.decode(encoded);
            ServerHeader header = new ReconstructedServerHeader(
                    buffered.agentId(), buffered.agentName(), buffered.applicationName(), buffered.agentStartTime());
            try {
                if (buffered.type() == BufferedSpan.Type.SPAN) {
                    SpanBo bo = spanFactory.buildSpanBo(PSpan.parseFrom(buffered.protoBytes()), header, buffered.requestTime());
                    insertSampledSpanLive(bo);
                } else {
                    SpanChunkBo bo = spanFactory.buildSpanChunkBo(PSpanChunk.parseFrom(buffered.protoBytes()), header, buffered.requestTime());
                    insertSampledSpanChunkLive(bo);
                }
            } catch (Exception e) {
                logger.warn("failed to replay buffered span", e);
            }
        }
    }

    private void insertSampledSpanLive(SpanBo spanBo) {
        for (TraceService ts : sampledServices) {
            ts.insertSpan(spanBo);
        }
    }

    private void insertSampledSpanChunkLive(SpanChunkBo spanChunkBo) {
        for (TraceService ts : sampledServices) {
            ts.insertSpanChunk(spanChunkBo);
        }
    }
}
