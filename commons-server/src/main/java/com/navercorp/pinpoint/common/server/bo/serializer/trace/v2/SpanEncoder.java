package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;

import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface SpanEncoder {

    byte TYPE_SPAN = 0;
    byte TYPE_SPAN_CHUNK = 1;

    // reserved
    byte TYPE_PASSIVE_SPAN = 4;
    byte TYPE_INDEX = 7;

    Comparator<SpanEventBo> SPAN_EVENT_SEQUENCE_COMPARATOR = new Comparator<SpanEventBo>() {
        @Override
        public int compare(SpanEventBo o1, SpanEventBo o2) {
            final int sequenceCompare = Short.compare(o1.getSequence(), o2.getSequence());
            if (sequenceCompare != 0) {
                return sequenceCompare;
            }
            final int asyncId1 = o1.getAsyncId();
            final int asyncId2 = o2.getAsyncId();
            final int asyncIdCompare = Integer.compare(asyncId1, asyncId2);
            if (asyncIdCompare != 0) {
//                bug Comparison method violates its general contract!
//                TODO temporary fix
//                if (asyncId1 == -1) {
//                    return -1;
//                }
//                if (asyncId2 == -1) {
//                    return -1;
//                }
                return asyncIdCompare;
            }
            return Integer.compare(o1.getAsyncSequence(), o2.getAsyncSequence());
        }
    };

    Comparator<AnnotationBo> ANNOTATION_COMPARATOR = new Comparator<AnnotationBo>() {
        @Override
        public int compare(AnnotationBo o1, AnnotationBo o2) {
            return Integer.compare(o1.getKey(), o2.getKey());
        }
    };

    ByteBuffer encodeSpanQualifier(SpanEncodingContext<SpanBo> encodingContext);

    ByteBuffer encodeSpanColumnValue(SpanEncodingContext<SpanBo> encodingContext);


    ByteBuffer encodeSpanChunkQualifier(SpanEncodingContext<SpanChunkBo> encodingContext);

    ByteBuffer encodeSpanChunkColumnValue(SpanEncodingContext<SpanChunkBo> encodingContext);
}
