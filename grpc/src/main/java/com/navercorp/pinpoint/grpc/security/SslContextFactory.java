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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.security.pkcs.PKCS8Key;
import sun.security.util.DerValue;
import sun.security.x509.X509CertImpl;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public final class SslContextFactory {

    private static final Logger LOGGER = LogManager.getLogger(SslContextFactory.class);

    public static SslContext create(SslServerConfig serverConfig) throws SSLException {
        Objects.requireNonNull(serverConfig, "serverConfig");

        SslProvider sslProvider = getSslProvider(serverConfig.getSslProviderType());

        SslContextBuilder sslContextBuilder;
        try {
            X509Certificate[] certificates = readX509Certificates(serverConfig.getKeyCertChainResources());
            String password = serverConfig.getKeyPassword();
            PrivateKey privateKey = readPrivateKey(serverConfig.getKeyResource());

            sslContextBuilder = SslContextBuilder.forServer(privateKey, password, certificates);
            SslContext sslContext = createSslContext(sslContextBuilder, sslProvider);

            assertValidCipherSuite(sslContext);

            return sslContext;
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    public static SslContext create(SslClientConfig clientConfig) throws SSLException {
        Objects.requireNonNull(clientConfig, "clientConfig");

        if (!clientConfig.isEnable()) {
            throw new IllegalArgumentException("sslConfig is disabled.");
        }

        SslProvider sslProvider = getSslProvider(clientConfig.getSslProviderType());

        SslContextBuilder sslContextBuilder;
        try {
            sslContextBuilder = SslContextBuilder.forClient();

            Resource[] trustCertResources = clientConfig.getTrustCertResources();
            if (trustCertResources != null) {
                sslContextBuilder.trustManager(readX509Certificates(trustCertResources));
            } else {
                // Loads default Root CA certificates (generally, from JAVA_HOME/lib/cacerts)
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore)null);
                sslContextBuilder.trustManager(trustManagerFactory);
            }

            SslContext sslContext = createSslContext(sslContextBuilder, sslProvider);

            assertValidCipherSuite(sslContext);

            return sslContext;
        } catch (SSLException e) {
            throw e;
        } catch (Exception e) {
            throw new SSLException(e);
        }
    }

    private static X509Certificate[] readX509Certificates(Resource[] resources) {
        X509Certificate[] certs = new X509Certificate[resources.length];
        for (int i = 0; i < resources.length; i++) {
            certs[i] = readX509Certificate(resources[i]);
        }
        return certs;
    }

    private static X509Certificate readX509Certificate(Resource resource) {
        try {
            return new X509CertImpl(resource.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read certificate: " + resource, e);
        }
    }

    private static PrivateKey readPrivateKey(Resource resource) {
        try {
            return PKCS8Key.parseKey(new DerValue(resource.getInputStream()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read private key: " + resource, e);
        }
    }

    private static SslContext createSslContext(SslContextBuilder sslContextBuilder, SslProvider sslProvider) throws SSLException {
        sslContextBuilder.sslProvider(sslProvider);

        sslContextBuilder.protocols(SecurityConstants.DEFAULT_SUPPORT_PROTOCOLS.toArray(new String[0]));
        sslContextBuilder.ciphers(SecurityConstants.DEFAULT_SUPPORT_CIPHER_SUITE, SupportedCipherSuiteFilter.INSTANCE);

        SslContextBuilder configure = GrpcSslContexts.configure(sslContextBuilder, sslProvider);
        return configure.build();
    }

    private static void assertValidCipherSuite(SslContext sslContext) throws SSLException {
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