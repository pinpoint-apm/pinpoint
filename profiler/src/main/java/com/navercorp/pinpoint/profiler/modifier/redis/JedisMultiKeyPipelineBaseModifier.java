package com.navercorp.pinpoint.profiler.modifier.redis;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

/**
 * jedis(redis client) pipeline modifier
 * 
 * @author jaehong.kim
 *
 */
public class JedisMultiKeyPipelineBaseModifier extends JedisPipelineBaseModifier {

    public JedisMultiKeyPipelineBaseModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "redis/clients/jedis/MultiKeyPipelineBase";
    }
}