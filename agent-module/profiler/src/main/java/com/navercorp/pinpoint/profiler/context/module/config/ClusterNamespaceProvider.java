package com.navercorp.pinpoint.profiler.context.module.config;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import jakarta.inject.Provider;

import java.util.function.Function;

public class ClusterNamespaceProvider implements Provider<String> {

    private static final String[] clusterNamespaceKeys = new String[] {
            // pinpoint V4 : property key of cluster namespace
            "profiler.cluster.namespace",
            // legacy key V1~V3
            "profiler.application.namespace",
    };

    private final String clusterNamespace;

    @Inject
    public ClusterNamespaceProvider(ProfilerConfig properties) {
        this(properties::readString);
    }

    public ClusterNamespaceProvider(Function<String, String> properties) {
        this.clusterNamespace = getNamespace(properties);
    }

    private static String getNamespace(Function<String, String> profilerConfig) {
        for (String namespaceKey : clusterNamespaceKeys) {
            String namespace = profilerConfig.apply(namespaceKey);
            if (namespace != null) {
                return namespace;
            }
        }
        return null;
    }

    @Override
    public String get() {
        return clusterNamespace;
    }
}
