package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.ServiceType;

import java.util.List;

/**
 *
 */
public interface DatabaseInfo {
    List<String> getHost();

    String getMultipleHost();

    String getDatabaseId();

    String getRealUrl();

    String getUrl();

    ServiceType getType();

    ServiceType getExecuteQueryType();
}
