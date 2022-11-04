package com.navercorp.pinpoint.common.server.bo.stat.join;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class JoinDoubleFieldBoTest {
    @Test
    public void testAccept() {
        JoinDoubleFieldBo field1 = new JoinDoubleFieldBo(1, 1, "min1", 100, "max1");
        JoinDoubleFieldBo field2 = new JoinDoubleFieldBo(1, 2, "min2", 200, "max2");

        JoinDoubleFieldBo merge = JoinDoubleFieldBo.merge(Arrays.asList(field1, field2));
        Assertions.assertEquals(1D, merge.getMin(), 10);
        Assertions.assertEquals("min1", merge.getMinAgentId());
        Assertions.assertEquals(200D, merge.getMax(), 10);
        Assertions.assertEquals("max2", merge.getMaxAgentId());
    }
}