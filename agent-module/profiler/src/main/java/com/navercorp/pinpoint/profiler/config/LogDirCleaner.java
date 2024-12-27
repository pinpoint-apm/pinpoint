package com.navercorp.pinpoint.profiler.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogDirCleaner {
    private final Path logPath;
    private final int maxSize;

    public LogDirCleaner(Path logPath, int maxSize) {
        this.logPath = Objects.requireNonNull(logPath, "logPath");
        this.maxSize = maxSize;
    }

    public void clean() {
        if (!Files.exists(logPath)) {
            return;
        }
        if (!Files.isDirectory(logPath)) {
            return;
        }
        List<Path> agentDirectories = directoryList();
        if (agentDirectories.isEmpty()) {
            return;
        }

        if (agentDirectories.size() > maxSize) {
            delete(agentDirectories);
        }
    }

    private List<Path> directoryList() {
        try (Stream<Path> stream = Files.list(logPath)) {
            return stream.filter(Files::isDirectory)
                    .sorted(Comparator.comparing(Path::toFile))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    private void delete(List<Path> agentDirectories) {
        agentDirectories.sort(Comparator.comparing(this::getLastModifiedTime));

        int removeSize = agentDirectories.size() - maxSize;
        List<Path> deleteTargets = agentDirectories.subList(0, removeSize);

        for (Path file : deleteTargets) {
            deleteAll(file);
        }

    }

    private long getLastModifiedTime(Path path) {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return lastModifiedTime.toMillis();
        } catch (IOException e) {
            return 0;
        }
    }


    private void deleteAll(Path file) {
        try (Stream<Path> paths = Files.walk(file)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(this::deletePath);
        } catch (IOException ignore) {
        }
    }

    public void deletePath(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ignore) {
        }
    }

}
