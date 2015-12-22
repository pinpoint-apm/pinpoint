/**
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * @author Jongho Moon
 *
 */
public class Cache {
    private static final int SHARD_SIZE_LIMIT = 64;
    private static final int SHARD_NUM = 13;
    
    private final Shard[] shards;
    
    public Cache() {
        shards = new Shard[SHARD_NUM];
        
        for (int i = 0; i < SHARD_NUM; i++) {
            shards[i] = new Shard();
        }
    }
    
    public boolean contains(Class<?> clazz) {
        final Shard shard = getShard(clazz);
        
        synchronized (shard) {
            return shard.containsKey(clazz);
        }
    }
    
    public void put(Class<?> clazz) {

        final Shard shard = getShard(clazz);
        synchronized (shard) {
            shard.put(clazz, Boolean.TRUE);
        }
    }
    
    private Shard getShard(Class<?> clazz) {
        int idx = clazz.getName().hashCode() % SHARD_NUM;
        
        if (idx < 0) {
            idx += SHARD_NUM;
        }
        
        return shards[idx];
    }
    
    @SuppressWarnings("serial")
    private static final class Shard extends LinkedHashMap<Class<?>, Boolean> {

        @Override
        protected boolean removeEldestEntry(Entry<Class<?>, Boolean> eldest) {
            return size() > SHARD_SIZE_LIMIT;
        }
        
    }
}
