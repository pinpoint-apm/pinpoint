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
package com.navercorp.pinpoint.test.plugin.maven;

import com.navercorp.pinpoint.test.plugin.shared.ArtifactIdUtils;
import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryCache;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.version.Version;
import org.tinylog.TaggedLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static com.navercorp.pinpoint.test.plugin.util.CollectionUtils.union;

/**
 * @author Jongho Moon
 */
public class DependencyResolver {
    private static final String FOLLOW_PRECEEDING = "FOLLOW_PRECEEDING";

    private static final TaggedLogger logger = TestLogger.getLogger();

    private final List<RemoteRepository> repositories;
    private final RepositorySystem system;
    private final RepositorySystemSession session;

    static RepositorySystem newRepositorySystem(boolean supportRemote) {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        if (supportRemote) {
            locator.addService(TransporterFactory.class, org.eclipse.aether.transport.http.HttpTransporterFactory.class);
        }

        locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
                logger.error(exception, "serviceCreationFailed type:{}, impl:{} {}", type, impl);
            }
        });

        return locator.getService(RepositorySystem.class);
    }

    static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setCache(newRepositoryCache());

        MavenRepository mavenRepository = new MavenRepository();
        String localRepositoryPath = mavenRepository.resolveLocalRepository();
        if (logger.isInfoEnabled()) {
            logger.info("Local repository: {}", localRepositoryPath);
        }
        LocalRepository localRepository = new LocalRepository(localRepositoryPath);

        LocalRepositoryManager localRepositoryManager = system.newLocalRepositoryManager(session, localRepository);
        session.setLocalRepositoryManager(localRepositoryManager);

        return session;
    }

    private static RepositoryCache newRepositoryCache() {
        RepositoryCache cache = new DefaultRepositoryCache();
        final String enableTraceCache = System.getProperty("pinpoint.ittest.tracecache", "false");
        if (Boolean.parseBoolean(enableTraceCache)) {
            return new TraceRepositoryCache(cache);
        }
        return cache;
    }


    static List<RemoteRepository> newRepositories(String... urls) {
        List<RemoteRepository> repositories = new ArrayList<>(urls.length + 1);

        RemoteRepository mavenCentralRepository = newMavenCentralRepository();
        repositories.add(mavenCentralRepository);

        int localRepositoriesCount = 0;
        for (String url : urls) {
            RemoteRepository remoteRepository = new RemoteRepository.Builder("local" + localRepositoriesCount, "default", url).build();
            repositories.add(remoteRepository);
        }

        return repositories;
    }

    private static RemoteRepository newMavenCentralRepository() {
        final String mavenCentral = MavenCentral.getAddress();
        RemoteRepository.Builder builder = new RemoteRepository.Builder("central", "default", mavenCentral);
        return builder.build();
    }

    public DependencyResolver(RepositorySystem system, RepositorySystemSession session, List<RemoteRepository> repositories) {
        this.system = system;
        this.session = session;
        this.repositories = repositories;
    }

    public List<Version> getVersions(Artifact artifact, Predicate<String> filter) throws VersionRangeResolutionException {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(repositories);

        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);
        List<Version> versions = new ArrayList<>();
        if (filter != null) {
            for (Version version : rangeResult.getVersions()) {
                if (DependencyVersionFilter.NOT_FILTERED == filter.test(version.toString())) {
                    versions.add(version);
                }
            }
        } else {
            versions = new ArrayList<>(rangeResult.getVersions());
        }
        versions.sort(Comparator.naturalOrder());

        return versions;
    }

    public List<File> resolveArtifactsAndDependencies(String artifactsAsString) throws DependencyResolutionException {
        List<Artifact> artifactList = getArtifactList(artifactsAsString);
        return resolveArtifactsAndDependencies(artifactList);
    }

    private static List<Artifact> getArtifactList(String value) {
        if (value == null) {
            return Collections.emptyList();
        }

        String[] artifactNameArray = value.split(ArtifactIdUtils.ARTIFACT_SEPARATOR);
        return ArtifactIdUtils.toArtifact(artifactNameArray);
    }

    public List<File> resolveArtifactsAndDependencies(List<Artifact> artifacts) throws DependencyResolutionException {
        List<Dependency> dependencies = new ArrayList<>();

        for (Artifact artifact : artifacts) {
            dependencies.add(new Dependency(artifact, JavaScopes.RUNTIME));
        }

        CollectRequest collectRequest = new CollectRequest((Dependency) null, dependencies, repositories);
        DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(JavaScopes.RUNTIME);
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);
        DependencyResult result = system.resolveDependencies(session, dependencyRequest);

        List<File> files = new ArrayList<>();

        for (ArtifactResult artifactResult : result.getArtifactResults()) {
            files.add(artifactResult.getArtifact().getFile());
        }

        return files;
    }

    public String getNewestVersion(String groupId, String artifactId) throws VersionRangeResolutionException {
        Artifact artifact = new DefaultArtifact(groupId, artifactId, "jar", "[0,)");

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(repositories);

        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

        Version newestVersion = rangeResult.getHighestVersion();

        return newestVersion.toString();
    }

    public Map<String, List<Artifact>> resolveDependencySets(String... dependencies) {
        return resolveDependencySets(null, dependencies);
    }

    public Map<String, List<Artifact>> resolveDependencySets(Predicate<String> filter, String... dependencies) {
        List<List<Artifact>> companions = resolve(dependencies);

        List<List<List<Artifact>>> xxx = new ArrayList<>();

        for (List<Artifact> companion : companions) {

            Artifact representative = companion.get(0);
            List<Version> versions;

            try {
                versions = getVersions(representative, filter);
            } catch (VersionRangeResolutionException e) {
                throw new IllegalArgumentException("Fail to resolve version of: " + representative);
            }

            if (versions.isEmpty()) {
                throw new IllegalArgumentException("No version in the given range: " + representative);
            }

            List<List<Artifact>> companionVersions = new ArrayList<>(versions.size());

            for (Version version : versions) {
                List<Artifact> companionVersion = new ArrayList<>(companion.size());

                for (Artifact artifact : companion) {
                    Artifact verArtifact = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getExtension(), version.toString());
                    companionVersion.add(verArtifact);
                }

                companionVersions.add(companionVersion);
            }

            xxx.add(companionVersions);
        }

        return combination(xxx);
    }

    private List<List<Artifact>> resolve(String[] dependencies) {
        List<List<Artifact>> companions = new ArrayList<>();
        List<Artifact> lastCompanion = null;

        for (String dependency : dependencies) {
            int first = dependency.indexOf(':');
            if (first == -1) {
                throw new IllegalArgumentException("Bad artifact coordinates: " + dependency + ", artifacts: " + Arrays.deepToString(dependencies));
            }

            int second = dependency.indexOf(':', first + 1);
            if (second == -1) {
                dependency += ":" + FOLLOW_PRECEEDING;
            }

            Artifact artifact = new DefaultArtifact(dependency);

            if (FOLLOW_PRECEEDING.equals(artifact.getVersion())) {
                if (lastCompanion != null) {
                    lastCompanion.add(artifact);
                } else {
                    throw new IllegalArgumentException("Version is not specified: " + dependency + ", artifacts: " + Arrays.deepToString(dependencies));
                }
            } else {
                lastCompanion = new ArrayList<>();
                lastCompanion.add(artifact);
                companions.add(lastCompanion);
            }
        }
        return companions;
    }

    private Map<String, List<Artifact>> combination(List<List<List<Artifact>>> groups) {
        if (groups.size() == 1) {
            Map<String, List<Artifact>> result = new LinkedHashMap<>();
            List<List<Artifact>> group = groups.get(0);

            if (group.size() == 1) {
                result.put("", group.get(0));
            } else {
                for (List<Artifact> aCase : group) {
                    Artifact representative = aCase.get(0);
                    result.put(representative.getArtifactId() + "-" + representative.getVersion(), aCase);
                }
            }

            return result;
        }

        List<List<Artifact>> thisGroup = groups.get(0);
        Map<String, List<Artifact>> sub = combination(groups.subList(1, groups.size()));

        Map<String, List<Artifact>> result = new LinkedHashMap<>();

        if (thisGroup.size() == 1) {
            List<Artifact> thisArtifacts = thisGroup.get(0);

            for (Entry<String, List<Artifact>> subEntry : sub.entrySet()) {
                List<Artifact> subArtifacts = subEntry.getValue();
                List<Artifact> t = union(thisArtifacts, subArtifacts);

                result.put(subEntry.getKey(), t);
            }
        } else {
            for (List<Artifact> thisArtifacts : thisGroup) {
                Artifact representative = thisArtifacts.get(0);
                String thisKey = representative.getArtifactId() + "-" + representative.getVersion();

                for (Entry<String, List<Artifact>> subEntry : sub.entrySet()) {
                    List<Artifact> subArtifacts = subEntry.getValue();
                    List<Artifact> t = union(thisArtifacts, subArtifacts);

                    String subKey = subEntry.getKey();
                    String key = subKey.isEmpty() ? thisKey : thisKey + ", " + subKey;
                    result.put(key, t);
                }
            }

        }

        return result;
    }

}
