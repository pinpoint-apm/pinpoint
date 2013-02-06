package com.profiler.common;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 */
public class AnnotationNamesTest {

    @Test
    public void getCode() {

        AnnotationNames annotationNames = AnnotationNames.findAnnotationNames(AnnotationNames.API.getCode());
        Assert.assertEquals(annotationNames, AnnotationNames.API);
    }
}
