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
import com.google.common.base.Stopwatch;
import io.grpc.NameResolver;
import io.grpc.NameResolverProvider;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * copy from : https://github.com/grpc/grpc-java/blob/master/core/src/main/java/io/grpc/internal/DnsNameResolverProvider.java
 */
public final class PinpointDnsNameResolverProvider extends NameResolverProvider {

    private static final String SCHEME = "dns";

    private final Executor dnsExecutor;
    private final String name;

    public PinpointDnsNameResolverProvider(String name, Executor dnsExecutor) {
        this.name = Objects.requireNonNull(name, "name");
        this.dnsExecutor = Objects.requireNonNull(dnsExecutor, "dnsExecutorService");
    }

    @Override
    public DnsNameResolver newNameResolver(URI targetUri, NameResolver.Args args) {
        if (SCHEME.equals(targetUri.getScheme())) {
            String targetPath = Preconditions.checkNotNull(targetUri.getPath(), "targetPath");
            Preconditions.checkArgument(targetPath.startsWith("/"),
                    "the path component (%s) of the target (%s) must start with '/'", targetPath, targetUri);
            String name = targetPath.substring(1);
            return new DnsNameResolver(
                    targetUri.getAuthority(),
                    name,
                    args,
                    // rename thread
                    wrapExecutor(this.dnsExecutor),
                    Stopwatch.createUnstarted(), false);
        } else {
            return null;
        }
    }

    private SharedResourceHolder.Resource<Executor> wrapExecutor(final Executor executor) {
        return new SharedResourceHolder.Resource<Executor>() {
            @Override
            public Executor create() {
                return executor;
            }

            @Override
            public void close(Executor instance) {
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
