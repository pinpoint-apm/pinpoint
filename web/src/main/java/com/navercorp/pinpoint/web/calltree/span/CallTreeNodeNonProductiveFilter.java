/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.calltree.span;

import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CallTreeNodeNonProductiveFilter implements Predicate<CallTreeNode> {
    private static final List<String> NON_PRODUCTIVE_SERVICE_TYPE_NAMES = List.of(
            "REACTOR",
            "ASYNC",
            "KT_COROUTINES"
    );

    private static Predicate<CallTreeNode> filter;

    public static Predicate<CallTreeNode> of(ServiceTypeRegistryService serviceTypeRegistryService) {
        if (filter == null) {
            final List<ServiceType> nonProductiveServiceTypes = new ArrayList<>();
            for (String name : NON_PRODUCTIVE_SERVICE_TYPE_NAMES) {
                final ServiceType serviceType = serviceTypeRegistryService.findServiceTypeByName(name);
                if (serviceType != null && serviceType != ServiceType.UNDEFINED) {
                    // UNDEFINED is null
                    nonProductiveServiceTypes.add(serviceType);
                }
            }
            filter = new CallTreeNodeNonProductiveFilter(nonProductiveServiceTypes);
        }
        return filter;
    }

    private final List<ServiceType> nonProductiveServiceTypes;

    CallTreeNodeNonProductiveFilter(List<ServiceType> nonProductiveServiceTypes) {
        this.nonProductiveServiceTypes = nonProductiveServiceTypes;
    }

    @Override
    public boolean test(CallTreeNode node) {
        if(node == null) {
            return false;
        }
        if (node.hasChild()) {
            return false;
        }
        final Align align = node.getAlign();
        if (align instanceof SpanAlign) {
            // span is productive
            return false;
        }
        final SpanEventBo spanEventBo = align.getSpanEventBo();
        if (spanEventBo == null) {
            // defense code
            return false;
        }
        if (spanEventBo.hasException()) {
            // exception is productive
            return false;
        }
        final int serviceTypeCode = spanEventBo.getServiceType();
        for (ServiceType serviceType : nonProductiveServiceTypes) {
            if (serviceTypeCode == serviceType.getCode()) {
                return true;
            }
        }

        return false;
    }
}