package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SpanBoTes {

    @Test
    void hasApplicationServiceType() {
        SpanBo spanBo = new SpanBo();
        spanBo.setApplicationServiceType(10);

        Assertions.assertTrue(spanBo.hasApplicationServiceType());
    }

    @Test
    void hasApplicationServiceType_emptyApplicationServiceType0() {
        SpanBo spanBo = new SpanBo();
        spanBo.setApplicationServiceType(0);

        Assertions.assertFalse(spanBo.hasApplicationServiceType());
    }

    @Test
    void hasApplicationServiceType_emptyApplicationServiceType1() {
        SpanBo spanBo = new SpanBo();
        spanBo.setApplicationServiceType(ServiceType.UNDEFINED.getCode());

        Assertions.assertFalse(spanBo.hasApplicationServiceType());
    }
}