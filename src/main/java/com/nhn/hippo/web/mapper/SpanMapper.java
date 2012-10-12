package com.nhn.hippo.web.mapper;

import com.profiler.common.dto.thrift.Span;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;

/**
 *
 */
@Component
public class SpanMapper implements RowMapper<List<Span>> {

    private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private BinaryAnnotationDecoder binaryAnnotationDecoder = new JavaObjectDecoder();

    @Override
    public List<Span> mapRow(Result result, int rowNum) throws Exception {
        NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(COLFAM_SPAN);
        if (familyMap == null) {
            return Collections.emptyList();
        }

        List<Span> spanList = new ArrayList<Span>(familyMap.size());
        // TODO thrift 포멧이 아니고 따로 풀어서 넣어야 될거 같음.
        TDeserializer de = new TDeserializer();
        for (NavigableMap.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
            Span span = new Span();
            // spainid가 이미 value에 들어 있어서 일단 필요가 없음.
            //byte[] spanId = entry.getKey();
            de.deserialize(span, entry.getValue());
            if (binaryAnnotationDecoder != null) {
                binaryAnnotationDecoder.decode(span);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("deserailze span :{}", span);
            }
            spanList.add(span);
        }
        return spanList;
    }
}
