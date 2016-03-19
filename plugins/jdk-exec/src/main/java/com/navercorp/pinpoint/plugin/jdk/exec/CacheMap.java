package com.navercorp.pinpoint.plugin.jdk.exec;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hamlet-lee
 */
public class CacheMap {
    static class ValuePair{
        Object val;
        long ts;
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
                    if( map.size() < 100) {
                        //do nothing if the map is not big
                        return;
                    }
                    long maxAge = System.currentTimeMillis() - 1000*10;
                    for (Map.Entry<Object, ValuePair> entry : map.entrySet()){
                        if(entry.getValue().ts < maxAge)
                        {
                            map.remove(entry.getKey());
                        }
                    }
                }

            }, new Date(), 1000*10);
        }
    }
    private Map<Object, ValuePair> map = new ConcurrentHashMap<Object, ValuePair>();
    public void put(Object key, Object val){
        ValuePair pair = new ValuePair();
        pair.val = val;
        pair.ts = System.currentTimeMillis();
        map.put(key, pair);
    }

    public Object get(Object key){
        ValuePair pair = map.get(key);
        if( pair == null)
        {
            return null;
        }
        return pair.val;
    }
}
