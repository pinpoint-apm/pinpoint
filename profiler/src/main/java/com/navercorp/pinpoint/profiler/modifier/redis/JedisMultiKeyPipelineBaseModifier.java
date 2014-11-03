package com.nhn.pinpoint.profiler.modifier.redis;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;

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