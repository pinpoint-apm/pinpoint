package com.nhn.hippo.web.calltree.span;

import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
@Deprecated
public class SpanPopulator {
    private List<SpanAlign> list;
    private int index = 0;

    public SpanPopulator(List<SpanAlign> list) {
        this.list = list;
    }

    public List<SpanAlign> populateSubSpan() {
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
        List<SubSpanBo> subSpanBos = sortSubSpan(span);
        for (SubSpanBo subSpanBo : subSpanBos) {
            long subStartTime = startTime + subSpanBo.getStartElapsed();
            long nextSpanStartTime = getNextSpanStartTime();
            if (subStartTime <= nextSpanStartTime) {
                SpanAlign subSpanAlign = new SpanAlign(spanAlign.getDepth(), span, subSpanBo);
                subSpanAlign.setSpan(false);
                populatedList.add(subSpanAlign);
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

    private List<SubSpanBo> sortSubSpan(SpanBo span) {
        List<SubSpanBo> subSpanList = span.getSubSpanList();
        if (subSpanList == null) {
            return Collections.emptyList();
        }
        Collections.sort(subSpanList, new Comparator<SubSpanBo>() {
            @Override
            public int compare(SubSpanBo o1, SubSpanBo o2) {
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
        return subSpanList;
    }
}
