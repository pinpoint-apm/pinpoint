package com.nhn.pinpoint.profiler.modifier.redis.filter;

import java.util.Arrays;
import java.util.Set;

/**
 * RedisClusterPipeline method names
 *   - jedis method names + RedisCluster method names
 * 
 * @author jaehong.kim
 *
 */
public class RedisClusterMethodNames {

    private static Set<String> names = null;
    
    public static Set<String> get() {
        if(names != null) {
            return names;
        }
        
        final String[] methodNames = { 
                "zadd2",
                "slkeys",
                "slget",
                "slmget",
                "sladd",
                "sladdAt",
                "slset",
                "sldel",
                "slrem",
                "slcount",
                "slexists",
                "slexpire",
                "slttl",
                "ssget",
                "ssmget",
                "sskeys",
                "ssadd",
                "ssaddAt",
                "ssset",
                "ssdel",
                "ssrem",
                "sscount",
                "ssexists",
                "ssexpire",
                "ssttl"
        };
        
        final Set<String> jedisMethodNames = JedisMethodNames.get();
        jedisMethodNames.addAll(Arrays.asList(methodNames));
        names = jedisMethodNames;
        
        return names;
    }
}
