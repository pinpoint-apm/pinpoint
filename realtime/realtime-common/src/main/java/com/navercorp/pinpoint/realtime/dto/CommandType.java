/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.realtime.dto;


public enum CommandType {
    // Using reflection would make code cleaner.
    // But it also makes it hard to handle exception, constructor and will show relatively low performance.

    RESULT((short) 320),

    TRANSFER((short) 700),
    TRANSFER_RESPONSE((short) 701),

    ECHO((short) 710),

    THREAD_DUMP((short) 720),
    THREAD_DUMP_RESPONSE((short) 721),

    ACTIVE_THREAD_COUNT((short) 730),
    ACTIVE_THREAD_COUNT_RESPONSE((short) 731),

    ACTIVE_THREAD_DUMP((short) 740),
    ACTIVE_THREAD_DUMP_RESPONSE((short) 741),

    ACTIVE_THREAD_LIGHT_DUMP((short) 750),
    ACTIVE_THREAD_LIGHT_DUMP_RESPONSE((short) 751);

    private final short code;

    CommandType(short code) {
        this.code = code;
    }

    public short getCode() {
        return code;
    }

    public static CommandType getType(short code) {
        return switch (code) {
            case 320 -> RESULT;
            case 700 -> TRANSFER;
            case 701 -> TRANSFER_RESPONSE;
            case 710 -> ECHO;
            case 720 -> THREAD_DUMP;
            case 721 -> THREAD_DUMP_RESPONSE;
            case 730 -> ACTIVE_THREAD_COUNT;
            case 731 -> ACTIVE_THREAD_COUNT_RESPONSE;
            case 740 -> ACTIVE_THREAD_DUMP;
            case 741 -> ACTIVE_THREAD_DUMP_RESPONSE;
            case 750 -> ACTIVE_THREAD_LIGHT_DUMP;
            case 751 -> ACTIVE_THREAD_LIGHT_DUMP_RESPONSE;
            default -> null;
        };
    }

}
