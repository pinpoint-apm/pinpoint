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

package com.navercorp.pinpoint.grpc.client.config;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.grpc.util.FileSystemResource;
import com.navercorp.pinpoint.grpc.util.Resource;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class SslOption {

    public static final boolean DISABLE = Boolean.FALSE;
    public static final String DEFAULT_PROVIDER_TYPE = "jdk"; // jdk
    public static final Resource DEFAULT_TRUST_CERT_RESOURCE = null; // `null` is load from (JAVA_HOME/lib/cacerts)

    private final boolean enable;
    private final String providerType;
    private final Resource trustCertResource;

    private SslOption(boolean enable, String providerType, Resource trustCertResource) {
        this.enable = enable;
        this.providerType = providerType;
        this.trustCertResource = trustCertResource;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getProviderType() {
        return providerType;
    }

    public Resource getTrustCertResource() {
        return trustCertResource;
    }

    public static class Builder {

        private final String basePath;

        @Value("${enable}")
        private boolean enable;
        @Value("${provider.type}")
        private String providerType;
        @Value("${trust.cert.file.path}")
        private String trustCertFilePath;

        public Builder(String basePath) {
            this.basePath = basePath;
        }

        public String getBasePath() {
            return basePath;
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

        public String getTrustCertFilePath() {
            return trustCertFilePath;
        }

        public void setTrustCertFilePath(String trustCertFilePath) {
            this.trustCertFilePath = trustCertFilePath;
        }

        public SslOption build()  {
            if (enable) {
                Objects.requireNonNull(providerType);
                Resource trustCertResource = toResource(trustCertFilePath);
                return new SslOption(true, this.providerType, trustCertResource);
            } else {
                return new SslOption(this.enable, DEFAULT_PROVIDER_TYPE, DEFAULT_TRUST_CERT_RESOURCE);
            }
        }

        private static final String CLASSPATH_URL_PREFIX = "classpath:";
        private static final String FILE_URL_PREFIX = "file:";

        private Resource toResource(String filePath) {
            if (!StringUtils.hasText(filePath)) {
                return null;
            }

            Resource resource = null;
            if (filePath.startsWith(CLASSPATH_URL_PREFIX)) {
                String path = filePath.substring(CLASSPATH_URL_PREFIX.length());
                resource = FileSystemResource.createResource(basePath, path);
            } else if (filePath.startsWith(FILE_URL_PREFIX)) {
                String path = filePath.substring(FILE_URL_PREFIX.length());
                resource = FileSystemResource.createResource(path);
            }

            if (resource != null && resource.exists()) {
                return resource;
            }
            throw new IllegalArgumentException("Could not find file.(path:" + filePath + ")");
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SslOption{");
        sb.append("enable=").append(enable);
        sb.append(", providerType='").append(providerType).append('\'');
        sb.append(", trustCertResource=").append(trustCertResource);
        sb.append('}');
        return sb.toString();
    }
}
