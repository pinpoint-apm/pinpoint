package com.navercorp.pinpoint.test.rpc;

import com.google.inject.Inject;
import com.navercorp.pinpoint.common.profiler.message.DefaultResultResponse;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;

public class MockMessageConverter implements MessageConverter<Object, ResultResponse> {

    @Inject
    public MockMessageConverter() {
    }

    @Override
    public ResultResponse toMessage(Object message) {
        return new DefaultResultResponse(true, "success by mocking");
    }
}
