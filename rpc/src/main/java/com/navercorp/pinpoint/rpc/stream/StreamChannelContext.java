/*
 * Copyright 2014 NAVER Corp.
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
 */

package com.navercorp.pinpoint.rpc.stream;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author koo.taejin
 */
public abstract class StreamChannelContext {

    private final ConcurrentHashMap<String, Object> attribute = new ConcurrentHashMap<String, Object>();

    public StreamChannelContext() {
    }

    abstract public StreamChannel getStreamChannel();

    public int getStreamId() {
        return getStreamChannel().getStreamId();
    }

    public final Object getAttribute(String key) {
        return attribute.get(key);
    }

    public final Object setAttributeIfAbsent(String key, Object value) {
        return attribute.putIfAbsent(key, value);
    }

    public final Object removeAttribute(String key) {
        return attribute.remove(key);
    }

    public boolean isServer() {
        return getStreamChannel().isServer();
    }

    @Override
    public String toString() {
        return getStreamChannel().toString();
    }

}
