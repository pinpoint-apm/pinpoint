package com.navercorp.pinpoint.profiler.context.provider.grpc;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.grpc.client.config.SslOption;
import com.navercorp.pinpoint.grpc.security.SslContextFactory;
import com.navercorp.pinpoint.grpc.util.Resource;
import com.navercorp.pinpoint.profiler.context.grpc.config.GrpcTransportConfig;
import io.netty.handler.ssl.SslContext;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SSLContextProvider implements Provider<SslContext> {

    private final SslOption sslOption;

    @Inject
    public SSLContextProvider(GrpcTransportConfig grpcTransportConfig) {
        Objects.requireNonNull(grpcTransportConfig, "grpcTransportConfig");
        this.sslOption = grpcTransportConfig.getSslOption();
    }

    @Override
    public SslContext get() {
        try {
            if (!sslOption.isEnable()) {
                return null;
            }

            String providerType = sslOption.getProviderType();
            SslContextFactory factory = new SslContextFactory(providerType);

            Resource trustCertResource = sslOption.getTrustCertResource();
            if (trustCertResource.exists()) {
                return factory.forClient(trustCertResource.getInputStream());
            } else {
                return factory.forClient();
            }
        } catch (Exception e) {
            throw new PinpointException(e);
        }
    }
}
