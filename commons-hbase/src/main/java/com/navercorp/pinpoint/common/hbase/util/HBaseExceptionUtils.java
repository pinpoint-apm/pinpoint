package com.navercorp.pinpoint.common.hbase.util;

import com.navercorp.pinpoint.common.hbase.HbaseSystemException;
import org.apache.hadoop.hbase.client.RetriesExhaustedWithDetailsException;

import java.util.HashSet;
import java.util.Set;

public final class HBaseExceptionUtils {
    private HBaseExceptionUtils() {
    }

    public static String getErrorHost(RetriesExhaustedWithDetailsException e) {
        final int numExceptions = e.getNumExceptions();
        Set<String> hostErrors = new HashSet<>(numExceptions);
        for (int i = 0; i < numExceptions; i++) {
            String hostnamePort = e.getHostnamePort(i);
            hostErrors.add(hostnamePort);
        }
        return hostErrors.toString();
    }

    public static <T> T rethrowHbaseException(Throwable e) {
        if (e instanceof RuntimeException ex) {
            throw ex;
        }
        if (e instanceof Error error) {
            throw error;
        }
        throw new HbaseSystemException(e);
    }
}
