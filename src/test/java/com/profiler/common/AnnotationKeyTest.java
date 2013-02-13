package com.profiler.common;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class AnnotationKeyTest {

    @Test
    public void getCode() {

        AnnotationKey annotationKey = AnnotationKey.findAnnotationKey(AnnotationKey.API.getCode());
        Assert.assertEquals(annotationKey, AnnotationKey.API);
    }
}
