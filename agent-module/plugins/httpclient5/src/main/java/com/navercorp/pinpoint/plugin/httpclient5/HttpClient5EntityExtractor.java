/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpRequest;

public class HttpClient5EntityExtractor implements EntityExtractor<HttpRequest> {

    public static final EntityExtractor<HttpRequest> INSTANCE = new HttpClient5EntityExtractor();

    @Override
    public String getEntity(HttpRequest httpRequest) {
        if (httpRequest instanceof HttpEntityContainer) {
            final HttpEntity httpEntity = ((HttpEntityContainer) httpRequest).getEntity();
            if (httpEntity != null) {
                final long length = httpEntity.getContentLength();
                final StringBuilder sb = new StringBuilder();
                sb.append("HTTP entity length: ");
                sb.append(length);
                return sb.toString();
            }
        }

        return null;
    }
}
