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

import java.io.File;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class SslServerConfig {

    private static final boolean DISABLED = false;
    private static final String EMPTY_STRING = "";

    public static final SslServerConfig DISABLED_CONFIG = new SslServerConfig(DISABLED, EMPTY_STRING, null, null);

    private final boolean enable;

    private final String sslProviderType;
    private final File keyFile;
    private final File keyCertChainFile;

    public SslServerConfig(boolean enable, String sslProviderType, File keyFile, File keyCertChainFile) {
        this.enable = enable;
        this.sslProviderType = Objects.requireNonNull(sslProviderType, "sslProviderType");
        this.keyFile = keyFile;
        this.keyCertChainFile = keyCertChainFile;
    }

    public boolean isEnable() {
        return enable;
    }

    public String getSslProviderType() {
        return sslProviderType;
    }

    public File getKeyFile() {
        return keyFile;
    }

    public File getKeyCertChainFile() {
        return keyCertChainFile;
    }

    @Override
    public String toString() {
        return "SslServerConfig{" +
                "enable=" + enable +
                ", sslProviderType='" + sslProviderType + '\'' +
                ", keyFile='" + keyFile + '\'' +
                ", keyCertChainFile='" + keyCertChainFile + '\'' +
                '}';
    }
}