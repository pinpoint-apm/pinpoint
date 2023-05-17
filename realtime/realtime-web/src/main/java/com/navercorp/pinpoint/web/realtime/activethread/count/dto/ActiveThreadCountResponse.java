/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.web.realtime.activethread.count.dto;

import com.navercorp.pinpoint.common.server.cluster.ClusterKey;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author youngjin.kim2
 */
public class ActiveThreadCountResponse {

    private static final String COMMAND = "activeThreadCount";
    private static final String TYPE = "RESPONSE";

    private final Result result;

    public ActiveThreadCountResponse(String applicationName, long timeStamp) {
        this.result = new Result(applicationName, timeStamp);
    }

    @SuppressWarnings("unused")
    public String getCommand() {
        return COMMAND;
    }

    @SuppressWarnings("unused")
    public String getType() {
        return TYPE;
    }

    @SuppressWarnings("unused")
    public Result getResult() {
        return result;
    }

    public void putSuccessAgent(ClusterKey agentKey, List<Integer> values) {
        putAgent(agentKey.getAgentId(), 0, "OK", values);
    }

    public void putFailureAgent(ClusterKey agentKey, ATCSupply supply, long connectUntil) {
        final String message = decideMessage(supply, connectUntil);
        putAgent(agentKey.getAgentId(), -1, message, List.of());
    }

    private String decideMessage(ATCSupply supply, long connectUntil) {
        if (supply != null) {
            return supply.getMessage().getMessage();
        }
        if (System.currentTimeMillis() <= connectUntil) {
            return "CONNECTING";
        } else {
            return "TIMEOUT";
        }
    }

    private void putAgent(String agentId, int code, String message, List<Integer> status) {
        final Result.Count count = new Result.Count(code, message, status);
        result.activeThreadCounts.put(agentId, count);
    }

    public static class Result {
        private final Map<String, Count> activeThreadCounts = new HashMap<>();
        private final String applicationName;
        private final long timeStamp;

        public Result(String applicationName, long timeStamp) {
            this.applicationName = applicationName;
            this.timeStamp = timeStamp;
        }

        @SuppressWarnings("unused")
        public String getApplicationName() {
            return applicationName;
        }

        @SuppressWarnings("unused")
        public long getTimeStamp() {
            return timeStamp;
        }

        @SuppressWarnings("unused")
        public Map<String, Count> getActiveThreadCounts() {
            return activeThreadCounts;
        }

        public static class Count {
            private final int code;
            private final String message;
            private final List<Integer> status;

            public Count(int code, String message, List<Integer> status) {
                this.code = code;
                this.message = message;
                this.status = status;
            }

            @SuppressWarnings("unused")
            public int getCode() {
                return code;
            }

            @SuppressWarnings("unused")
            public String getMessage() {
                return message;
            }

            @SuppressWarnings("unused")
            public List<Integer> getStatus() {
                return status;
            }
        }
    }

}
