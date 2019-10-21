/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request.util;

import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;


/**
 * @author Woonduk Kang(emeroad)
 */
public class RealIpHeaderResolver<T> implements RemoteAddressResolver<T> {

    private final String realIpHeaderName;
    private final String realIpHeaderEmptyValue;

    public RealIpHeaderResolver(final String realIpHeaderName, final String realIpHeaderEmptyValue) {
        this.realIpHeaderName = Assert.requireNonNull(realIpHeaderName, "realIpHeaderName");
        this.realIpHeaderEmptyValue = realIpHeaderEmptyValue;
    }

    @Override
    public String resolve(RequestAdaptor<T> requestAdaptor, T request) {
        final String realIp = requestAdaptor.getHeader(request, realIpHeaderName);
        if (StringUtils.isEmpty(realIp)) {
            return null;
        }

        if (realIpHeaderEmptyValue != null && realIpHeaderEmptyValue.equalsIgnoreCase(realIp)) {
            return null;
        }

        final int firstIndex = realIp.indexOf(',');
        if (firstIndex == -1) {
            return realIp;
        } else {
            return realIp.substring(0, firstIndex);
        }
    }

}
