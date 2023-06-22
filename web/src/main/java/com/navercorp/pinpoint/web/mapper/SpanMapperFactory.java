package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.trace.v2.SpanDecoderV0;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


@Component
public class SpanMapperFactory {

    private final RowKeyDecoder<TransactionId> rowKeyDecoder;

    private final int stringCacheSize;

    private final RowMapper<List<SpanBo>> mapper;

    private final SpanDecoder spanDecoder = new SpanDecoderV0();

    public SpanMapperFactory(@Qualifier("traceRowKeyDecoderV2") RowKeyDecoder<TransactionId> rowKeyDecoder,
                             @Value("${web.hbase.mapper.cache.string.size:-1}") int stringCacheSize) {
        this.rowKeyDecoder = Objects.requireNonNull(rowKeyDecoder, "rowKeyDecoder");
        this.stringCacheSize = stringCacheSize;

        this.mapper = wrap(new SpanMapperV2(rowKeyDecoder, stringCacheSize));
    }

    public RowMapper<List<SpanBo>> getSpanMapper() {
        return mapper;
    }

    private RowMapper<List<SpanBo>> wrap(RowMapper<List<SpanBo>> spanMapperV2) {
        final Logger logger = LogManager.getLogger(spanMapperV2.getClass());
        if (logger.isDebugEnabled()) {
            return CellTraceMapper.wrap(spanMapperV2);
        } else {
            return spanMapperV2;
        }
    }

    public RowMapper<List<SpanBo>> getSpanMapper(Predicate<SpanBo> spanFilter) {
        if (spanFilter == null) {
            return getSpanMapper();
        }

        final SpanDecoder targetSpanDecoder = new FilteringSpanDecoder(spanDecoder, spanFilter);
        return new SpanMapperV2(rowKeyDecoder, targetSpanDecoder, stringCacheSize);
    }
}
