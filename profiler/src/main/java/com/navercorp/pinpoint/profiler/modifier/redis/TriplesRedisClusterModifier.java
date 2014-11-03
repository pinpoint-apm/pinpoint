package com.nhn.pinpoint.profiler.modifier.redis;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;

/**
 * RedisCluster(nBase-ARC client) modifier
 * 
 * @author jaehong.kim
 *
 */
public class TriplesRedisClusterModifier extends RedisClusterModifier {

    public TriplesRedisClusterModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public String getTargetClass() {
        return "com/nhncorp/redis/cluster/triples/TriplesRedisCluster";
    }
}