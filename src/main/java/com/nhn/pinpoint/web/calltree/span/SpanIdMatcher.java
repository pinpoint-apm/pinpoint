package com.nhn.pinpoint.web.calltree.span;

import com.nhn.pinpoint.common.bo.SpanBo;

import java.util.List;

/**
 *
 */
public class SpanIdMatcher {
    private List<SpanBo> nextSpanBoList;

    public SpanIdMatcher(List<SpanBo> nextSpanBoList) {
        this.nextSpanBoList = nextSpanBoList;
    }

    public SpanBo executeTimeBaseMatch(long spanEventBoStartTime) {
        // 매칭 알고리즘이 있어야 함.
        return nextSpanBoList.remove(0);
    }

    public List<SpanBo> other() {
        if (nextSpanBoList.size() == 0) {
            return null;
        }
        return nextSpanBoList;
    }
}
