package com.navercorp.pinpoint.grpc.client.retry;

import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import io.grpc.Metadata;

import java.util.Objects;

public class RetryHeaderFactory implements HeaderFactory {

    private final HeaderFactory headerFactory;

    public RetryHeaderFactory(HeaderFactory headerFactory) {
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");
    }

    @Override
    public Metadata newHeader() {
        Metadata metadata = headerFactory.newHeader();
        metadata.put(Header.GRPC_BUILT_IN_RETRY, Boolean.TRUE.toString());
        return metadata;
    }
}
