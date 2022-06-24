package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class AnnotationUtilsTest {

    @Test
    public void findApiAnnotation() {
        AnnotationBo annotationBo = new AnnotationBo(AnnotationKey.API.getCode(), "a");
        String value = AnnotationUtils.findApiAnnotation(Collections.singletonList(annotationBo));
        Assertions.assertEquals("a", value);
    }

    @Test
    public void findApiAnnotation_invalidType() {
        AnnotationBo annotationBo = new AnnotationBo(AnnotationKey.API.getCode(), 1);
        String value = AnnotationUtils.findApiAnnotation(Collections.singletonList(annotationBo));
        Assertions.assertNull(null, value);
    }
}