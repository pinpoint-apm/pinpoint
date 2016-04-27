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

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author emeroad
 */
// TODO development class
public class RpcURLPatternFilter implements URLPatternFilter {

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final String urlPattern;
    private final AntPathMatcher matcher = new AntPathMatcher();
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AnnotationKeyRegistryService annotationKeyRegistryService;

    // TODO remove. hard coded annotation for compatibility
    @Deprecated
    private final AnnotationKey npcUrl;

    public RpcURLPatternFilter(String urlPattern, ServiceTypeRegistryService serviceTypeRegistryService, AnnotationKeyRegistryService annotationKeyRegistryService) {
        if (urlPattern == null) {
            throw new NullPointerException("urlPattern must not be null");
        }
        // TODO remove decode
        this.urlPattern = new String(Base64.decodeBase64(urlPattern), UTF8);
        // TODO serviceType rpctype

        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.annotationKeyRegistryService = annotationKeyRegistryService;
        this.npcUrl = this.annotationKeyRegistryService.findAnnotationKeyByName("npc.url");
    }

    @Override
    public boolean accept(List<SpanBo> fromSpanList) {
        for (SpanBo spanBo : fromSpanList) {
            List<SpanEventBo> spanEventBoList = spanBo.getSpanEventBoList();
            if (spanEventBoList == null) {
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
        return key == AnnotationKey.HTTP_URL.getCode() || key == npcUrl.getCode();
    }

    private String getPath(String endPoint) {
        if (endPoint == null) {
            return  null;
        }
        // is URI format
        final int authorityIndex = endPoint.indexOf("://");
        if (authorityIndex == -1) {
            return endPoint;
        }
        final int pathIndex = endPoint.indexOf('/', authorityIndex + 1);
        if (pathIndex == -1) {
//            ???
            return endPoint;
        }
        return endPoint.substring(pathIndex+1);
    }
}
