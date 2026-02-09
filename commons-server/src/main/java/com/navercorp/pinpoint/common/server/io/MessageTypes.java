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

package com.navercorp.pinpoint.common.server.io;



public enum MessageTypes implements MessageType {

    EMPTY(-1),
    SPAN(40),
    AGENT_INFO(50),
    AGENT_STAT(55),
    AGENT_STAT_BATCH(56),
    AGENT_URI_STAT(57),
    PING(60),
    PING_CLOSE(62),
    SPANCHUNK(70),
    SQLMETADATA(300),
    SQLUIDMETADATA(301),
    APIMETADATA(310),
    STRINGMETADATA(330),
    EXCEPTIONMETADATA(340);

    private final int code;

    MessageTypes(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }


}