package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.server.bo.PassiveSpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.HbaseSerializer;
import com.navercorp.pinpoint.common.server.bo.serializer.SerializationContext;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;


import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACE_V2_CF_PASSIVE_SPAN;

/**
 * @author Peter Chen
 */
@Component
public class PassiveSpanSerializerV2 implements HbaseSerializer<PassiveSpanBo, Put> {


    @Autowired
    private SpanEncoder spanEncoder;

    public PassiveSpanSerializerV2() {
    }


    @Override
    public void serialize(PassiveSpanBo passiveSpanBo, Put put, SerializationContext context) {

        final SpanEncodingContext<PassiveSpanBo> encodingContext = new SpanEncodingContext<>(passiveSpanBo);

        ByteBuffer qualifier = spanEncoder.encodePassiveSpanQualifier(encodingContext);
        ByteBuffer columnValue = spanEncoder.encodePassiveSpanColumnValue(encodingContext);

        long acceptedTime = put.getTimeStamp();
        put.addColumn(TRACE_V2_CF_PASSIVE_SPAN, qualifier, acceptedTime, columnValue);
    }



}
