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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum PinpointValueEncoder {
    INSTANCE;

    private Object encoder;
    private Method method;
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private String ENCODE_METHOD_NAME = "encode";
    private boolean initialize = false;

    private Method getEncodeMethod(Class clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(ENCODE_METHOD_NAME)) {
                return m;
            }
        }
        return null;
    }

    public void init(String className) {
        if (StringUtils.isEmpty(className)) return;
        try {
            Class clazz = Class.forName(className);
            encoder = clazz.newInstance();
            method = getEncodeMethod(clazz);
            initialize = true;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            logger.error("FAILED TO LOAD ENCODER", e);
        }
    }

    public <V> V encode(V value, String transactionId, String spanID, String parentSpanID, String parentApplicationName,
                        String parentApplicationType, String flags) {
        if (!initialize) return value;
        try {
            return (V) this.method.invoke(encoder, value, transactionId, spanID, parentSpanID, parentApplicationName, parentApplicationType, flags);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error("FAILED TO CALL ENCODE", e);
        }
        return value;
    }

}
