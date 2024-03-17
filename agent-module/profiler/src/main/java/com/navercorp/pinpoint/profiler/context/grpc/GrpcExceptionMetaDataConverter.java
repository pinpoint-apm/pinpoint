package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionMetaData;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.ExceptionMetaDataMapper;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;

public class GrpcExceptionMetaDataConverter implements MessageConverter<MetaDataType, GeneratedMessageV3> {
    private final ExceptionMetaDataMapper mapper;

    public GrpcExceptionMetaDataConverter(ExceptionMetaDataMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public GeneratedMessageV3 toMessage(MetaDataType message) {
        if (message instanceof ExceptionMetaData) {
            final ExceptionMetaData exceptionMetaData = (ExceptionMetaData) message;
            return mapper.toProto(exceptionMetaData);
        }
        return null;
    }
}
