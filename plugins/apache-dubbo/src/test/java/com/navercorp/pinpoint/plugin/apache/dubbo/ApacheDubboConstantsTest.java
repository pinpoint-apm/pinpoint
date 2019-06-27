package com.navercorp.pinpoint.plugin.apache.dubbo;

import org.junit.Assert;
import org.junit.Test;

public class ApacheDubboConstantsTest {

    @Test
    public void test() {
        Assert.assertEquals(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE.getCode(), 1999);
        Assert.assertEquals(ApacheDubboConstants.DUBBO_CONSUMER_SERVICE_TYPE.getCode(), 9997);
        Assert.assertEquals(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE.getCode(), 9999);
        Assert.assertEquals(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY.getCode(), 997);
        Assert.assertEquals(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY.getCode(), 998);
        Assert.assertEquals(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY.getCode(), 999);

        Assert.assertEquals(ApacheDubboConstants.META_DO_NOT_TRACE, "_DUBBO_DO_NOT_TRACE");
        Assert.assertEquals(ApacheDubboConstants.META_TRANSACTION_ID, "_DUBBO_TRASACTION_ID");
        Assert.assertEquals(ApacheDubboConstants.META_SPAN_ID, "_DUBBO_SPAN_ID");
        Assert.assertEquals(ApacheDubboConstants.META_PARENT_SPAN_ID, "_DUBBO_PARENT_SPAN_ID");
        Assert.assertEquals(ApacheDubboConstants.META_PARENT_APPLICATION_NAME, "_DUBBO_PARENT_APPLICATION_NAME");
        Assert.assertEquals(ApacheDubboConstants.META_PARENT_APPLICATION_TYPE, "_DUBBO_PARENT_APPLICATION_TYPE");
        Assert.assertEquals(ApacheDubboConstants.META_FLAGS, "_DUBBO_FLAGS");
        Assert.assertEquals(ApacheDubboConstants.MONITOR_SERVICE_FQCN, "org.apache.dubbo.monitor.MonitorService");
    }
}