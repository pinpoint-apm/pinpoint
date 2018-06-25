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

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultMessage<T> implements Message<T> {
    private final Header header;
    private final T data;

    public DefaultMessage(Header header, T data) {
        if (header == null) {
            throw new NullPointerException("header must not be null");
        }

        this.header = header;
        this.data = data;
    }

    @Override
    public Header getHeader() {
        return header;
    }

    @Override
    public T getData() {
        return data;
    }
}
