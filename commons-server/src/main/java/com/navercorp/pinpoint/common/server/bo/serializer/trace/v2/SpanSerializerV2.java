package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;


import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACE_V2_CF_SPAN;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanSerializerV2 implements HbaseSerializer<SpanBo, Put> {


    @Autowired
    private SpanEncoder spanEncoder;

    public SpanSerializerV2() {
    }


    @Override
    public void serialize(SpanBo spanBo, Put put, SerializationContext context) {

        final SpanEncodingContext<SpanBo> encodingContext = new SpanEncodingContext<SpanBo>(spanBo);

        ByteBuffer qualifier = spanEncoder.encodeSpanQualifier(encodingContext);
        ByteBuffer columnValue = spanEncoder.encodeSpanColumnValue(encodingContext);

        long acceptedTime = put.getTimeStamp();
        put.addColumn(TRACE_V2_CF_SPAN, qualifier, acceptedTime, columnValue);
    }



}
