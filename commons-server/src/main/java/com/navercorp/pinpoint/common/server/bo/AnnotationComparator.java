package com.navercorp.pinpoint.common.server.bo;

import java.util.Comparator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class AnnotationComparator {

    public static final Comparator<AnnotationBo> INSTANCE = Comparator.comparingInt(AnnotationBo::getKey);

}
