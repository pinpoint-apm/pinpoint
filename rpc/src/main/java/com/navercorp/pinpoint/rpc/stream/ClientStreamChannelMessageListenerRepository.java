/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.rpc.stream;

import com.navercorp.pinpoint.rpc.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Taejin Koo
 */
public class ClientStreamChannelMessageListenerRepository<V extends ClientStreamChannelMessageListener> {

    private final ConcurrentHashMap<String, V> repository = new ConcurrentHashMap<String, V>();

    public void put(String key, V messageListener) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is empty.");
        }

        if (messageListener == null) {
            throw new IllegalArgumentException("messageListener is null.");
        }

        repository.put(key, messageListener);
    }

    public void get(String key) {
        repository.get(key);
    }

    public void remove(String key) {
        if (StringUtils.isEmpty(key)) {
            return;
        }

        repository.remove(key);
    }

    public void remove(V messageListener) {
        if (messageListener == null) {
            return;
        }

        String key = getKey(messageListener);
        repository.remove(key);
    }

    public boolean contains(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }

        return repository.containsKey(key);
    }

    public boolean contains(V value) {
        if (value == null) {
            return false;
        }

        return repository.contains(value);
    }

    private String getKey(V messageListener) {
        for (Map.Entry<String, V> entry : repository.entrySet()) {
            String key = entry.getKey();
            V value = entry.getValue();

            if (messageListener == value) {
                return key;
            }
        }

        return null;
    }

    public Collection<V> values() {
        return repository.values();
    }

    public int size() {
        return repository.size();
    }

}
