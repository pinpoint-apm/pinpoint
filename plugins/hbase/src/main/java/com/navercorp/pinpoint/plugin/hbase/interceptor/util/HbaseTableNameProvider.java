package com.navercorp.pinpoint.plugin.hbase.interceptor.util;

public interface HbaseTableNameProvider {
    String getName(Object target);
}
