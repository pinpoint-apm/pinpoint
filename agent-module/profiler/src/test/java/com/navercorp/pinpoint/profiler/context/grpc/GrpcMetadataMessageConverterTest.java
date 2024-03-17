package com.navercorp.pinpoint.profiler.context.grpc;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapper;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapperImpl;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaData;
import com.navercorp.pinpoint.profiler.metadata.SqlUidMetaData;
import com.navercorp.pinpoint.profiler.metadata.StringMetaData;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static com.navercorp.pinpoint.profiler.context.grpc.MapperTestUtil.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
class GrpcMetadataMessageConverterTest {

    Random random = new Random();

    MetaDataMapper mapper = new MetaDataMapperImpl();
    GrpcMetadataMessageConverter converter = new GrpcMetadataMessageConverter(mapper);

    @Test
    void testSqlMetaData() {
        SqlMetaData sqlMetaData = new SqlMetaData(random.nextInt(), randomString());
        PSqlMetaData pSqlMetaData = (PSqlMetaData) converter.toMessage(sqlMetaData);

        assertEquals(sqlMetaData.getSql(), pSqlMetaData.getSql());
        assertEquals(sqlMetaData.getSqlId(), pSqlMetaData.getSqlId());
    }

    @Test
    void testSqlUidMetaData() {
        SqlUidMetaData sqlUidMetaData = new SqlUidMetaData(UUID.randomUUID().toString().getBytes(), randomString());
        PSqlUidMetaData pSqlUidMetaData = (PSqlUidMetaData) converter.toMessage(sqlUidMetaData);

        assertEquals(ByteString.copyFrom(sqlUidMetaData.getSqlUid()), pSqlUidMetaData.getSqlUid());
        assertEquals(sqlUidMetaData.getSql(), pSqlUidMetaData.getSql());
    }

    @Test
    void testApiMetaData() {
        ApiMetaData apiMetaData = new ApiMetaData(random.nextInt(), randomString(), random.nextInt(), random.nextInt());
        PApiMetaData pApiMetaData = (PApiMetaData) converter.toMessage(apiMetaData);

        assertEquals(apiMetaData.getApiId(), pApiMetaData.getApiId());
        assertEquals(apiMetaData.getApiInfo(), pApiMetaData.getApiInfo());
        assertEquals(apiMetaData.getLine(), pApiMetaData.getLine());
        assertEquals(apiMetaData.getType(), pApiMetaData.getType());
    }

    @Test
    void testStringMetaData() {
        StringMetaData stringMetaData = new StringMetaData(random.nextInt(), randomString());
        PStringMetaData pStringMetaData = (PStringMetaData) converter.toMessage(stringMetaData);
        assertEquals(stringMetaData.getStringId(), pStringMetaData.getStringId());
        assertEquals(stringMetaData.getStringValue(), pStringMetaData.getStringValue());
    }
}