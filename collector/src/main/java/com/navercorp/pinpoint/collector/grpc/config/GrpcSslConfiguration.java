/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.grpc.security.SslServerConfig;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcSslConfiguration {

    private final boolean enable;
    private final String providerType;
    private final Resource keyResource;
    private final Resource keyCertChainResource;

    private GrpcSslConfiguration(boolean enable, String providerType,
                                 Resource keyResource, Resource keyCertChainResource) {
        this.enable = enable;
        this.providerType = providerType;
        this.keyResource = keyResource;
        this.keyCertChainResource = keyCertChainResource;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getProviderType() {
        return providerType;
    }

    public Resource getKeyResource() {
        return keyResource;
    }

    public Resource getKeyCertChainResource() {
        return keyCertChainResource;
    }

    public SslServerConfig toSslServerConfig() {
        if (enable) {
            SslServerConfig sslServerConfig = new SslServerConfig(enable, providerType,
                    new SpringResource(keyResource), new SpringResource(keyCertChainResource));
            return sslServerConfig;
        } else {
            return SslServerConfig.DISABLED_CONFIG;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private boolean enable;
        private String providerType;
        private Resource keyFilePath;
        private Resource keyCertFilePath;

        private Builder() {
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public String getProviderType() {
            return providerType;
        }

        public void setProviderType(String providerType) {
            this.providerType = providerType;
        }

        public Resource getKeyFilePath() {
            return keyFilePath;
        }

        public void setKeyFilePath(Resource keyFilePath) {
            this.keyFilePath = keyFilePath;
        }

        public Resource getKeyCertFilePath() {
            return keyCertFilePath;
        }

        public void setKeyCertFilePath(Resource keyCertFilePath) {
            this.keyCertFilePath = keyCertFilePath;
        }

        public GrpcSslConfiguration build() throws IOException {
            if (enable) {
                Objects.requireNonNull(providerType);
                return new GrpcSslConfiguration(this.enable, this.providerType, this.keyFilePath, this.keyCertFilePath);
            } else {
                return new GrpcSslConfiguration(this.enable, this.providerType, null, null);
            }
        }
    }

    @Override
    public String toString() {
        return "GrpcSslConfiguration{" +
                "enable=" + enable +
                ", providerType='" + providerType + '\'' +
                ", keyResource='" + keyResource + '\'' +
                ", keyCertChainResource='" + keyCertChainResource + '\'' +
                '}';
    }
}
