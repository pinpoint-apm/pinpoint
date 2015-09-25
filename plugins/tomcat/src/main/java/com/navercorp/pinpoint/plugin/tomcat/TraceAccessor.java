package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.context.Trace;

public interface TraceAccessor {
    void _$PINPOINT$_setTrace(Trace trace);
    Trace _$PINPOINT$_getTrace();
}