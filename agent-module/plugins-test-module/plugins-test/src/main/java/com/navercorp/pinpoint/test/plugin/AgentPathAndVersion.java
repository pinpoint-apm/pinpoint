package com.navercorp.pinpoint.test.plugin;

import java.nio.file.Path;
import java.util.Objects;

public class AgentPathAndVersion {
    private final Path path;
    private final String version;

    public AgentPathAndVersion(Path path, String version) {
        this.path = Objects.requireNonNull(path, "path");
        this.version = Objects.requireNonNull(version, "version");
    }

    public Path getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }
}
