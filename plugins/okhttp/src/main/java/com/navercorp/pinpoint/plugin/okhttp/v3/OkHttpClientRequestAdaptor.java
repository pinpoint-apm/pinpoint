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

package com.navercorp.pinpoint.plugin.okhttp.v3;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import okhttp3.HttpUrl;
import okhttp3.Request;

import java.net.URL;

/**
 * @author jaehong.kim
 */
public class OkHttpClientRequestAdaptor implements ClientRequestAdaptor<Request> {

    public OkHttpClientRequestAdaptor() {
    }


    @Override
    public String getDestinationId(Request request) {
        final HttpUrl httpUrl = request.url();
        if (httpUrl != null) {
            URL url = httpUrl.url();
            if (url != null && url.getHost() != null) {
                final int port = EndPointUtils.getPort(url.getPort(), url.getDefaultPort());
                return HostAndPort.toHostAndPortString(url.getHost(), port);
            }
        }
        return "Unknown";
    }

    @Override
    public String getUrl(Request request) {
        final HttpUrl httpUrl = request.url();
        if (httpUrl != null) {
            return httpUrl.url().toString();
        }
        return null;
    }

}
