package com.navercorp.pinpoint.plugin.hbase.interceptor.data;

public class UnsupportedSizeProvider implements DataSizeProvider {
    @Override
    public int getDataSize(Object param) {
        return 0;
    }
}
