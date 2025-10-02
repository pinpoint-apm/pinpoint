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

package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.common.server.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;

public interface LinkService {

    /**
     * Calling MySQL from Tomcat generates the following message for the caller(Tomcat) :<br/>
     * emeroad-app (TOMCAT) -> MySQL_DB_ID (MYSQL)[10.25.141.69:3306] <br/>
     * <br/>
     * The following message is generated for the in(MySQL) :<br/>
     * MySQL (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080]
     *
     * @param selfVertex  outVertex
     * @param selfAgentId outAgentId
     * @param outVertex   inVertex
     * @param outHost     inHost
     * @param elapsed    elapsed
     * @param isError    isError
     */
    void updateOutLink(
            long requestTime,
            Vertex selfVertex,
            String selfAgentId,
            Vertex outVertex,
            String outHost,
            int elapsed, boolean isError
    );

    /**
     * Calling MySQL from Tomcat generates the following message for the in(MySQL) :<br/>
     * MySQL_DB_ID (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080] <br/>
     * <br/><br/>
     * The following message is generated for the out(Tomcat) :<br/>
     * emeroad-app (TOMCAT) -> MySQL (MYSQL)[10.25.141.69:3306]
     *
     * @param inVertex  inVertex
     * @param outVertex outVertex
     * @param outHost   outHost
     * @param elapsed   elapsed
     * @param isError   isError
     */
    void updateInLink(
            long requestTime,
            Vertex inVertex,
            Vertex outVertex,
            String outHost,
            int elapsed, boolean isError
    );

    void updateResponseTime(
            long requestTime,
            Vertex appVertex,
            String agentId,
            int elapsed, boolean isError
    );

    void updateAgentState(
            long requestTime,
            String outApplicationName,
            ServiceType outServiceType,
            String outAgentId
    );
}
