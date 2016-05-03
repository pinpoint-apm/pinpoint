package com.navercorp.pinpoint.common.server.bo.serializer;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.AnnotationBoList;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.server.util.AcceptedTimeService;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.protobuf.generated.CellProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.navercorp.pinpoint.common.hbase.HBaseTables.TRACES_CF_TERMINALSPAN;

/**
 * @author Woonduk Kang(emeroad)
 */
@Component
public class SpanEventSerializer implements HbaseSerializer<SpanEventBo, Put> {

    private AnnotationSerializer annotationSerializer;

    private AcceptedTimeService acceptedTimeService;

    @Autowired
    public void setAnnotationSerializer(AnnotationSerializer annotationSerializer) {
        this.annotationSerializer = annotationSerializer;
    }

    @Autowired
    public void setAcceptedTimeService(AcceptedTimeService acceptedTimeService) {
        this.acceptedTimeService = acceptedTimeService;
    }

    @Override
    public void serialize(SpanEventBo spanEventBo, Put put, SerializationContext context) {

        byte[] rowId = BytesUtils.add(spanEventBo.getSpanId(), spanEventBo.getSequence(), spanEventBo.getAsyncId(), spanEventBo.getAsyncSequence());

        final byte[] value = writeValue(spanEventBo);
        final long acceptedTime = acceptedTimeService.getAcceptedTime();

        put.addColumn(TRACES_CF_TERMINALSPAN, rowId, acceptedTime, value);

    }

    public byte[] writeValue(SpanEventBo spanEventBo) {
        final Buffer buffer = new AutomaticBuffer(512);

        buffer.put(spanEventBo.getVersion());

        buffer.putPrefixedString(spanEventBo.getAgentId());
        buffer.putPrefixedString(spanEventBo.getApplicationId());
        buffer.putVar(spanEventBo.getAgentStartTime());

        buffer.putVar(spanEventBo.getStartElapsed());
        buffer.putVar(spanEventBo.getEndElapsed());

        // don't need to put sequence because it is set at Qualifier
        // buffer.put(sequence);

        buffer.putPrefixedString(spanEventBo.getRpc());
        buffer.put(spanEventBo.getServiceType());
        buffer.putPrefixedString(spanEventBo.getEndPoint());
        buffer.putPrefixedString(spanEventBo.getDestinationId());
        buffer.putSVar(spanEventBo.getApiId());

        buffer.putSVar(spanEventBo.getDepth());
        buffer.put(spanEventBo.getNextSpanId());

        if (spanEventBo.hasException()) {
            buffer.put(true);
            buffer.putSVar(spanEventBo.getExceptionId());
            buffer.putPrefixedString(spanEventBo.getExceptionMessage());
        } else {
            buffer.put(false);
        }
        final List<AnnotationBo> annotationBoList = spanEventBo.getAnnotationBoList();
        this.annotationSerializer.writeAnnotationList(annotationBoList, buffer);

        buffer.putSVar(spanEventBo.getNextAsyncId());

        return buffer.getBuffer();
    }

}
