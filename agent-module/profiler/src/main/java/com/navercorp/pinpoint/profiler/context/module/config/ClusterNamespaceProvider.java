package com.navercorp.pinpoint.profiler.context.module.config;

import java.util.function.Function;

public class ClusterNamespaceProvider {

    private static final String[] clusterNamespace = new String[] {
            // pinpoint V4 : property key of cluster namespace
            "profiler.cluster.namespace",
            // legacy key V1~V3
            "profiler.application.namespace",
    };

    public static String getNamespace(Function<String, String> profilerConfig) {
        for (String namespaceKey : clusterNamespace) {
            String namespace = profilerConfig.apply(namespaceKey);
            if (namespace != null) {
                return namespace;
            }
        }
        return null;
    }

}
