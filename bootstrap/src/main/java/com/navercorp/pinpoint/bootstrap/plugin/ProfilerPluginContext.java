package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

public class ProfilerPluginContext {
    private final ByteCodeInstrumentor instrumentor;
    private final TraceContext traceContext;
    
    public ProfilerPluginContext(ByteCodeInstrumentor instrumentor, TraceContext traceContext) {
        this.instrumentor = instrumentor;
        this.traceContext = traceContext;
    }

    public ClassEditorBuilder newClassEditorBuilder() {
        return new ClassEditorBuilder(instrumentor, traceContext); 
    }
    
    public ProfilerConfig getConfig() {
        return traceContext.getProfilerConfig();
    }
    
}
