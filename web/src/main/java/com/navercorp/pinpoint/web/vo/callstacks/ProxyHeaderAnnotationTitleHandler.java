/*
 * Copyright 2025 NAVER Corp.
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


import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestType;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.LongIntIntByteByteStringValue;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.service.ProxyRequestTypeRegistryService;

import java.util.Objects;

public class ProxyHeaderAnnotationTitleHandler implements AnnotationHandler {
    private final ProxyRequestTypeRegistryService proxyRequestTypeRegistryService;

    public ProxyHeaderAnnotationTitleHandler(ProxyRequestTypeRegistryService proxyRequestTypeRegistryService) {
        this.proxyRequestTypeRegistryService = Objects.requireNonNull(proxyRequestTypeRegistryService, "proxyRequestTypeRegistryService");
    }

    @Override
    public String format(AnnotationKey annotationKey, AnnotationBo annotationBo, Align align) {
        if (!(annotationBo.getValue() instanceof LongIntIntByteByteStringValue value)) {
            return proxyRequestTypeRegistryService.unknown().getDisplayName();
        }

        final ProxyRequestType type = this.proxyRequestTypeRegistryService.findByCode(value.getIntValue1());
        return type.getDisplayName(value.getStringValue());
    }
}
