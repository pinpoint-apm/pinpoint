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

package com.navercorp.pinpoint.collector.grpc.ssl;

import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class GrpcSslProperties {

    private final String providerType;
    private final Resource keyResource;
    private final Resource keyCertChainResource;

    private GrpcSslProperties(String providerType,
                              Resource keyResource, Resource keyCertChainResource) {
        this.providerType = providerType;
        this.keyResource = Objects.requireNonNull(keyResource, "keyResource");
        this.keyCertChainResource = Objects.requireNonNull(keyCertChainResource, "keyCertChainResource");
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


    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String providerType;
        private Resource keyFilePath;
        private Resource keyCertFilePath;

        private Builder() {
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

        public GrpcSslProperties build() throws IOException {
            Objects.requireNonNull(providerType, "providerType");
            Objects.requireNonNull(keyFilePath, "keyFilePath does not exists");
            Objects.requireNonNull(keyCertFilePath, "keyCertFilePath does not exists");
            return new GrpcSslProperties(this.providerType, this.keyFilePath, this.keyCertFilePath);
        }
    }

    @Override
    public String toString() {
        return "GrpcSslProperties{" +
                "providerType='" + providerType + '\'' +
                ", keyResource='" + keyResource + '\'' +
                ", keyCertChainResource='" + keyCertChainResource + '\'' +
                '}';
    }
}
