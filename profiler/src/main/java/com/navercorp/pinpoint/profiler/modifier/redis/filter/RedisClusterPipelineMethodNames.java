package com.nhn.pinpoint.profiler.modifier.redis.filter;

import java.util.Arrays;
import java.util.Set;

/**
 * RedisClusterPipeline method names
 *   - RedisCluster method names + pipeline method names
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterPipelineMethodNames {

    private static Set<String> names = null;
    
    public static Set<String> get() {
        if(names != null) {
            return names;
        }
        
        final String[] methodNames = { 
                "sync",
                "syncAndReturnAll",
                "close"
        };
        
        final Set<String> redisClusterMethodNames = RedisClusterMethodNames.get();
        redisClusterMethodNames.addAll(Arrays.asList(methodNames));
        names = redisClusterMethodNames;
        
        return names;
    }
}