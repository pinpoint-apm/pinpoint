package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractor;

public interface ParameterExtractorFactory {
    public ParameterExtractor get(InstrumentClass targetClass, MethodInfo targetMethod);
}
