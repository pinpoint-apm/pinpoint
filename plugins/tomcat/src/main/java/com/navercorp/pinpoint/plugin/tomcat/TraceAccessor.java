package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.context.Trace;

public interface TraceAccessor {
    public void _$PINPOINT$_setTrace(Trace trace);
    public Trace _$PINPOINT$_getTrace();
}