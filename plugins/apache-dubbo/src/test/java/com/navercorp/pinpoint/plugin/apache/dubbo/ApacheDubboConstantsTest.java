/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.apache.dubbo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ApacheDubboConstantsTest {

    @Test
    public void test() {
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE.getCode(), 1999);
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_CONSUMER_SERVICE_TYPE.getCode(), 9997);
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_NO_STATISTICS_TYPE.getCode(), 9999);
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_ARGS_ANNOTATION_KEY.getCode(), 997);
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_RESULT_ANNOTATION_KEY.getCode(), 998);
        Assertions.assertEquals(ApacheDubboConstants.DUBBO_RPC_ANNOTATION_KEY.getCode(), 999);

        Assertions.assertEquals(ApacheDubboConstants.META_DO_NOT_TRACE, "_DUBBO_DO_NOT_TRACE");
        Assertions.assertEquals(ApacheDubboConstants.META_TRANSACTION_ID, "_DUBBO_TRASACTION_ID");
        Assertions.assertEquals(ApacheDubboConstants.META_SPAN_ID, "_DUBBO_SPAN_ID");
        Assertions.assertEquals(ApacheDubboConstants.META_PARENT_SPAN_ID, "_DUBBO_PARENT_SPAN_ID");
        Assertions.assertEquals(ApacheDubboConstants.META_PARENT_APPLICATION_NAME, "_DUBBO_PARENT_APPLICATION_NAME");
        Assertions.assertEquals(ApacheDubboConstants.META_PARENT_APPLICATION_TYPE, "_DUBBO_PARENT_APPLICATION_TYPE");
        Assertions.assertEquals(ApacheDubboConstants.META_FLAGS, "_DUBBO_FLAGS");
        Assertions.assertEquals(ApacheDubboConstants.MONITOR_SERVICE_FQCN, "org.apache.dubbo.monitor.MonitorService");
    }
}