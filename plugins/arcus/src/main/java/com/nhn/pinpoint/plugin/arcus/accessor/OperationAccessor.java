package com.nhn.pinpoint.plugin.arcus.accessor;

import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

public interface OperationAccessor extends TraceValue {
    public Operation __getOperation();
    public void __setOperation(Operation operation);
}
