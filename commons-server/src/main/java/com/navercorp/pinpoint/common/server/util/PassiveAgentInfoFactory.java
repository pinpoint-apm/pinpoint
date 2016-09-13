/*
 * Copyright 2014 NAVER Corp.
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


/**
 * @author Peter Chen
 */
public class PassiveAgentInfoFactory implements AgentInfoFactory{
    private static final short APACHE_AGENT_TYPE = 1;
    private static final short NGINX_AGENT_TYPE = 2;

    public String createAgentId(short agentType) {
        switch (agentType) {
            case APACHE_AGENT_TYPE:
                return "apache";
            case NGINX_AGENT_TYPE:
                return "nginx";
        }
        return "unknown";
    }
    public String createApplicationName(short agentType) {
        switch (agentType) {
            case APACHE_AGENT_TYPE:
                return "Apache";
            case NGINX_AGENT_TYPE:
                return "Nginx";
        }
        return "unknown";
    }
}