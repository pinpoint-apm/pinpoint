package com.navercorp.pinpoint.common.server.bo;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SpanEventComparator {

    public static final Comparator<SpanEventBo> INSTANCE = Comparator.comparingInt(SpanEventBo::getSequence);

}
