package com.navercorp.pinpoint.collector.monitor.micrometer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BulkOperationMetricsTest {

    @Test
    void cleanupParentClass() {
        String s = BulkOperationMetrics.cleanupParentClass("test$InnerClass$Another");
        Assertions.assertEquals("test", s);

        String s2 = BulkOperationMetrics.cleanupParentClass("test$InnerClass");
        Assertions.assertEquals("test", s2);
    }

    @Test
    void cleanupParentClassWithoutInnerClass() {
        String s = BulkOperationMetrics.cleanupParentClass("test");
        Assertions.assertEquals("test", s);
    }

    @Test
    void cleanupParentClass_lambda() {
        Runnable runnable = () -> {};

        String simpleName = runnable.getClass().getSimpleName();
        String s = BulkOperationMetrics.cleanupParentClass(simpleName);

        Assertions.assertEquals("BulkOperationMetricsTest", s);
    }

}