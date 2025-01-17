package com.navercorp.pinpoint.profiler.test;

import com.navercorp.pinpoint.common.profiler.message.AsyncDataSender;
import com.navercorp.pinpoint.common.profiler.message.DataConsumer;
import com.navercorp.pinpoint.common.profiler.message.DefaultResultResponse;
import com.navercorp.pinpoint.common.profiler.message.ResultResponse;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AsyncDataSenderDelegator implements AsyncDataSender<MetaDataType, ResultResponse> {

    private final DataConsumer<MetaDataType> dataSender;

    private final ResultResponse success = new DefaultResultResponse(true, "success");
    private final ResultResponse failed = new DefaultResultResponse(false, "failed");

    public AsyncDataSenderDelegator(DataConsumer<MetaDataType> dataSender) {
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
    }

    @Override
    public CompletableFuture<ResultResponse> request(MetaDataType data) {
        if (this.dataSender.send(data)) {
            return CompletableFuture.completedFuture(success);
        } else {
            return CompletableFuture.completedFuture(failed);
        }
    }

    @Override
    public boolean send(MetaDataType data) {
        return this.dataSender.send(data);
    }

    @Override
    public void close() {
        // empty
    }
}
