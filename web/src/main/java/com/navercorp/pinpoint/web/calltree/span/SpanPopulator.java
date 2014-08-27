package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author emeroad
 */
@Deprecated
public class SpanPopulator {
    private List<SpanAlign> list;
    private int index = 0;

    public SpanPopulator(List<SpanAlign> list) {
        this.list = list;
    }

    public List<SpanAlign> populateSpanEvent() {
        if (list.size() == 0) {
            return Collections.emptyList();
        }
        List<SpanAlign> populatedList = new ArrayList<SpanAlign>();

        while (index < list.size()) {
            populatedSpan(populatedList);
        }

        return populatedList;
    }

    private void populatedSpan(List<SpanAlign> populatedList) {
        SpanAlign spanAlign = list.get(index);
        SpanBo span = spanAlign.getSpanBo();
        populatedList.add(spanAlign);
        long startTime = span.getStartTime();
        List<SpanEventBo> spanEventBoList = sortSpanEvent(span);
        for (SpanEventBo spanEventBo : spanEventBoList) {
            long subStartTime = startTime + spanEventBo.getStartElapsed();
            long nextSpanStartTime = getNextSpanStartTime();
            if (subStartTime <= nextSpanStartTime) {
                SpanAlign SpanEventAlign = new SpanAlign(spanAlign.getDepth(), span, spanEventBo);
                SpanEventAlign.setSpan(false);
                populatedList.add(SpanEventAlign);
            } else {
                if (nextSpanStartTime == Long.MAX_VALUE) {
                    return;
                }
                index++;
                populatedSpan(populatedList);
            }
        }
        index++;
    }

    public long getNextSpanStartTime() {
        int nextIndex = index + 1;
        if (nextIndex >= list.size()) {
            return Long.MAX_VALUE;
        }
        return list.get(nextIndex).getSpanBo().getStartTime();
    }

    private List<SpanEventBo> sortSpanEvent(SpanBo span) {
        List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (spanEventBoList == null) {
            return Collections.emptyList();
        }
        Collections.sort(spanEventBoList, new Comparator<SpanEventBo>() {
            @Override
            public int compare(SpanEventBo o1, SpanEventBo o2) {
                long o1Timestamp = o1.getSequence();
                long o2Timestamp = o2.getSequence();
                if (o1Timestamp > o2Timestamp) {
                    return 1;
                }
                if (o1Timestamp == o2Timestamp) {
                    return 0;
                }
                return -1;
            }
        });
        return spanEventBoList;
    }
}
