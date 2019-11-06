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


import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RemoteAddressResolverFactory {


    public static <T> RequestAdaptor<T> wrapRealIpSupport(RequestAdaptor<T> requestAdaptor, final String realIpHeaderName, final String realIpHeaderEmptyValue) {
        if (!StringUtils.hasLength(realIpHeaderName)) {
            return requestAdaptor;
        }
        RealIpHeaderResolver<T> tealIpHeaderResolver = new RealIpHeaderResolver<T>(realIpHeaderName, realIpHeaderEmptyValue);
        RequestAdaptor<T> wrap = new HeaderResolveRequestAdaptor<T>(requestAdaptor, tealIpHeaderResolver);
        return wrap;
    }
}
