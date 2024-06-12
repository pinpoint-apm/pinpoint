/*
 * Copyright 2024 NAVER Corp.
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
package com.navercorp.pinpoint.common.id;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.util.AgentUuidUtils;
import com.navercorp.pinpoint.common.util.UuidUtils;

import java.util.UUID;

/**
 * @author youngjin.kim2
 */
public class AgentId extends StringPinpointIdentifier {

    private static final int MAX_LENGTH = PinpointConstants.AGENT_ID_MAX_LEN;

    private AgentId(String value) {
        super(value);

        if (value().length() > MAX_LENGTH) {
            throw new IllegalArgumentException("length of agentId cannot be greater than " + MAX_LENGTH);
        }
    }

    public static AgentId random() {
        UUID uuid = UuidUtils.createV4();
        String str = AgentUuidUtils.encode(uuid);
        return new AgentId(str);
    }

    public static AgentId of(String value) {
        if (value == null) { // TODO: null-check should be done prior
            return null;
        }

        return new AgentId(value);
    }

    public static String unwrap(AgentId agentId) {
        if (agentId == null) { // TODO: null-check should be done prior
            return null;
        }
        return agentId.value();
    }

}
