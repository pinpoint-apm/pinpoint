/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.codec;

import com.navercorp.pinpoint.common.buffer.Buffer;

/**
 * @author Taejin Koo
 */
public interface StringTypedBufferHandler {
    void put(Buffer buffer, String value);
    String read(Buffer buffer);

    StringTypedBufferHandler VARIABLE_HANDLER = new StringTypedBufferHandler() {

        @Override
        public void put(Buffer buffer, String value) {
            buffer.putPrefixedString(value);
        }

        @Override
        public String read(Buffer buffer) {
            return buffer.readPrefixedString();
        }

    };

}
