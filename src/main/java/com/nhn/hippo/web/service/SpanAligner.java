package com.nhn.hippo.web.service;

import com.profiler.common.dto.thrift.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class SpanAligner {

    public static final Long SPAN_ROOT = (long) 0;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Span> spans;

    private Map<Long, Span> spanIdMap;
    private Map<Long, List<Span>> parentSpanIdMap;

    private int depth = 0;

    private static final Comparator<Span> timeComparator = new Comparator<Span>() {
        @Override
        public int compare(Span o1, Span o2) {
            long o1Timestamp = o1.getTimestamp();
            long o2Timestamp = o2.getTimestamp();
            if(o1Timestamp > o2Timestamp) {
                return 1;
            }
            if(o1Timestamp == o2Timestamp) {
                return 0;
            }
            return -1;
        }
    };

    public SpanAligner(List<Span> spans) {
        this.spans = spans;
    }

    List<SpanAlign> sort() {
        buildIndex();

        List<SpanAlign> result = new ArrayList<SpanAlign>(spans.size());

        Span root = findRoot();

        result.add(new SpanAlign(0, root));
        logger.debug("find root {}", root);
        List<Span> next = nextSpan(root);
        doNext(next, result);
        return result;
    }

    public void buildIndex() {
        Map<Long, Span> spanIdMap = new HashMap<Long, Span>();
        Map<Long, List<Span>> parentSpanIdMap = new HashMap<Long, List<Span>>();

        for(Span span :spans) {
            Span spanId = spanIdMap.put(span.getSpanID(), span);
            if(spanId != null) {
                // span id 중복체크
                logger.info("duplicated spanId. {}", spans);
                throw new IllegalStateException("duplicated spanId.");
            }

            long parentSpanId = span.getParentSpanId();
            List<Span> spanList = parentSpanIdMap.get(parentSpanId);
            if(spanList != null) {
                spanList.add(span);
            } else {
                LinkedList<Span> newSpanList = new LinkedList<Span>();
                newSpanList.add(span);
                parentSpanIdMap.put(parentSpanId, newSpanList);
            }
        }
        this.depth = 0;
        this.spanIdMap = spanIdMap;
        this.parentSpanIdMap = parentSpanIdMap;
    }

    private void doNext(List<Span> spans, List<SpanAlign> result) {
        if (spans == null) {
            return;
        }
        depth++;
        try {
            for (Span next : spans) {
                if(logger.isDebugEnabled()) {
                    logger.debug("{} {} next {}", new Object[]{ getSpace(), depth, next});
                }

                result.add(new SpanAlign(depth, next));

                List<Span> nextSpan = nextSpan(next);
                doNext(nextSpan, result);
            }
        } finally {
            depth--;
        }
    }

    private String getSpace() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }


    private List<Span> nextSpan(Span parent) {
        List<Span> child = this.parentSpanIdMap.get(parent.getSpanID());
        if (child == null) {
            return null;
        }
        this.parentSpanIdMap.remove(parent.getSpanID());
        // 동일 레벨은 시간순으로 정렬.
        Collections.sort(child, timeComparator);
        return child;
    }


    private Span findRoot() {
        Span root = this.spanIdMap.get(SPAN_ROOT);
        if (root == null) {
            logger.info("root span not found. {}", spans);
            throw new IllegalStateException("root span not found");
        }
        if (root.getParentSpanId() == -1) {
            logger.info("invalid root span. parentSpanId not -1 {}", root);
        }
        return root;
    }
}
