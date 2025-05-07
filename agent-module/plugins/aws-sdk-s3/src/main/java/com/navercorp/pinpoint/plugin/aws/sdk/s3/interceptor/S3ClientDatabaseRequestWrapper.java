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

package com.navercorp.pinpoint.plugin.aws.sdk.s3.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientDatabaseRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.URI;
import java.util.Objects;

public class S3ClientDatabaseRequestWrapper implements ClientDatabaseRequestWrapper {

    private final URI uri;

    public S3ClientDatabaseRequestWrapper(URI uri) {
        this.uri = Objects.requireNonNull(uri, "uri");
    }

    @Override
    public String getDestinationId() {
        return "S3";
    }

    @Override
    public String getEndPoint() {
        return HostAndPort.toHostAndPortString(uri.getHost(), uri.getPort());
    }
}