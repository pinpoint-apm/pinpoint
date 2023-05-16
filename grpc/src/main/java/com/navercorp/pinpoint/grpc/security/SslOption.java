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

/**
 * @author Taejin Koo
 */
public enum SslOption {

    //// for all
    ENABLE("ssl.enable", "false"),

    PROVIDER_TYPE("ssl.provider.type", SecurityConstants.DEFAULT_SSL_PROVIDER),

    //// for server
    KEY_FILE_PATH("ssl.key.file.path", ""),
    KEY_CERT_CHAIN_FILE_PATH("ssl.key.cert.file.path", ""),

    //// for client
    TRUST_CERT_FILE_PATH("ssl.trust.cert.file.path", "");

    private final String key;
    private final String defaultValue;

    SslOption(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

}