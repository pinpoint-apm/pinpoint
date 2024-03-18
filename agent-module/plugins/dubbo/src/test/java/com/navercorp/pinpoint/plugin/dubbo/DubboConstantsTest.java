package com.navercorp.pinpoint.plugin.dubbo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DubboConstantsTest {

    @Test
    public void test() {

        Assertions.assertEquals(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE.getCode(), 1110);
        Assertions.assertEquals(DubboConstants.DUBBO_CONSUMER_SERVICE_TYPE.getCode(), 9110);
        Assertions.assertEquals(DubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE.getCode(), 9111);
        Assertions.assertEquals(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY.getCode(), 90);
        Assertions.assertEquals(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY.getCode(), 91);
        Assertions.assertEquals(DubboConstants.DUBBO_RPC_ANNOTATION_KEY.getCode(), 92);

        Assertions.assertEquals(DubboConstants.META_DO_NOT_TRACE, "_DUBBO_DO_NOT_TRACE");
        Assertions.assertEquals(DubboConstants.META_TRANSACTION_ID, "_DUBBO_TRASACTION_ID");
        Assertions.assertEquals(DubboConstants.META_SPAN_ID, "_DUBBO_SPAN_ID");
        Assertions.assertEquals(DubboConstants.META_PARENT_SPAN_ID, "_DUBBO_PARENT_SPAN_ID");
        Assertions.assertEquals(DubboConstants.META_PARENT_APPLICATION_NAME, "_DUBBO_PARENT_APPLICATION_NAME");
        Assertions.assertEquals(DubboConstants.META_PARENT_APPLICATION_TYPE, "_DUBBO_PARENT_APPLICATION_TYPE");
        Assertions.assertEquals(DubboConstants.META_FLAGS, "_DUBBO_FLAGS");
        Assertions.assertEquals(DubboConstants.MONITOR_SERVICE_FQCN, "com.alibaba.dubbo.monitor.MonitorService");

    }
}