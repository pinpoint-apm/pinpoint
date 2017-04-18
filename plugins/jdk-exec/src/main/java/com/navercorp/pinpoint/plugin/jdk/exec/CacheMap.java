/*
 * Copyright 2016 NAVER Corp.
 *
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
 *
 */
package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hamlet-lee
 */
public class CacheMap {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    static class ValuePair{
        Object val;
        long ts;
        long threadId;
    }

    static HashMap<String, CacheMap> maps = new HashMap<String, CacheMap>();
    public static synchronized CacheMap getInstance(String name){
        CacheMap m = maps.get(name);
        if( m == null){
            m = new CacheMap(name);
            maps.put(name, m);
        }
        return m;
    }

    protected CacheMap(String name){
        if( true )
        {
            new Timer("pinpoint-JDK-EXEC-CacheMap-"+name+"-cleaner").schedule(new TimerTask(){
                @Override
                public void run() {
                    //auto clear every 10 secs
                    if( sharedMap.size() < 100) {
                        //do nothing if the map is not big
                        return;
                    }
                    long maxAge = System.currentTimeMillis() - 1000*10;
                    for (Map.Entry<Object, ValuePair> entry : sharedMap.entrySet()){
                        if(entry.getValue().ts < maxAge)
                        {
                            sharedMap.remove(entry.getKey());
                        }
                    }
                }

            }, new Date(), 1000*10);
        }
    }
    private Map<Object, ValuePair> sharedMap = new ConcurrentHashMap<Object, ValuePair>();
    private ThreadLocal<LocalCache> threadLocalCache = new ThreadLocal<LocalCache>(){
        @Override
        protected LocalCache initialValue() {
            return new LocalCache();
        }
    };

    public void put(Object key, Object val){
        ValuePair pair = new ValuePair();
        pair.val = val;
        pair.ts = System.currentTimeMillis();
        pair.threadId = Thread.currentThread().getId();
        sharedMap.put(key, pair);
    }

    public Object get(Object key){
        ValuePair pair = sharedMap.get(key);
        if( pair != null){
            if( Thread.currentThread().getId() != pair.threadId ){
                //move to thread local map, so will not be cleared before task end
                sharedMap.remove(key);
                threadLocalCache.get().push( key, pair.val);
            }
            return pair.val;
        }else{
            Object v = threadLocalCache.get().getAndRemoveNewer(key);
            if( v != null){
                if( logger.isDebugEnabled()) {
                    logger.debug("not found in thread local cache!", new Throwable("show stack"));
                }
                return v;
            }else{
                return null;
            }
        }
    }

    public static class LocalCache {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
        private ArrayList<Object> arrayList = new ArrayList<Object>();
        private HashMap<Object, Integer> hashMap = new HashMap<Object, Integer>();
        public void push(Object key, Object val){
            //size check
            if( arrayList.size() > 100) {
                //nested 100 layers, should never happen
                if( logger.isDebugEnabled() ) {
                    logger.debug("local cache size too large," +
                            " something happened!", new Throwable());
                }
                arrayList.clear();
                hashMap.clear();
            }

            //add at tail
            arrayList.add(val);
            //record position
            hashMap.put(val, arrayList.size() - 1);
        }
        public Object getAndRemoveNewer(Object key){
            Integer pos = hashMap.get(key);
            if( pos != null) {
                //found
                Object value = arrayList.get(pos);
                //remove newer
                while( pos < arrayList.size() - 1) {
                    arrayList.remove( arrayList.size() - 1);
                }
                return value;
            }else{
                return null;
            }
        }
    }
}
