package com.navercorp.pinpoint.test.plugin.classloader.predicates;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IsPinpointPackageTest {

    @Test
    void test1() {
        IsPinpointPackage filter = new IsPinpointPackage();

        Assertions.assertTrue(filter.test("com.navercorp.pinpoint.bootstrap."));

        Assertions.assertFalse(filter.test("com.apache.commons.lang3"));
    }
}