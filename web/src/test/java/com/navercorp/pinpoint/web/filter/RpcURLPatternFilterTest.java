/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.filter;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.AnnotationKeyFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class RpcURLPatternFilterTest {

    private final AnnotationKey TEST_RPC_URL_ANNOTATION_KEY = AnnotationKeyFactory.of(-1, "rpc.url");
    private final short TEST_RPC_SERVICE_TYPE_CODE = 9999;
    private final String TEST_RPC_SERVICE_TYPE_NAME = "TEST_RPC";
    private final ServiceType TEST_RPC_SERVICE_TYPE = ServiceTypeFactory.of(TEST_RPC_SERVICE_TYPE_CODE, TEST_RPC_SERVICE_TYPE_NAME, ServiceTypeProperty.RECORD_STATISTICS);

    private ServiceTypeRegistryService serviceTypeRegistryService;

    private AnnotationKeyRegistryService annotationKeyRegistryService;

    @Before
    public void setUp() {
        serviceTypeRegistryService = new ServiceTypeRegistryService() {
            @Override
            public ServiceType findServiceType(short serviceType) {
                return TEST_RPC_SERVICE_TYPE;
            }

            @Override
            public ServiceType findServiceTypeByName(String typeName) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<ServiceType> findDesc(String desc) {
                throw new UnsupportedOperationException();
            }
        };
        annotationKeyRegistryService = new AnnotationKeyRegistryService() {
            @Override
            public AnnotationKey findAnnotationKey(int annotationCode) {
                throw new UnsupportedOperationException();
            }

            @Override
            public AnnotationKey findAnnotationKeyByName(String keyName) {
                return TEST_RPC_URL_ANNOTATION_KEY;
            }

            @Override
            public AnnotationKey findApiErrorCode(int annotationCode) {
                throw new UnsupportedOperationException();
            }
        };
    }


    private RpcURLPatternFilter newRpcURLPatternFilter(String urlPattern) {
        return new RpcURLPatternFilter(urlPattern, serviceTypeRegistryService, annotationKeyRegistryService);
    }

    @Test
    public void emptyPatternShouldReject() {
        // Given
        final String urlPattern = "";
        final String rpcUrl = "http://a.b.c";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertFalse(accept);
    }

    @Test
    public void testPath() {
        // Given
        final String urlPattern = "/test/**";
        final String rpcUrl = "/test/rpc/path";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertTrue(accept);
    }

    @Test
    public void testFullUrl() {
        // Given
        final String urlPattern = "/test/**";
        final String rpcUrl = "http://some.test.domain:8080/test/rpc/path";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertTrue(accept);
    }

    @Test
    public void testDomainAndPath() {
        // Given
        final String urlPattern = "some.test.domain/test/rpc/**";
        final String rpcUrl = "some.test.domain/test/rpc/test?value=11";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertTrue(accept);
    }

    @Test
    public void testString() {
        // Given
        final String urlPattern = "some*";
        final String rpcUrl = "someName";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertTrue(accept);
    }

    @Test
    public void testWeirdPath() {
        // Given
        final String urlPattern = ":/**";
        final String rpcUrl = ":/invalid/uri";
        final RpcURLPatternFilter rpcURLPatternFilter = newRpcURLPatternFilter(urlPattern);
        // When
        boolean accept = rpcURLPatternFilter.accept(createTestRpcSpans(rpcUrl));
        // Then
        Assert.assertTrue(accept);
    }

    private List<SpanBo> createTestRpcSpans(String... rpcUrls) {
        List<SpanBo> spanBos = new ArrayList<>();
        for (String rpcUrl : rpcUrls) {
            SpanEventBo testRpcSpanEvent = new SpanEventBo();
            testRpcSpanEvent.setServiceType(TEST_RPC_SERVICE_TYPE_CODE);
            AnnotationBo testRpcAnnotationBo = new AnnotationBo(TEST_RPC_URL_ANNOTATION_KEY.getCode(), rpcUrl);
            testRpcSpanEvent.setAnnotationBoList(Collections.singletonList(testRpcAnnotationBo));
            SpanBo spanBo = new SpanBo();
            spanBo.addSpanEvent(testRpcSpanEvent);
            spanBos.add(spanBo);
        }
        return spanBos;
    }
}
