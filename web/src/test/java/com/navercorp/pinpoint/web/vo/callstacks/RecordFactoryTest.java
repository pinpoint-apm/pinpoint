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

package com.navercorp.pinpoint.web.vo.callstacks;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.service.AnnotationKeyMatcherService;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RecordFactoryTest {

    @Mock
    private ServiceTypeRegistryService mockServiceTypeRegistryService;

    @Mock
    private AnnotationKeyRegistryService mockAnnotationKeyRegistryService;

    @Mock
    private AnnotationKeyMatcherService mockAnnotationKeyMatcherService;

    @Mock
    private ProxyRequestTypeRegistryService mockProxyRequestTypeRegistryService;

    private RecordFactory newRecordFactory() {
        return new RecordFactory(mockAnnotationKeyMatcherService, mockServiceTypeRegistryService, mockAnnotationKeyRegistryService, mockProxyRequestTypeRegistryService);
    }

    public void get() {
        // TODO
    }

    @Test
    public void getException_check_argument() {
        final RecordFactory factory = newRecordFactory();

        SpanBo spanBo = new SpanBo();
        spanBo.setTransactionId(new TransactionId("test", 0, 0));
        spanBo.setExceptionInfo(1, null);
        Align align = new SpanAlign(spanBo);


        Record exceptionRecord = factory.getException(0, 0, align);

        Assert.assertNotNull(exceptionRecord.getArguments());
    }



    @Test
    public void getParameter_check_argument() {

        final RecordFactory factory = newRecordFactory();

        Record exceptionRecord = factory.getParameter(0, 0, "testMethod", null);

        Assert.assertEquals(exceptionRecord.getArguments(), "null");
    }

}