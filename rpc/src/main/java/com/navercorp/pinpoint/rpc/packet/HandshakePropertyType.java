/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.packet;

import com.navercorp.pinpoint.rpc.util.ClassUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Taejin Koo
 */
public enum HandshakePropertyType {

    SUPPORT_SERVER("supportServer", Boolean.class, false),
    SUPPORT_COMMAND_LIST("supportCommandList", List.class, false),

    HOSTNAME("hostName", String.class),
    IP("ip", String.class),
    AGENT_ID("agentId", String.class),
    APPLICATION_NAME("applicationName", String.class),
    SERVICE_TYPE("serviceType", Integer.class),
    PID("pid", Integer.class),
    VERSION("version", String.class),
    START_TIMESTAMP("startTimestamp", Long.class);


    private final String name;
    private final Class clazzType;
    private final boolean isRequired;

    private static final Set<HandshakePropertyType> HANDSHAKE_PROPERTY_TYPE = EnumSet.allOf(HandshakePropertyType.class);

    HandshakePropertyType(String name, Class clazzType) {
        this(name, clazzType, true);
    }

    HandshakePropertyType(String name, Class clazzType, boolean isRequired) {
        this.name = name;
        this.clazzType = clazzType;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public Class getClazzType() {
        return clazzType;
    }

    public static boolean hasRequiredKeys(Map properties) {
        for (HandshakePropertyType type : HANDSHAKE_PROPERTY_TYPE) {
            if (!type.isRequired) {
                continue;
            }

            Object value = properties.get(type.getName());

            if (value == null) {
                return false;
            }

            if (!ClassUtils.isAssignable(value.getClass(), type.getClazzType())) {
                return false;
            }
        }

        return true;
    }

}
