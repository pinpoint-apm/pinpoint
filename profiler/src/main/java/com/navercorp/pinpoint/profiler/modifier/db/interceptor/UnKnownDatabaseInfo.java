package com.nhn.pinpoint.profiler.modifier.db.interceptor;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.profiler.modifier.db.DefaultDatabaseInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class UnKnownDatabaseInfo {
    public static final DatabaseInfo INSTANCE;

    static{
        final List<String> urls = new ArrayList();
        urls.add("unknown");
        INSTANCE = new DefaultDatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, "unknown", "unknown", urls, "unknown");
    }
}
