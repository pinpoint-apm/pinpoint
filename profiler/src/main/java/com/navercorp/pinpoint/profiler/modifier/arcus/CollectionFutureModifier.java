package com.nhn.pinpoint.profiler.modifier.arcus;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class CollectionFutureModifier extends AbstractFutureModifier {

    public CollectionFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public String getTargetClass() {
        return "net/spy/memcached/internal/CollectionFuture";

    }
}
