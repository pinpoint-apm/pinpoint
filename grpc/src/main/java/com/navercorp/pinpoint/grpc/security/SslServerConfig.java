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

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class SslServerConfig {

    private static final boolean DISABLED = false;
    private static final String EMPTY_STRING = "";

    public static final SslServerConfig DISABLED_CONFIG = new SslServerConfig(DISABLED, EMPTY_STRING, null, null, null);

    private final boolean enable;

    private final String sslProviderType;
    private final Resource keyResource;
    private final Resource[] keyCertChainResources;
    private final String keyPassword;

    public SslServerConfig(boolean enable, String sslProviderType, Resource keyResource, Resource[] keyCertChainResources, String keyPassword) {
        this.enable = enable;
        this.sslProviderType = Objects.requireNonNull(sslProviderType, "sslProviderType");
        this.keyResource = keyResource;
        this.keyCertChainResources = keyCertChainResources;
        this.keyPassword = keyPassword;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getSslProviderType() {
        return sslProviderType;
    }

    public Resource getKeyResource() {
        return keyResource;
    }

    public Resource[] getKeyCertChainResources() {
        return keyCertChainResources;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    @Override
    public String toString() {
        return "SslServerConfig{" +
                "enable=" + enable +
                ", sslProviderType='" + sslProviderType + '\'' +
                ", keyFileUrl='" + keyResource + '\'' +
                ", keyCertChainFileUrls='" + Arrays.toString(keyCertChainResources) + '\'' +
                '}';
    }
}