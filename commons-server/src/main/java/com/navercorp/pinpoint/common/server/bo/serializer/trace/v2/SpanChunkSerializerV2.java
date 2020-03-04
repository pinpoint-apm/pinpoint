package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanChunkSerializerV2 implements HbaseSerializer<SpanChunkBo, Put> {

    @Autowired
    private SpanEncoder spanEncoder;

    @Override
    public void serialize(SpanChunkBo spanChunkBo, Put put, SerializationContext context) {
        if (spanChunkBo == null) {
            throw new NullPointerException("spanChunkBo");
        }

        SpanEncodingContext<SpanChunkBo> encodingContext = new SpanEncodingContext<SpanChunkBo>(spanChunkBo);

        ByteBuffer qualifier = spanEncoder.encodeSpanChunkQualifier(encodingContext);
        ByteBuffer columnValue = spanEncoder.encodeSpanChunkColumnValue(encodingContext);

        long acceptedTime = put.getTimeStamp();
        put.addColumn(HbaseColumnFamily.TRACE_V2_SPAN.getName(), qualifier, acceptedTime, columnValue);
    }

}
