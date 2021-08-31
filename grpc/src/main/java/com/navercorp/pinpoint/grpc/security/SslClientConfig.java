/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.security;

import com.navercorp.pinpoint.grpc.util.Resource;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SslClientConfig {

    private static final boolean DISABLED = false;
    private static final String EMPTY_STRING = "";

    public static SslClientConfig DISABLED_CONFIG = new SslClientConfig(false, EMPTY_STRING, null);

    private final boolean enable;
    private final String sslProviderType;
    private final Resource trustCertResource;

    public SslClientConfig(boolean enable, String sslProviderType, Resource trustCertResource) {
        this.enable = enable;

        this.sslProviderType = Objects.requireNonNull(sslProviderType, "sslProviderType");
        this.trustCertResource = trustCertResource;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getSslProviderType() {
        return sslProviderType;
    }

    public Resource getTrustCertResource() {
        return trustCertResource;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SslClientConfig{");
        sb.append("enable=").append(enable);
        sb.append(", sslProviderType='").append(sslProviderType).append('\'');
        sb.append(", trustCertResource=").append(trustCertResource);
        sb.append('}');
        return sb.toString();
    }
}