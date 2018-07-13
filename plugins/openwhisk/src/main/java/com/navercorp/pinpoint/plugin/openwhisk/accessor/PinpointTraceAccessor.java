package com.navercorp.pinpoint.plugin.openwhisk.accessor;

import com.navercorp.pinpoint.bootstrap.context.Trace;

/**
 * @author upgle (Seonghyun, Oh)
 */
public interface PinpointTraceAccessor {
    void _$PINPOINT$_setPinpointTrace(Trace test);
    Trace _$PINPOINT$_getPinpointTrace();
}
