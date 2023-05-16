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
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class SslContextFactory {

    private final Logger LOGGER = LogManager.getLogger(SslContextFactory.class);

    private final SslProvider sslProvider;

    public SslContextFactory(String providerType) throws SSLException {
        Objects.requireNonNull(providerType, "providerType");
        this.sslProvider = getSslProvider(providerType);
    }

    public SslContext forServer(InputStream keyCertChainInputStream, InputStream keyInputStream) throws SSLException {
        Objects.requireNonNull(keyCertChainInputStream, "keyCertChainInputStream");
        Objects.requireNonNull(keyInputStream, "keyInputStream");

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyCertChainInputStream, keyInputStream);
            SslContext sslContext = createSslContext(sslContextBuilder, sslProvider);

            assertValidCipherSuite(sslContext);

            return sslContext;
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    public SslContext forClient(InputStream trustCertCollectionInputStream) throws SSLException {
        Objects.requireNonNull(trustCertCollectionInputStream, "trustCertCollectionInputStream");

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            sslContextBuilder.trustManager(trustCertCollectionInputStream);
            return createSslContext(sslContextBuilder, sslProvider);
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    public SslContext forClient() throws SSLException {
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();

            // Loads default Root CA certificates (generally, from JAVA_HOME/lib/cacerts)
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore)null);
            sslContextBuilder.trustManager(trustManagerFactory);

            return createSslContext(sslContextBuilder, sslProvider);
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }


    private SslContext createSslContext(SslContextBuilder sslContextBuilder, SslProvider sslProvider) throws SSLException {
        sslContextBuilder.sslProvider(sslProvider);

        sslContextBuilder.protocols(SecurityConstants.DEFAULT_SUPPORT_PROTOCOLS.toArray(new String[0]));
        sslContextBuilder.ciphers(SecurityConstants.DEFAULT_SUPPORT_CIPHER_SUITE, SupportedCipherSuiteFilter.INSTANCE);

        SslContextBuilder configure = GrpcSslContexts.configure(sslContextBuilder, sslProvider);
        return configure.build();
    }

    private void assertValidCipherSuite(SslContext sslContext) throws SSLException {
        Objects.requireNonNull(sslContext, "sslContext must not be null");

        List<String> supportedCipherSuiteList = sslContext.cipherSuites();
        if (CollectionUtils.isEmpty(supportedCipherSuiteList)) {
            throw new SSLException("cipherSuites must not be empty");
        }

        for (String cipherSuite : supportedCipherSuiteList) {
            if (SecurityConstants.BAD_CIPHER_SUITE_LIST.contains(cipherSuite)) {
                throw new SSLException(cipherSuite + " is not safe. Please check this url.(https://httpwg.org/specs/rfc7540.html#BadCipherSuites)");
            }
        }

        LOGGER.info("Support cipher list : {} {}", sslContext, supportedCipherSuiteList);
    }

    SslProvider getSslProvider(String providerType) throws SSLException {
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