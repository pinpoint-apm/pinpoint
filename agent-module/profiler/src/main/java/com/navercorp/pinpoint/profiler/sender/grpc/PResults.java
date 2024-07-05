package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.profiler.message.DefaultResultResponse;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.grpc.trace.PResult;

public final class PResults {
    public static ResultResponse toResponse(PResult result) {
        return new DefaultResultResponse(result.getSuccess(), result.getMessage());
    }
}
