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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Request;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RequestHeaderAdaptorV2 implements ClientHeaderAdaptor<Request> {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(Request request, String name, String value) {
        final HttpHeaders httpRequestHeaders = request.getHeaders();
        if (httpRequestHeaders != null) {
            httpRequestHeaders.set(name, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        }
    }
}
