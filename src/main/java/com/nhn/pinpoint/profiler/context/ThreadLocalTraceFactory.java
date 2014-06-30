package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.Trace;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.context.TraceId;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.context.storage.Storage;
import com.nhn.pinpoint.profiler.context.storage.StorageFactory;
import com.nhn.pinpoint.profiler.monitor.metric.MetricRegistry;
import com.nhn.pinpoint.profiler.util.NamedThreadLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author emeroad
 */
public class ThreadLocalTraceFactory implements TraceFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ThreadLocal<Trace> threadLocal = new NamedThreadLocal<Trace>("Trace");

    private final TraceContext traceContext;
    private final MetricRegistry metricRegistry;

    private final StorageFactory storageFactory;
    private final Sampler sampler;

    // internal stacktrace 추적때 필요한 unique 아이디, activethreadcount의  slow 타임 계산의 위해서도 필요할듯 함.
    // 일단 소스를 좀더 단순화 하기 위해서 옮김.
    private final AtomicLong transactionId = new AtomicLong(0);

    public ThreadLocalTraceFactory(TraceContext traceContext, MetricRegistry metricRegistry, StorageFactory storageFactory, Sampler sampler) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (metricRegistry == null) {
            throw new NullPointerException("metricRegistry must not be null");
        }
        if (storageFactory == null) {
            throw new NullPointerException("storageFactory must not be null");
        }
        if (sampler == null) {
            throw new NullPointerException("sampler must not be null");
        }
        this.traceContext = traceContext;
        this.metricRegistry = metricRegistry;
        this.storageFactory = storageFactory;
        this.sampler = sampler;
    }


    /**
     * sampling 여부까지 체크하여 유효성을 검증한 후 Trace를 리턴한다.
     * @return
     */
    @Override
    public Trace currentTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        if (trace.canSampled()) {
            return trace;
        }
        return null;
    }

    @Override
    public Trace currentRpcTraceObject() {
        final Trace trace = threadLocal.get();
        if (trace == null) {
            return null;
        }
        return trace;
    }

    /**
     * 유효성을 검증하지 않고 Trace를 리턴한다.
     * @return
     */
    @Override
    public Trace currentRawTraceObject() {
        return threadLocal.get();
    }

    @Override
    public Trace disableSampling() {
        checkBeforeTraceObject();
        final Trace metricTrace = createMetricTrace();
        threadLocal.set(metricTrace);
        return metricTrace;
    }

    // remote 에서 샘플링 대상으로 선정된 경우.
    @Override
    public Trace continueTraceObject(final TraceId traceID) {
        checkBeforeTraceObject();

        // datasender연결 부분 수정 필요.
        final DefaultTrace trace = new DefaultTrace(traceContext, traceID);
        final Storage storage = storageFactory.createStorage();
        trace.setStorage(storage);
        // remote에 의해 trace가 continue될때는  sampling flag를 좀더 상위에서 하므로 무조껀 true여야함.
        // TODO remote에서 sampling flag로 마크가되는 대상으로 왔을 경우도 추가로 샘플링 칠수 있어야 할것으로 보임.
        trace.setSampling(true);

        threadLocal.set(trace);
        return trace;
    }

    private void checkBeforeTraceObject() {
        final Trace old = this.threadLocal.get();
        if (old != null) {
            final PinpointException exception = new PinpointException("already Trace Object exist.");
            if (logger.isWarnEnabled()) {
                logger.warn("beforeTrace:{}", old, exception);
            }
            throw exception;
        }
    }

    @Override
    public Trace newTraceObject() {
        checkBeforeTraceObject();
        // datasender연결 부분 수정 필요.
        final boolean sampling = sampler.isSampling();
        if (sampling) {
            final Storage storage = storageFactory.createStorage();
            final DefaultTrace trace = new DefaultTrace(traceContext, nextTransactionId());
            trace.setStorage(storage);
            trace.setSampling(sampling);
            threadLocal.set(trace);
            return trace;
        } else {
            final Trace metricTrace = createMetricTrace();
            threadLocal.set(metricTrace);
            return metricTrace;
        }
    }

    private MetricTrace createMetricTrace() {
        return new MetricTrace(traceContext, nextTransactionId());
    }

    private long nextTransactionId() {
        return this.transactionId.getAndIncrement();
    }


    @Override
    public void detachTraceObject() {
        this.threadLocal.remove();
    }
}
