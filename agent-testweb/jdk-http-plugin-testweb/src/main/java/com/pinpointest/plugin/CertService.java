package com.pinpointest.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Objects;

@Component
public class CertService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Resource keyStorePath;
    private final String password;
    private final String keyStoreType;

    private SSLContext sslContext;

    public CertService(@Value("${server.ssl.key-store-type}") String keyStoreType,
                       @Value("${server.ssl.key-store}") Resource keyStorePath,
                       @Value("${server.ssl.key-store-password}") String password) {
        this.keyStoreType = Objects.requireNonNull(keyStoreType, "keyStoreType");
        this.keyStorePath = Objects.requireNonNull(keyStorePath, "keyStorePath");
        this.password = Objects.requireNonNull(password, "password");
    }

    @PostConstruct
    public void importCertForLocalCall() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        logger.info("KeyStore type:{}", keyStore.getType());
        try (InputStream inputStream = keyStorePath.getInputStream()) {
            keyStore.load(inputStream, password.toCharArray());
        }


        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            logger.info("KeyStore aliases:{}", alias);
            Certificate certificate = keyStore.getCertificate(alias);
            logger.debug("Certificate:{}", certificate);
        }

        KeyManagerFactory kmf = newKeyManagerFactory(keyStore);
        TrustManagerFactory tmf = newTrustManagerFactory(keyStore);


        logger.info("KeyManagerFactory Algorithm:{} Provider:{}", kmf.getAlgorithm(), kmf.getProvider());
        logger.info("TrustManagerFactory Algorithm:{} Provider:{}", tmf.getAlgorithm(), tmf.getProvider());

        this.sslContext = newSSLContext(kmf, tmf);
    }

    private SSLContext newSSLContext(KeyManagerFactory kmf, TrustManagerFactory tmf) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");

        KeyManager[] keyManagers = kmf.getKeyManagers();
        TrustManager[] trustManagers = tmf.getTrustManagers();
        sslContext.init(keyManagers, trustManagers, new SecureRandom());

        return sslContext;
    }

    private TrustManagerFactory newTrustManagerFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        return tmf;
    }

    private KeyManagerFactory newKeyManagerFactory(KeyStore keyStore) throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        return kmf;
    }

    @Bean
    public SSLContext getSslContext() {
        return sslContext;
    }

}
