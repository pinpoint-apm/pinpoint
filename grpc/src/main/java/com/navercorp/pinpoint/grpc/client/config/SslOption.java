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

import java.util.Arrays;
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
    private final Resource[] trustCertResources;

    private SslOption(boolean enable, String providerType, Resource[] trustCertResources) {
        this.enable = enable;
        this.providerType = providerType;
        this.trustCertResources = trustCertResources;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getProviderType() {
        return providerType;
    }

    public Resource[] getTrustCertResources() {
        return trustCertResources;
    }

    public static class Builder {

        private final String basePath;

        @Value("${enable}")
        private boolean enable;
        @Value("${provider.type}")
        private String providerType;
        @Value("${trust.cert.file.paths}")
        private String[] trustCertFilePaths;

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

        public String[] getTrustCertFilePaths() {
            return trustCertFilePaths;
        }

        public void setTrustCertFilePaths(String[] trustCertFilePaths) {
            this.trustCertFilePaths = trustCertFilePaths;
        }

        public SslOption build()  {
            if (enable) {
                Objects.requireNonNull(providerType);
                Resource[] trustCertResources = toResources(trustCertFilePaths);
                return new SslOption(true, this.providerType, trustCertResources);
            } else {
                Resource[] trustCertResources = new Resource[1];
                trustCertResources[0] = DEFAULT_TRUST_CERT_RESOURCE;

                return new SslOption(this.enable, DEFAULT_PROVIDER_TYPE, trustCertResources);
            }
        }

        private static final String CLASSPATH_URL_PREFIX = "classpath:";
        private static final String FILE_URL_PREFIX = "file:";


        private Resource[] toResources(String[] filepaths) {
            Resource[] res = new Resource[filepaths.length];
            for (int i = 0; i < filepaths.length; i++) {
                res[i] = toResource(filepaths[i]);
            }
            return res;
        }

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
        sb.append(", trustCertResource=").append(Arrays.toString(trustCertResources));
        sb.append('}');
        return sb.toString();
    }
}
