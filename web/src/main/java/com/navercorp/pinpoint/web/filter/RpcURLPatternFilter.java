/*
 * Copyright 2014 NAVER Corp.
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

import com.google.common.collect.ImmutableSet;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * @author emeroad
 */
// TODO development class
public class RpcURLPatternFilter implements URLPatternFilter {

    private final String urlPattern;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    // TODO remove. hard coded annotation for compatibility, need a better to group rpc url annotations
    private final Set<Integer> rpcEndpointAnnotationCodes;

    public RpcURLPatternFilter(String urlPattern, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        this.urlPattern = Objects.requireNonNull(urlPattern, "urlPattern");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.annotationKeyRegistryService = Objects.requireNonNull(annotationKeyRegistryService, "annotationKeyRegistryService");

        // TODO serviceType rpctype

        // TODO remove. hard coded annotation for compatibility, need a better to group rpc url annotations
        this.rpcEndpointAnnotationCodes = initRpcEndpointAnnotations(
                AnnotationKey.HTTP_URL.getName(), AnnotationKey.MESSAGE_QUEUE_URI.getName(),
                "thrift.url", "npc.url", "nimm.url"
        );
    }

    private Set<Integer> initRpcEndpointAnnotations(String... annotationKeyNames) {
        Set<Integer> rpcEndPointAnnotationCodes = new HashSet<>();
        for (String annotationKeyName : annotationKeyNames) {
            try {
                final AnnotationKey pluginRpcEndpointAnnotationKey = annotationKeyRegistryService.findAnnotationKeyByName(annotationKeyName);
                if (pluginRpcEndpointAnnotationKey != null) {
                    rpcEndPointAnnotationCodes.add(pluginRpcEndpointAnnotationKey.getCode());
                }
            } catch (NoSuchElementException ignore) {
                // ignore
            }
        }
        return ImmutableSet.copyOf(rpcEndPointAnnotationCodes);
    }

    @Override
    public boolean accept(List<SpanBo> fromSpanList) {
        for (SpanBo spanBo : fromSpanList) {
            final List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            if (CollectionUtils.isEmpty(spanEventBoList)) {
                return REJECT;
            }

            for (SpanEventBo event : spanEventBoList) {
                final ServiceType eventServiceType = serviceTypeRegistryService.findServiceType(event.getServiceType());
                if (!eventServiceType.isRpcClient()) {
                    continue;
                }
                if (!eventServiceType.isRecordStatistics()) {
                    continue;
                }
//                http://api.domain.com/test/ArticleList.do
//                slice url ->/test/ArticleList.do
                final List<AnnotationBo> annotationBoList = event.getAnnotationBoList();
                if (annotationBoList == null) {
                    continue;
                }
                for (AnnotationBo annotationBo : annotationBoList) {
//                    TODO ?? url format & annotation type detect
                    int key = annotationBo.getKey();
                    if (isURL(key)) {
                        String url = (String) annotationBo.getValue();
                        String path = getPath(url);
                        final boolean match = matcher.match(urlPattern, path);
                        if (match) {
                            return ACCEPT;
                        }
                    }
                }

            }
        }

        return REJECT;
    }

    private boolean isURL(int key) {
        return rpcEndpointAnnotationCodes.contains(key);
    }

    private String getPath(String endPoint) {
        if (endPoint == null) {
            return  null;
        }
        // is URI format
        final String authoritySeparator = "://";
        final int authorityIndex = endPoint.indexOf(authoritySeparator);
        if (authorityIndex == -1) {
            return endPoint;
        }
        final int pathIndex = endPoint.indexOf('/', authorityIndex + authoritySeparator.length());
        if (pathIndex == -1) {
//            ???
            return endPoint;
        }
        return endPoint.substring(pathIndex);
    }
}
