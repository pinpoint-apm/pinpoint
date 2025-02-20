package com.navercorp.pinpoint.grpc;

import io.grpc.Metadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Header {

    Metadata.Key<String> AGENT_ID_KEY = newStringKey("agentid");
    Metadata.Key<String> AGENT_NAME_KEY = newStringKey("agentname");
    Metadata.Key<String> APPLICATION_NAME_KEY = newStringKey("applicationname");
    Metadata.Key<String> AGENT_START_TIME_KEY = newStringKey("starttime");

    // v4 header
    Metadata.Key<String> PROTOCOL_VERSION_NAME_KEY = newStringKey("protocol.version");
    Metadata.Key<String> SERVICE_NAME_KEY = newStringKey("servicename");
    Metadata.Key<String> API_KEY = newStringKey("apikey");
    // v4 header

    // optional header
    Metadata.Key<String> SOCKET_ID = newStringKey("socketid");
    Metadata.Key<String> SERVICE_TYPE_KEY = newStringKey("servicetype");
    Metadata.Key<String> SUPPORT_COMMAND_CODE = newStringKey("supportCommandCode");
    Metadata.Key<String> GRPC_BUILT_IN_RETRY = newStringKey("grpc.built-in.retry");

    String SUPPORT_COMMAND_CODE_DELIMITER = ";";

    static Metadata.Key<String> newStringKey(String s) {
        return Metadata.Key.of(s, Metadata.ASCII_STRING_MARSHALLER);
    }

    long SOCKET_ID_NOT_EXIST = -1;

    List<Integer> SUPPORT_COMMAND_CODE_LIST_NOT_EXIST = null;
    List<Integer> SUPPORT_COMMAND_CODE_LIST_PARSE_ERROR = Collections.emptyList();
    boolean DEFAULT_GRPC_BUILT_IN_RETRY = false;

    String getAgentId();

    String getAgentName();

    String getApplicationName();

    String getServiceName();

    long getAgentStartTime();

    long getSocketId();

    int getServiceType();

    List<Integer> getSupportCommandCodeList();

    boolean isGrpcBuiltInRetry();

    Object get(String key);

    Map<String, Object> getProperties();
}
