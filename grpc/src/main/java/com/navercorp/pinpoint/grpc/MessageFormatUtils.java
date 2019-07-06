/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.TextFormat;

/**
 * @author jaehong.kim
 */
public final class MessageFormatUtils {

    private static final String NULL_STR = "null";
    private static final LogMessage NULL_LOG = new NullLogMessage();

    private MessageFormatUtils() {
    }

    public static LogMessage debugLog(GeneratedMessageV3 message) {
        if (message == null) {
            return NULL_LOG;
        }
        return new LazyLogMessage(message);
    }

    // No need wrapper class
//    public static String debugString(GeneratedMessageV3 message) {
//        if (message == null) {
//            return NULL_STR;
//        }
//        return TextFormat.shortDebugString(message);
//    }


    public interface LogMessage {
    }

    private static class LazyLogMessage implements LogMessage {
        private final GeneratedMessageV3 message;

        private LazyLogMessage(final GeneratedMessageV3 message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return TextFormat.shortDebugString(message);
        }
    }

    private static class NullLogMessage implements LogMessage {
        @Override
        public String toString() {
            return NULL_STR;
        }
    }
}
