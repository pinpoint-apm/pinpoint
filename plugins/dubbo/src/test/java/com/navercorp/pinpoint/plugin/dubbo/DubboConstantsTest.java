package com.navercorp.pinpoint.plugin.dubbo;

import org.junit.Assert;
import org.junit.Test;

public class DubboConstantsTest {

    @Test
    public void test() {

        Assert.assertEquals(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE.getCode(), 1110);
        Assert.assertEquals(DubboConstants.DUBBO_CONSUMER_SERVICE_TYPE.getCode(), 9110);
        Assert.assertEquals(DubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE.getCode(), 9111);
        Assert.assertEquals(DubboConstants.DUBBO_ARGS_ANNOTATION_KEY.getCode(), 90);
        Assert.assertEquals(DubboConstants.DUBBO_RESULT_ANNOTATION_KEY.getCode(), 91);
        Assert.assertEquals(DubboConstants.DUBBO_RPC_ANNOTATION_KEY.getCode(), 92);

        Assert.assertEquals(DubboConstants.META_DO_NOT_TRACE, "_DUBBO_DO_NOT_TRACE");
        Assert.assertEquals(DubboConstants.META_TRANSACTION_ID, "_DUBBO_TRASACTION_ID");
        Assert.assertEquals(DubboConstants.META_SPAN_ID, "_DUBBO_SPAN_ID");
        Assert.assertEquals(DubboConstants.META_PARENT_SPAN_ID, "_DUBBO_PARENT_SPAN_ID");
        Assert.assertEquals(DubboConstants.META_PARENT_APPLICATION_NAME, "_DUBBO_PARENT_APPLICATION_NAME");
        Assert.assertEquals(DubboConstants.META_PARENT_APPLICATION_TYPE, "_DUBBO_PARENT_APPLICATION_TYPE");
        Assert.assertEquals(DubboConstants.META_FLAGS, "_DUBBO_FLAGS");
        Assert.assertEquals(DubboConstants.MONITOR_SERVICE_FQCN, "com.alibaba.dubbo.monitor.MonitorService");

    }
}