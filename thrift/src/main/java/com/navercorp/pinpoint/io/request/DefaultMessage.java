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
import com.navercorp.pinpoint.io.header.HeaderEntity;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultMessage<T> implements Message<T> {
    private final Header header;
    private final HeaderEntity headerEntity;
    private final T data;

    public DefaultMessage(Header header, HeaderEntity headerEntity, T data) {
        this.header = Objects.requireNonNull(header, "header");
        this.headerEntity = Objects.requireNonNull(headerEntity, "headerEntity");
        this.data = data;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public HeaderEntity getHeaderEntity() {
        return headerEntity;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DefaultMessage{" +
                "header=" + header +
                ", headerEntity=" + headerEntity +
                ", data=" + data +
                '}';
    }
}
