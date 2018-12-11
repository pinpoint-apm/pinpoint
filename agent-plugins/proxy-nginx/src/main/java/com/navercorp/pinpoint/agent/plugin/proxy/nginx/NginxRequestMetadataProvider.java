/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.agent.plugin.proxy.nginx;

import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestMetadataProvider;
import com.navercorp.pinpoint.agent.plugin.proxy.common.ProxyRequestMetadataSetupContext;

/**
 * @author jaehong.kim
 */
public class NginxRequestMetadataProvider implements ProxyRequestMetadataProvider {
    @Override
    public void setup(ProxyRequestMetadataSetupContext context) {
        context.addProxyHttpHeaderType(NginxRequestConstants.NGINX_REQUEST_TYPE);
    }
}