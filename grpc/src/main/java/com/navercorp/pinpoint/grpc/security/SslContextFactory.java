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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.StringUtils;

import com.navercorp.pinpoint.grpc.util.Resource;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class SslContextFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(SslContextFactory.class);

    public static SslContext create(SslServerConfig serverConfig) throws SSLException {
        Objects.requireNonNull(serverConfig, "serverConfig");

        SslProvider sslProvider = getSslProvider(serverConfig.getSslProviderType());

        SslContextBuilder sslContextBuilder;
        try {
            Resource keyCertChainFileResource = serverConfig.getKeyCertChainResource();
            Resource keyResource = serverConfig.getKeyResource();

            sslContextBuilder = SslContextBuilder.forServer(keyCertChainFileResource.getInputStream(), keyResource.getInputStream());
            sslContextBuilder.protocols(SecurityConstants.DEFAULT_SUPPORT_PROTOCOLS.toArray(new String[0]));
            sslContextBuilder.ciphers(SecurityConstants.DEFAULT_SUPPORT_CIPHER_SUITE, SupportedCipherSuiteFilter.INSTANCE);

            SslContextBuilder configure = GrpcSslContexts.configure(sslContextBuilder, sslProvider);
            SslContext sslContext = configure.build();

            List<String> supportedCipherSuiteList = sslContext.cipherSuites();
            if (CollectionUtils.isEmpty(supportedCipherSuiteList)) {
                throw new SSLException("cipherSuites must not be empty");
            }

            assertValidCipherSuite(supportedCipherSuiteList);

            LOGGER.info("Support cipher list : {} {}", sslContext, supportedCipherSuiteList);

            return sslContext;
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    private static void assertValidCipherSuite(List<String> supportedCipherSuiteList) throws SSLException {
        for (String cipherSuite : supportedCipherSuiteList) {
            if (SecurityConstants.BAD_CIPHER_SUITE_LIST.contains(cipherSuite)) {
                throw new SSLException(cipherSuite + " is not safe. Please check this url.(https://httpwg.org/specs/rfc7540.html#BadCipherSuites)");
            }
        }
    }

    static SslProvider getSslProvider(String providerType) throws SSLException {
        if (StringUtils.isEmpty(providerType)) {
            return SslProvider.OPENSSL;
        }

        if (SslProvider.OPENSSL.name().equalsIgnoreCase(providerType)) {
            return SslProvider.OPENSSL;
        }

        if (SslProvider.JDK.name().equalsIgnoreCase(providerType)) {
            return SslProvider.JDK;
        }

        throw new SSLException("can't find SslProvider. value:" + providerType);
    }

}