package com.nhn.pinpoint.plugin.arcus.accessor;

import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

public interface CacheKeyAccessor extends TraceValue {
    public String __getCacheKey();
    public void __setCacheKey(String cacheKey);
}
