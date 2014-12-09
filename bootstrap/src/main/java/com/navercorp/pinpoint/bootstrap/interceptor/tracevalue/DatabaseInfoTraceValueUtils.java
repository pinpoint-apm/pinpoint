package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;

/**
 * @author emeroad
 */
public class DatabaseInfoTraceValueUtils {

    public static DatabaseInfo __getTraceDatabaseInfo(Object target, DatabaseInfo defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof DatabaseInfoTraceValue) {
            final DatabaseInfo databaseInfo = ((DatabaseInfoTraceValue) target).__getTraceDatabaseInfo();
            if (databaseInfo == null) {
                return defaultValue;
            }
            return databaseInfo;
        }
        return defaultValue;
    }

    public static void __setTraceDatabaseInfo(Object target, DatabaseInfo databaseInfo) {
        if (target == null) {
            return;
        }
        if (target instanceof DatabaseInfoTraceValue) {
            ((DatabaseInfoTraceValue) target).__setTraceDatabaseInfo(databaseInfo);
        }
    }
}
