package com.nhn.pinpoint.profiler.modifier.redis;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentException;
import com.nhn.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;

/**
 * jedis(redis client) modifier
 * 
 * @author jaehong.kim
 *
 */
public class BinaryJedisModifier extends JedisModifier {

    public BinaryJedisModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/BinaryJedis";
    }

    @Override
    protected void beforeAddInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, final InstrumentClass instrumentClass) throws NotFoundInstrumentException, InstrumentException {
        // trace endPoint. 
        instrumentClass.addTraceValue(MapTraceValue.class);
    }
}
