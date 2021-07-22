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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jongho Moon
 */
public class DependencyResolver {
    private static final String FOLLOW_PRECEEDING = "FOLLOW_PRECEEDING";
    private static final String DEFAULT_LOCAL_REPOSITORY = "target/local-repo";

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

    private static class TraceRepositoryCache implements RepositoryCache {
        private final RepositoryCache delegate = new DefaultRepositoryCache();
        private final AtomicInteger hit = new AtomicInteger();
        private final AtomicInteger miss = new AtomicInteger();

        @Override
        public void put(RepositorySystemSession session, Object key, Object data) {
            if (logger.isInfoEnabled()) {
                logger.info("cache-put:{} {} {}", session, key, data);
            }
            delegate.put(session, key, data);
        }

        @Override
        public Object get(RepositorySystemSession session, Object key) {
            final Object result = delegate.get(session, key);
            if (result == null) {
                int count = miss.incrementAndGet();
                if (logger.isInfoEnabled()) {
                    logger.info("cache-get-miss-{}:{} {}", count, session, key);
                }
            } else {
                int count = hit.incrementAndGet();
                if (logger.isInfoEnabled()) {
                    logger.info("cache-get-hit-{}:{} {} result:{}", count, session, key, result);
                }
            }
            return result;
        }
    }

    static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setCache(newRepositoryCache());
        String localRepositoryPath = resolveLocalRepository();
        if (logger.isInfoEnabled()) {
            logger.info("Local repository: {}", localRepositoryPath);
        }
        LocalRepository localRepository = new LocalRepository(localRepositoryPath);

        LocalRepositoryManager localRepositoryManager = system.newLocalRepositoryManager(session, localRepository);
        session.setLocalRepositoryManager(localRepositoryManager);

        return session;
    }

    private static RepositoryCache newRepositoryCache() {
        final String enableTraceCache = System.getProperty("pinpoint.ittest.tracecache", "false");
        if (Boolean.parseBoolean(enableTraceCache)) {
            return new TraceRepositoryCache();
        }
        return new DefaultRepositoryCache();
    }

    private static String resolveLocalRepository() {
        String userHome = System.getProperty("user.home");

        if (userHome == null) {
            logger.info("Cannot find user.home property. Use default local repository");
            return DEFAULT_LOCAL_REPOSITORY;
        }

        File mavenHomeDir = new File(userHome, ".m2");

        if (!mavenHomeDir.exists() || !mavenHomeDir.isDirectory()) {
            logger.debug("Cannot find maven home directory {}. Use default local repository", mavenHomeDir);
            return DEFAULT_LOCAL_REPOSITORY;
        }

        File localRepository = new File(mavenHomeDir, "repository");
        File mavenConfigFile = new File(mavenHomeDir, "settings.xml");

        if (mavenConfigFile.exists() && mavenConfigFile.isFile()) {
            try {
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = builder.parse(mavenConfigFile);
                NodeList nodeList = document.getElementsByTagName("localRepository");

                if (nodeList.getLength() != 0) {
                    Node node = nodeList.item(0);
                    localRepository = new File(node.getTextContent());

                    logger.info("Use local repository {} configured at {}", localRepository, mavenConfigFile);
                }
            } catch (Exception e) {
                logger.info(e, "Fail to read maven configuration file: {}. Use default local repository", mavenConfigFile);
            }
        }

        if (localRepository.exists() && localRepository.isDirectory()) {
            return localRepository.getAbsolutePath();
        }

        logger.info("Local repository {} is not exists. Use default local repository", localRepository);

        return DEFAULT_LOCAL_REPOSITORY;
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

    public List<Version> getVersions(Artifact artifact) throws VersionRangeResolutionException {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(artifact);
        rangeRequest.setRepositories(repositories);

        VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);

        List<Version> versions = new ArrayList<>(rangeResult.getVersions());
        Collections.sort(versions);

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
        List<List<Artifact>> companions = resolve(dependencies);

        List<List<List<Artifact>>> xxx = new ArrayList<>();

        for (List<Artifact> companion : companions) {

            Artifact representative = companion.get(0);
            List<Version> versions;

            try {
                versions = getVersions(representative);
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

        Map<String, List<Artifact>> result = combination(xxx);

        return result;
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
                List<Artifact> t = new ArrayList<>(thisArtifacts.size() + subArtifacts.size());
                t.addAll(thisArtifacts);
                t.addAll(subArtifacts);

                result.put(subEntry.getKey(), t);
            }
        } else {
            for (List<Artifact> thisArtifacts : thisGroup) {
                Artifact representative = thisArtifacts.get(0);
                String thisKey = representative.getArtifactId() + "-" + representative.getVersion();

                for (Entry<String, List<Artifact>> subEntry : sub.entrySet()) {
                    List<Artifact> subArtifacts = subEntry.getValue();
                    List<Artifact> t = new ArrayList<>(thisArtifacts.size() + subArtifacts.size());
                    t.addAll(thisArtifacts);
                    t.addAll(subArtifacts);

                    String subKey = subEntry.getKey();
                    String key = subKey.isEmpty() ? thisKey : thisKey + ", " + subKey;
                    result.put(key, t);
                }
            }

        }

        return result;
    }

}
