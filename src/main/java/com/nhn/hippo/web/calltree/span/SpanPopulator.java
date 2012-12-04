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
public class SpanPopulator {
    private List<SpanAlign> list;

    public SpanPopulator(List<SpanAlign> list) {
        this.list = list;
    }

    public List populateSubSpan() {
        List<SpanAlign> populatedList = new ArrayList<SpanAlign>();

        for (int i = 0; i < list.size(); i++) {
            populatedSpan(populatedList, i);
        }
        return populatedList;
    }

    private void populatedSpan(List<SpanAlign> populatedList, int i) {
        SpanAlign spanAlign = list.get(i);
        SpanBo span = spanAlign.getSpan();
        populatedList.add(spanAlign);
        long startTime = span.getStartTime();
        List<SubSpanBo> subSpanBos = sortSubSpan(span);
        for (SubSpanBo subSpanBo : subSpanBos) {
            long subStartTime = startTime + subSpanBo.getStartElapsed();
            long nextSpanStartTime = getNextSpanStartTime(i);
            if (subStartTime >= nextSpanStartTime) {
                SpanAlign subSpanAlign = new SpanAlign(spanAlign.getDepth(), span, subSpanBo);
                subSpanAlign.setRoot(false);
                populatedList.add(subSpanAlign);
            } else {
                populatedSpan(populatedList, i);
            }
        }
    }

    public long getNextSpanStartTime(int i) {
        if (i < list.size()) {
            return 0;
        }
        return list.get(i + 1).getSpan().getStartTime();
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
