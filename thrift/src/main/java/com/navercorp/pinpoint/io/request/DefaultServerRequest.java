/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.io.header.Header;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultServerRequest<T> implements ServerRequest<T> {

    private final Message<T> message;

    // lazy initialize
    private Map<Object, Object> attribute;

    public DefaultServerRequest(Message<T> message) {
        if (message == null) {
            throw new NullPointerException("message must not be null");
        }
        this.message = message;
    }


    @Override
    public Header getHeader() {
        return message.getHeader();
    }

    @Override
    public T getData() {
        return message.getData();
    }

    @Override
    public void setAttribute(Object key, Object value) {
        Map<Object, Object> map = getAttributeMap();
        map.put(key, value);
    }

    private Map<Object, Object> getAttributeMap() {
        if (attribute == null) {
            attribute = new HashMap<Object, Object>();
        }
        return attribute;
    }

    @Override
    public Object getAttribute(Object key) {
        Map<Object, Object> map = getAttributeMap();
        return map.get(key);
    }
}
