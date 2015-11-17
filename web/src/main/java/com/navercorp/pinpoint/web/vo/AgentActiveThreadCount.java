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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;

/**
 * @Author Taejin Koo
 */
public class AgentActiveThreadCount {

    private static final short OK_CODE = 0;
    private static final String OK_CODE_MESSAGE = "OK";

    private final String agentId;

    private short code = -1;
    private String codeMessage = "UNKNOWN";

    private TCmdActiveThreadCountRes activeThreadCount;

    public AgentActiveThreadCount(String agentId) {
        this.agentId = agentId;
    }

    public void setResult(TCmdActiveThreadCountRes activeThreadCount) {
        if (activeThreadCount != null) {
            this.activeThreadCount = activeThreadCount;
            this.code = OK_CODE;
            this.codeMessage = OK_CODE_MESSAGE;
        }
    }

    public void setFail(String codeMessage) {
        setFail((short) -1, codeMessage);
    }

    public void setFail(short code, String codeMessage) {
        this.code = code;
        this.codeMessage = codeMessage;
    }

    public String getAgentId() {
        return agentId;
    }

    public short getCode() {
        return code;
    }

    public String getCodeMessage() {
        return codeMessage;
    }

    public TCmdActiveThreadCountRes getActiveThreadCount() {
        return activeThreadCount;
    }

    @Override
    public String toString() {
        return "AgentActiveThreadCount{" +
                "agentId='" + agentId + '\'' +
                ", code=" + getCode() +
                ", codeMessage=" + getCodeMessage() +
                ", activeThreadCount=" + activeThreadCount +
                '}';
    }

}
