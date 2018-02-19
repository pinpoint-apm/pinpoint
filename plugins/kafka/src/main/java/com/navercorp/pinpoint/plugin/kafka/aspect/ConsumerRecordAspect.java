package com.navercorp.pinpoint.plugin.kafka.aspect;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Aspect
public abstract class ConsumerRecordAspect<K, V> extends ConsumerRecord {

    public ConsumerRecordAspect() {
        super("topic", 0, 0, null, null);
    }

    @PointCut
    public V value() {
        String transactionId = null, spanID = null, parentSpanID = null, parentApplicationName = null, parentApplicationType = null, flags = null;
        for (org.apache.kafka.common.header.Header header : headers().toArray()) {
            if (header.key().equals(Header.HTTP_TRACE_ID.toString())) {
                transactionId = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_SPAN_ID.toString())) {
                parentSpanID = new String(header.value());
            } else if (header.key().equals(Header.HTTP_SPAN_ID.toString())) {
                spanID = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_NAME.toString())) {
                parentApplicationName = new String(header.value());
            } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_TYPE.toString())) {
                parentApplicationType = new String(header.value());
            } else if (header.key().equals(Header.HTTP_FLAGS.toString())) {
                flags = new String(header.value());
            }
        }

        if (transactionId != null && parentSpanID != null && spanID != null && parentApplicationName != null && parentApplicationType != null && flags != null) {
            byte[] pinpointHeader = new StringBuffer().append(KafkaConstants.PINPOINT_HEADER_PREFIX)
                    .append(transactionId).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(parentSpanID).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(spanID).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(parentApplicationName).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(parentApplicationType).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(flags).append(KafkaConstants.PINPOINT_HEADER_DELIMITIER)
                    .append(KafkaConstants.PINPOINT_HEADER_POSTFIX).toString().getBytes();
            byte[] valueIncludePinpointHeader = new byte[pinpointHeader.length + ((byte[]) __value()).length];
            System.arraycopy(pinpointHeader, 0, valueIncludePinpointHeader, 0, pinpointHeader.length);
            System.arraycopy(__value(), 0, valueIncludePinpointHeader, pinpointHeader.length, ((byte[]) __value()).length);
            return (V) valueIncludePinpointHeader;
        }
        return __value();
    }

    @JointPoint
    abstract V __value();
}
