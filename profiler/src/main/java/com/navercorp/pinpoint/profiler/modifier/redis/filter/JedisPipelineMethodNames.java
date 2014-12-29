package com.navercorp.pinpoint.profiler.modifier.redis.filter;

import java.util.Arrays;
import java.util.Set;

/**
 * JedisPipeline method names
 *   - jedis method names + pipeline method names
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineMethodNames {

    private static Set<String> names = null;
    
    public static Set<String> get() {
        if(names != null) {
            return names;
        }
        
        final String[] methodNames = { 
                "sync",
                "syncAndReturnAll"
            };
        
        final Set<String> jedisMethoNames = JedisMethodNames.get();
        jedisMethoNames.addAll(Arrays.asList(methodNames));
        names = jedisMethoNames;

        return names;
    }
}