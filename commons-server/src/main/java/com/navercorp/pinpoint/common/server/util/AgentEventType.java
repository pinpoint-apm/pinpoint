/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.thrift.dto.TDeadlock;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDumpResponse;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory.*;

/**
 * @author HyunGil Jeong
 */
public enum AgentEventType {
    AGENT_CONNECTED(10100, "Agent connected", Void.class, DURATIONAL, AGENT_LIFECYCLE),
    AGENT_PING(10199, "Agent ping", Void.class, AGENT_LIFECYCLE),
    AGENT_SHUTDOWN(10200, "Agent shutdown", Void.class, DURATIONAL, AGENT_LIFECYCLE),
    AGENT_UNEXPECTED_SHUTDOWN(10201, "Agent unexpected shutdown", Void.class, DURATIONAL, AGENT_LIFECYCLE),
    AGENT_CLOSED_BY_SERVER(10300, "Agent connection closed by server", Void.class, DURATIONAL, AGENT_LIFECYCLE),
    AGENT_UNEXPECTED_CLOSE_BY_SERVER(10301, "Agent connection unexpectedly closed by server", Void.class, DURATIONAL, AGENT_LIFECYCLE),
    AGENT_DEADLOCK_DETECTED(10401, "Agent deadlock detected", TDeadlock.class, AGENT_LIFECYCLE),
    USER_THREAD_DUMP(20100, "Thread dump by user", TCommandThreadDumpResponse.class, USER_REQUEST, THREAD_DUMP),
    OTHER(-1, "Other event", String.class, AgentEventTypeCategory.OTHER);
    
    private final int code;
    private final String desc;
    private final Class<?> messageType;
    private final Set<AgentEventTypeCategory> category;

    private static final Set<AgentEventType> AGENT_EVENT_TYPE = EnumSet.allOf(AgentEventType.class);

    AgentEventType(int code, String desc, Class<?> messageType, AgentEventTypeCategory... category) {
        this.code = code;
        this.desc = desc;
        this.messageType = messageType;
        this.category = asSet(category);
    }

    private Set<AgentEventTypeCategory> asSet(AgentEventTypeCategory[] category) {
        if (ArrayUtils.isEmpty(category)) {
           return Collections.emptySet();
       }
       return EnumSet.copyOf(Arrays.asList(category));
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return desc;
    }
    
    public Class<?> getMessageType() {
        return this.messageType;
    }
    
    public Set<AgentEventTypeCategory> getCategory() {
        return Collections.unmodifiableSet(this.category);
    }
    
    public boolean isCategorizedAs(AgentEventTypeCategory category) {
        return this.category.contains(category);
    }

    @Override
    public String toString() {
        return desc;
    }
    
    public static AgentEventType getTypeByCode(int code) {
        for (AgentEventType eventType : AGENT_EVENT_TYPE) {
            if (eventType.code == code) {
                return eventType;
            }
        }
        return null;
    }

    /**
     * typo API
     * @deprecated Since 1.7.0. Use {@link #getTypesByCategory}
     */
    @Deprecated
    public static Set<AgentEventType> getTypesByCatgory(AgentEventTypeCategory category) {
        return getTypesByCategory(category);
    }

    public static Set<AgentEventType> getTypesByCategory(AgentEventTypeCategory category) {
        final Set<AgentEventType> eventTypes = new HashSet<AgentEventType>();
        for (AgentEventType eventType : AGENT_EVENT_TYPE) {
            if (eventType.category.contains(category)) {
                eventTypes.add(eventType);
            }
        }
        return eventTypes;
    }
}
