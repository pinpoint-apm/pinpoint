package com.navercorp.pinpoint.common.server.bo;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpanEventComparator implements Comparator<SpanEventBo> {

    public static final SpanEventComparator INSTANCE = new SpanEventComparator();

    @Override
    public int compare(SpanEventBo o1, SpanEventBo o2) {
        final int sequenceCompare = Short.compare(o1.getSequence(), o2.getSequence());
        if (sequenceCompare != 0) {
            return sequenceCompare;
        }
        final int asyncId1 = o1.getAsyncId();
        final int asyncId2 = o2.getAsyncId();
        final int asyncIdCompare = Integer.compare(asyncId1, asyncId2);
        if (asyncIdCompare != 0) {
//                bug Comparison method violates its general contract!
//                TODO temporary fix
//                if (asyncId1 == -1) {
//                    return -1;
//                }
//                if (asyncId2 == -1) {
//                    return -1;
//                }
            return asyncIdCompare;
        }
        return Integer.compare(o1.getAsyncSequence(), o2.getAsyncSequence());
    }
}
