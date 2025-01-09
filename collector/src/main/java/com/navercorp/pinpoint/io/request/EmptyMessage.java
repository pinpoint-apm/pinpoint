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
import com.navercorp.pinpoint.io.header.v1.HeaderV1;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptyMessage<T> implements Message<T> {
    public static final Message<?> INSTANCE = new EmptyMessage<>();

    private static final Header EMPTY_HEADER = new HeaderV1((short) -1);

    @Override
    public Header getHeader() {
        return EMPTY_HEADER;
    }

    @Override
    public HeaderEntity getHeaderEntity() {
        return HeaderEntity.EMPTY_HEADER_ENTITY;
    }

    @Override
    public T getData() {
        return null;
    }

    public static <T> Message<T> emptyMessage() {
        return (Message<T>) INSTANCE;
    }
}
