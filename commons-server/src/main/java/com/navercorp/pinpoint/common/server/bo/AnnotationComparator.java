package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.server.util.IntegerUtils;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationComparator implements Comparator<AnnotationBo> {

    public static final AnnotationComparator INSTANCE = new AnnotationComparator();

    @Override
    public int compare(AnnotationBo o1, AnnotationBo o2) {
        return IntegerUtils.compare(o1.getKey(), o2.getKey());
    }

}
