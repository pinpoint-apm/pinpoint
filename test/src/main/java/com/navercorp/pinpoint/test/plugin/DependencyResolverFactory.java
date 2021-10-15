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
package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.common.util.MapUtils;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 */
public class DependencyResolverFactory {

    private final RepositorySystem system;
    private final RepositorySystemSession session;

    public DependencyResolverFactory() {
        this(true, Collections.EMPTY_MAP);
    }

    public DependencyResolverFactory(Map<String, Object> sessionConfig) {
        this(true, sessionConfig);
    }

    public DependencyResolverFactory(boolean supportRemote, Map<String, Object> sessionConfig) {
        this.system = DependencyResolver.newRepositorySystem(supportRemote);

        // at org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession
        // The session config can be changed only with the system property.
        if (MapUtils.hasLength(sessionConfig)) {
            for (Map.Entry<String, Object> entry : sessionConfig.entrySet()) {
                System.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        this.session = DependencyResolver.newRepositorySystemSession(this.system);
    }

    public DependencyResolver get(String... repositoryUrls) {
        List<RemoteRepository> remoteRepositories = DependencyResolver.newRepositories(repositoryUrls);
        return new DependencyResolver(system, session, remoteRepositories);
    }
}
