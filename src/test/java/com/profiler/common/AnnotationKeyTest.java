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

//    @Test
    public void intSize() {
//        2147483647
        System.out.println(Integer.MAX_VALUE);
//        -2147483648
        System.out.println(Integer.MIN_VALUE);
    }
}
