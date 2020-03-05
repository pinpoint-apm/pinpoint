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

package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import io.netty.handler.codec.http.HttpRequest;

/**
 * @author jaehong.kim
 */
public class VertxHttpClientRequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpRequest httpRequest;
    private final String host;


    public VertxHttpClientRequestWrapper(final HttpRequest httpRequest, final String host) {
        this.httpRequest = Assert.requireNonNull(httpRequest, "httpRequest");
        this.host = host;
    }


    @Override
    public String getDestinationId() {
        if (this.host != null) {
            return this.host;
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        return this.httpRequest.uri();
    }


}
