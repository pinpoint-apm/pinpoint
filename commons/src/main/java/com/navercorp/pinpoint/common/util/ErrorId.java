/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

public class ErrorId {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private static final long EMPTY_ID = 0;

    public static final ErrorId EMPTY = new ErrorId(EMPTY_ID);

    private final long id;

    private ErrorId(long id) {
        this.id = id;
    }

    public static ErrorId of(long id) {
        if (id == EMPTY_ID) {
            return EMPTY;
        }
        return new ErrorId(id);
    }

    public static ErrorId random() {
        long id;
        do {
            id = ThreadLocalRandom.current().nextLong();
        } while (id == EMPTY_ID);
        return new ErrorId(id);
    }

    public long getId() {
        return id;
    }

    public String toBase64() {
        if (id == EMPTY_ID) {
            return "";
        }
        byte[] bytes = new byte[BytesUtils.LONG_BYTE_LENGTH];
        BytesUtils.writeLong(id, bytes, 0);
        return ENCODER.encodeToString(bytes);
    }

    @Override
    public String toString() {
        return toBase64();
    }
}