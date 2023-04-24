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
public final class SslServerProperties {

    private final String sslProviderType;
    private final Resource keyResource;
    private final Resource keyCertChainResource;

    public SslServerProperties(String sslProviderType, Resource keyResource, Resource keyCertChainResource) {
        this.sslProviderType = Objects.requireNonNull(sslProviderType, "sslProviderType");
        this.keyResource = keyResource;
        this.keyCertChainResource = keyCertChainResource;
    }


    public String getSslProviderType() {
        return sslProviderType;
    }

    public Resource getKeyResource() {
        return keyResource;
    }

    public Resource getKeyCertChainResource() {
        return keyCertChainResource;
    }

    @Override
    public String toString() {
        return "SslServerConfig{" +
                "sslProviderType='" + sslProviderType + '\'' +
                ", keyFileUrl='" + keyResource + '\'' +
                ", keyCertChainFileUrl='" + keyCertChainResource + '\'' +
                '}';
    }
}