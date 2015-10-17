/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.web.vo.AgentActiveThreadCountList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author Taejin Koo
 */
public class ActiveThreadCountResponseMessageConverter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper JSON_SERIALIZER = new ObjectMapper();

    private static final String APPLICATION_NAME = "applicationName";
    private static final String ACTIVE_THREAD_COUNTS = "activeThreadCounts";
    private static final String TIME_STAMP = "timeStamp";

    private final String applicationName;

    ActiveThreadCountResponseMessageConverter(String applicationName) {
        this.applicationName = applicationName;
    }

    TextMessage createResponseMessage(AgentActiveThreadCountList activeThreadCountList) {
        return createResponseMessage(activeThreadCountList, System.currentTimeMillis());
    }

    TextMessage createResponseMessage(AgentActiveThreadCountList activeThreadCountList, long timeStamp) {
        String responseMessage = createResponseMessage0(activeThreadCountList, timeStamp);
        return new TextMessage(responseMessage);
    }

    private String createResponseMessage0(AgentActiveThreadCountList activeThreadCount, long timeStamp) {
        Map<String, Object> response = new HashMap<String, Object>();

        response.put(APPLICATION_NAME, applicationName);
        response.put(ACTIVE_THREAD_COUNTS, activeThreadCount);
        response.put(TIME_STAMP, timeStamp);

        try {
            return JSON_SERIALIZER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.warn(e.getMessage(), e);
        }

        return createEmptyResponseMessage(applicationName, timeStamp);
    }

    private String createEmptyResponseMessage(String applicationName, long timeStamp) {
        StringBuilder emptyJsonMessage = new StringBuilder();
        emptyJsonMessage.append("{");
        emptyJsonMessage.append("\"").append(APPLICATION_NAME).append("\"").append(":").append("\"").append(applicationName).append("\"").append(",");
        emptyJsonMessage.append("\"").append(ACTIVE_THREAD_COUNTS).append("\"").append(":").append("{}").append(",");
        emptyJsonMessage.append("\"").append(TIME_STAMP).append("\"").append(":").append(timeStamp);
        emptyJsonMessage.append("}");

        return emptyJsonMessage.toString();
    }

}
