package com.nhn.hippo.web.calltree.span;

import com.profiler.common.dto.thrift.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class SpanAligner {

    public static final Long SPAN_ROOT = -1L;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Span> spans;

    //    private Map<Long, Span> spanIdMap;
    private Map<Long, List<Span>> parentSpanIdMap;

    private int depth = 0;

    private static final Comparator<Span> timeComparator = new Comparator<Span>() {
        @Override
        public int compare(Span o1, Span o2) {
            long o1Timestamp = o1.getTimestamp();
            long o2Timestamp = o2.getTimestamp();
            if (o1Timestamp > o2Timestamp) {
                return 1;
            }
            if (o1Timestamp == o2Timestamp) {
                return 0;
            }
            return -1;
        }
    };

    public SpanAligner(List<Span> spans) {
        this.spans = spans;
    }

    public List<SpanAlign> sort() {
        buildIndex();

        List<SpanAlign> result = new ArrayList<SpanAlign>(spans.size());

        Span root = findRoot();
        logger.debug("find root {}", root);
        result.add(new SpanAlign(0, root));

        List<Span> next = nextSpan(root);
        doNext(next, result);
        return result;
    }

    public void buildIndex() {
        SpanIdChecker spanIdCheck = new SpanIdChecker(spans);
        Map<Long, List<Span>> parentSpanIdMap = new HashMap<Long, List<Span>>();

        for (Span span : spans) {
            spanIdCheck.check(span);

            long parentSpanId = span.getParentSpanId();
            List<Span> spanList = parentSpanIdMap.get(parentSpanId);
            if (spanList != null) {
                spanList.add(span);
            } else {
                LinkedList<Span> newSpanList = new LinkedList<Span>();
                newSpanList.add(span);
                parentSpanIdMap.put(parentSpanId, newSpanList);
            }
        }
        this.depth = 0;
//        this.spanIdMap = spanIdMap;
        this.parentSpanIdMap = parentSpanIdMap;
    }

    private void doNext(List<Span> spans, List<SpanAlign> result) {
        if (spans == null) {
            return;
        }
        depth++;
        try {
            for (Span next : spans) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} {} next {}", new Object[]{getSpace(), depth, next});
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
        List<Span> root = this.parentSpanIdMap.get(SPAN_ROOT);
        if (root == null) {
            logger.warn("root span not found. {}", spans);
            throw new IllegalStateException("root span not found");
        }
        if (root.size() == -1) {
            logger.info("invalid root span. duplicated root span {}", root);
            throw new IllegalStateException("duplicated root span");
        }
        return root.get(0);
    }

    public static class SpanIdChecker {
        private Map<Long, Span> spanCheck = new HashMap<Long, Span>();
        private List<Span> spans;

        public SpanIdChecker(List<Span> spans) {
            this.spans = spans;
        }

        public void check(Span span) {
            Span before = spanCheck.put(span.getSpanID(), span);
            if (before != null) {
                // span id 중복체크
                deplicatedSpanIdDump(span);
                throw new IllegalStateException("duplicated spanId. id:" + span.getSpanID());
            }
        }

        private void deplicatedSpanIdDump(Span span) {
            // 중복 span dump
            Logger internalLog = LoggerFactory.getLogger(this.getClass());
            internalLog.info("duplicated spanId {}, list:{}", span, spans);
        }
    }
}
