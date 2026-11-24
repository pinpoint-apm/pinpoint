/*
 * Copyright 2026 NAVER Corp.
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

package com.pinpoint.test.plugin;

import jakarta.jms.JMSException;
import jakarta.jms.Message;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads the trace headers the producer side of the plugin put on a message, so that the propagation can be
 * checked from the app log without a collector.
 */
final class PinpointHeaders {

    private static final String PREFIX = "Pinpoint-";

    private PinpointHeaders() {
    }

    static Map<String, Object> of(Message message) {
        Map<String, Object> headers = new TreeMap<>();
        try {
            Enumeration<?> names = message.getPropertyNames();
            while (names.hasMoreElements()) {
                String name = String.valueOf(names.nextElement());
                if (name.startsWith(PREFIX)) {
                    headers.put(name, message.getObjectProperty(name));
                }
            }
        } catch (JMSException e) {
            headers.put("error", e.toString());
        }
        return headers;
    }
}
