package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.test.plugin.util.ArrayUtils;
import com.navercorp.pinpoint.test.plugin.util.CodeSourceUtils;
import com.navercorp.pinpoint.test.plugin.util.StringUtils;
import com.navercorp.pinpoint.test.plugin.util.TestPluginVersion;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigResolver {

    public static final int NO_JVM_VERSION = -1;

    public AgentPathAndVersion getAgentPathAndVersion(PinpointAgent agent) {
        Path agentPath = getAgentPath(agent);
        String version = getVersion(agent);
        return new AgentPathAndVersion(agentPath, version);
    }

    public Path resolveAgentPath(AgentPathAndVersion agent) {
        Path relativePath = getRelativePath(agent.getPath(), agent.getVersion());

        Path parent = workDir().toPath();
        while (true) {
            Path candidate = parent.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate.toAbsolutePath();
            }

            parent = parent.getParent();

            if (parent == null) {
                throw new IllegalArgumentException("Cannot find agent path: " + relativePath);
            }
        }
    }

    public Path getAgentPath(PinpointAgent agent) {
        final Path defaultPath = Paths.get("agent/target/pinpoint-agent-" + TestPluginVersion.getVersion());
        if (agent == null) {
            return defaultPath;
        }
        if (StringUtils.hasLength(agent.value())) {
            return Paths.get(agent.value());
        }
        return defaultPath;
    }

    private String getVersion(PinpointAgent agent) {
        if (agent == null) {
            return TestPluginVersion.getVersion();
        }
        if (StringUtils.hasLength(agent.version())) {
            return agent.version();
        }
        return TestPluginVersion.getVersion();
    }

    public String resolveProfile(PinpointProfile profile) {
        if (profile == null) {
            return PinpointProfile.DEFAULT_PROFILE;
        }
        return profile.value();
    }

    public List<String> getJvmArguments(JvmArgument jvmArgument) {
        if (jvmArgument == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(jvmArgument.value());
    }

    public int[] getJvmVersion(JvmVersion jvmVersion) {
        if (jvmVersion == null) {
            return new int[]{NO_JVM_VERSION};
        }
        return jvmVersion.value();
    }

    public List<String> getImportPlugin(ImportPlugin importPlugin) {
        if (importPlugin == null) {
            return null;
        }
        String[] ids = importPlugin.value();
        if (ArrayUtils.isEmpty(ids)) {
            return null;
        }
        return Arrays.asList(ids);
    }

    public List<String> getRepository(Repository repository) {
        if (repository == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(repository.value());
    }

    public List<String> getTransformInclude(TransformInclude transformInclude) {
        if (transformInclude == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(transformInclude.value());
    }


    private Path getRelativePath(Path path, String version) {
        String agentJar = String.format("pinpoint-bootstrap-%s.jar", version);
        return path.resolve(agentJar);
    }

    public Path resolveConfigFileLocation(String configFile) {
        String resource = configFile.startsWith("/") ? configFile : "/" + configFile;
        URL url = getClass().getResource(resource);

        if (url != null) {
            return toPathString(url);
        }

        Path config = Paths.get(configFile);
        if (!Files.exists(config)) {
            throw new RuntimeException("Cannot find pinpoint configuration file: " + configFile);
        }
        return config.toAbsolutePath();
    }

    public static Path toPathString(URL url) {
        try {
            return Paths.get(url.toURI()).toAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isDebugMode() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    }

    public Path resolveTestClassLocation(Class<?> testClass) {
        final URL testClassLocation = CodeSourceUtils.getCodeLocation(testClass);
        if (testClassLocation == null) {
            throw new IllegalStateException(testClass + " url not found");
        }
        return toPathString(testClassLocation);
    }

    public static File workDir() {
        return new File(".").getAbsoluteFile();
    }

}
