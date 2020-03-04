/*
 * Copyright 2019 NAVER Corp.
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

package io.grpc.internal;

import com.google.common.base.Preconditions;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.Attributes;
import io.grpc.NameResolverProvider;

import java.net.URI;
import java.util.concurrent.ExecutorService;

/**
 * copy from : https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/DnsNameResolverProvider.java
 */
public final class PinpointDnsNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "dns";

    private final ExecutorService dnsExecutorService;
    private final String name;

    public PinpointDnsNameResolverProvider(String name, ExecutorService dnsExecutorService) {
        this.name = Assert.requireNonNull(name, "name");
        this.dnsExecutorService = Assert.requireNonNull(dnsExecutorService, "dnsExecutorService");
    }

    @Override
    public DnsNameResolver newNameResolver(URI targetUri, Attributes params) {
        if (SCHEME.equals(targetUri.getScheme())) {
            String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
            Preconditions.checkArgument(targetPath.startsWith("/"),
                    "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
            String name = targetPath.substring(1);
            return new DnsNameResolver(
                    targetUri.getAuthority(),
                    name,
                    params,
                    // rename thread
                    wrapDnsExecutor(this.dnsExecutorService),
                    GrpcUtil.getDefaultProxyDetector());
        } else {
            return null;
        }
    }

    private SharedResourceHolder.Resource<ExecutorService> wrapDnsExecutor(final ExecutorService dnsExecutorService) {
        return new SharedResourceHolder.Resource<ExecutorService>() {
            @Override
            public ExecutorService create() {
                return dnsExecutorService;
            }

            @Override
            public void close(ExecutorService instance) {
//                instance.shutdown();
                // ignore
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @Override
    public String getDefaultScheme() {
        return SCHEME;
    }

    @Override
    protected boolean isAvailable() {
        return true;
    }

    @Override
    protected int priority() {
        return 5;
    }
}
