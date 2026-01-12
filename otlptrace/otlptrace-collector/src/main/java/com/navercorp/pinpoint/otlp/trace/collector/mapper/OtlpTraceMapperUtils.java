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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.util.List;

public class OtlpTraceMapperUtils {
    public static String getAgentId(List<KeyValue> attributesList) {
        final String agentId = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.agentId")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (agentId == null) {
            throw new IllegalStateException("not found agentId");
        }

        return agentId;
    }

    public static String getApplicationName(List<KeyValue> attributesList) {
        final String applicationName = attributesList.stream().filter(kv -> kv.getKey().equals("pinpoint.applicationName")).findFirst().map(kv -> kv.getValue().getStringValue()).orElse(null);
        if (applicationName == null) {
            throw new IllegalStateException("not found applicationName");
        }

        return applicationName;
    }

    public static long getSpanId(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            throw new IllegalArgumentException("not found spanId");
        }

        return ByteArrayUtils.bytesToLong(bytes, 0);
    }

    public static long getParentSpanId(byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return -1;
        }

        return ByteArrayUtils.bytesToLong(bytes, 0);
    }

    public static String getAttributeAnnotationValue(List<KeyValue> keyValueList) {
        StringBuilder sb = new StringBuilder();
        for (KeyValue kv : keyValueList) {
            // TODO add filter
            if (kv.getKey().equals("exception.stacktrace") || kv.getKey().equals("db.user")) {
                continue;
            }

            if (sb.isEmpty()) {
                sb.append("{ ");
            } else {
                sb.append(" , ");
            }
            sb.append(getKeyValue(kv));
        }
        if (!sb.isEmpty()) {
            sb.append(" }");
        }
        return sb.toString();
    }

    public static String getKeyValue(KeyValue keyValue) {
        AnyValue anyValue = keyValue.getValue();
        if (anyValue.hasIntValue()) {
            return keyValue.getKey() + " : " + anyValue.getIntValue();
        } else if (anyValue.hasDoubleValue()) {
            return keyValue.getKey() + " : " + anyValue.getDoubleValue();
        } else if (anyValue.hasBoolValue()) {
            return keyValue.getKey() + " : " + anyValue.getBoolValue();
        } else if (anyValue.hasStringValue()) {
            return keyValue.getKey() + " : " + anyValue.getStringValue();
        } else if (anyValue.hasArrayValue()) {
            return keyValue.getKey() + " : " + anyValue.getArrayValue();
        } else if (anyValue.hasBytesValue()) {
            return keyValue.getKey() + " : " + anyValue.getBytesValue();
        } else if (anyValue.hasKvlistValue()) {
            return keyValue.getKey() + " : " + getAttributeAnnotationValue(anyValue.getKvlistValue().getValuesList());
        } else {
            return keyValue.getKey() + " : " + anyValue.toString();
        }
    }
}
