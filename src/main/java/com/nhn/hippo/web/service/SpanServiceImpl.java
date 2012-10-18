package com.nhn.hippo.web.service;

import com.nhn.hippo.web.calltree.span.SpanAlign;
import com.nhn.hippo.web.calltree.span.SpanAligner;
import com.nhn.hippo.web.dao.TraceDao;
import com.profiler.common.bo.SpanBo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 */
@Service
public class SpanServiceImpl implements SpanService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Override
    public List<SpanAlign> selectSpan(String uuid) {
        UUID id = UUID.fromString(uuid);
        List<SpanBo> spans = traceDao.selectSpanAndAnnotation(id);
        if (spans == null) {
            return Collections.emptyList();
        }
        List<SpanAlign> order = order(spans);
        // TODO root span not found시 row data라도 보여줘야 됨.
        if (order.size() != spans.size()) {
            // TODO 중간 노드 데이터 분실 ? 혹은 잘못된 데이터 생성?
            logger.info("span node not complete! ");
        }
        return order;

    }

    private List<SpanAlign> order(List<SpanBo> spans) {

        SpanAligner spanAligner = new SpanAligner(spans);
        return spanAligner.sort();
    }
}
