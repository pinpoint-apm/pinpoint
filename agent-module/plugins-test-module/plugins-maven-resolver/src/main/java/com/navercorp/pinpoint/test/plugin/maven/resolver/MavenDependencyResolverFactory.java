/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.plugin.maven.resolver;

import com.navercorp.pinpoint.test.plugin.maven.DependencyResolver;
import com.navercorp.pinpoint.test.plugin.maven.DependencyResolverFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Instantiated by name from {@code DependencyResolverFactoryLoader}, so the {@code Map} constructor must stay.
 *
 * @author emeroad
 */
public class MavenDependencyResolverFactory implements DependencyResolverFactory {

    private final RepositorySystem system;
    private final RepositorySystemSession session;

    public MavenDependencyResolverFactory() {
        this(true, Collections.emptyMap());
    }

    public MavenDependencyResolverFactory(Map<String, Object> sessionConfig) {
        this(true, sessionConfig);
    }

    public MavenDependencyResolverFactory(boolean supportRemote, Map<String, Object> sessionConfig) {
        Objects.requireNonNull(sessionConfig, "sessionConfig");
        this.system = MavenDependencyResolver.newRepositorySystem(supportRemote);
        this.session = MavenDependencyResolver.newRepositorySystemSession(this.system, sessionConfig);
    }

    @Override
    public DependencyResolver get(List<String> repositoryUrls) {
        Objects.requireNonNull(repositoryUrls, "repositoryUrls");
        return get(repositoryUrls.toArray(new String[0]));
    }

    @Override
    public DependencyResolver get(String... repositoryUrls) {
        List<RemoteRepository> remoteRepositories = MavenDependencyResolver.newRepositories(repositoryUrls);
        return new MavenDependencyResolver(system, session, remoteRepositories);
    }
}
