package com.nhn.hippo.web.service;

import com.nhn.hippo.web.dao.TraceDao;
import com.profiler.common.dto.thrift.Span;
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
        List<Span> spans = traceDao.readSpan(id);
        if (spans == null) {
            return Collections.emptyList();
        }
        List<SpanAlign> order = order(spans);

        if(order.size() != spans.size()) {
            logger.info("span node not complete! ");
        }
        return order;

    }

    private List<SpanAlign> order(List<Span> spans) {
        SpanAligner spanAligner = new SpanAligner(spans);
        return spanAligner.sort();
    }
}
