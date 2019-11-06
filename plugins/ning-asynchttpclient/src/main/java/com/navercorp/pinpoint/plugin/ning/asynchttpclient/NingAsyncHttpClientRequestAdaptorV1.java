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

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.ning.http.client.Request;

/**
 * @author jaehong.kim
 */
public class NingAsyncHttpClientRequestAdaptorV1 implements ClientRequestAdaptor<Request> {

    public NingAsyncHttpClientRequestAdaptorV1() {
    }


    @Override
    public String getDestinationId(Request request) {
        return EndPointUtils.getEndPoint(request.getUrl(), "Unknown");
    }

    @Override
    public String getUrl(Request request) {
        return request.getUrl();
    }



}