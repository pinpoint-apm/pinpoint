/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.encoder;

import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;

public class BytesEncoder {
    public byte[] encode(byte[] value, String transactionId, String parentSpanID, String spanID , String parentApplicationName,
                         String parentApplicationType, String flags) {
        byte[] pinpointHeader = new StringBuffer().append(KafkaConstants.PINPOINT_HEADER_PREFIX)
                .append(transactionId).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(parentSpanID).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(spanID).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(parentApplicationName).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(parentApplicationType).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(flags).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                .append(KafkaConstants.PINPOINT_HEADER_POSTFIX).toString().getBytes();
        byte[] valueIncludePinpointHeader = new byte[pinpointHeader.length + value.length];
        System.arraycopy(pinpointHeader, 0, valueIncludePinpointHeader, 0, pinpointHeader.length);
        System.arraycopy(value, 0, valueIncludePinpointHeader, pinpointHeader.length, value.length);
        return valueIncludePinpointHeader;
    }
}
