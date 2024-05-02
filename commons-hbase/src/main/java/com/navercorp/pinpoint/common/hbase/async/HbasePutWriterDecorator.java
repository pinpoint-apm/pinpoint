package com.navercorp.pinpoint.common.hbase.async;

public interface HbasePutWriterDecorator {
    HbasePutWriter decorator(HbasePutWriter hbasePutWriter);
}
