package com.navercorp.pinpoint.collector.applicationmap.service;

import com.navercorp.pinpoint.collector.applicationmap.Vertex;
import com.navercorp.pinpoint.common.trace.ServiceType;
import jakarta.validation.constraints.NotBlank;

public interface LinkService {

    /**
     * Calling MySQL from Tomcat generates the following message for the caller(Tomcat) :<br/>
     * emeroad-app (TOMCAT) -> MySQL_DB_ID (MYSQL)[10.25.141.69:3306] <br/>
     * <br/>
     * The following message is generated for the in(MySQL) :<br/>
     * MySQL (MYSQL) <- emeroad-app (TOMCAT)[localhost:8080]
     *
     * @param outVertex  outVertex
     * @param outAgentId outAgentId
     * @param inVertex   inVertex
     * @param inHost     inHost
     * @param elapsed    elapsed
     * @param isError    isError
     */
    void updateOutLink(
            long requestTime,
            Vertex outVertex,
            @NotBlank String outAgentId,
            Vertex inVertex,
            String inHost,
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
            @NotBlank String outApplicationName,
            ServiceType outServiceType,
            @NotBlank String outAgentId
    );
}
