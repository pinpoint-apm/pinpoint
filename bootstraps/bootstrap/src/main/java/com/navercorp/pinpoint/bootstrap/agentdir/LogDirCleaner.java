package com.navercorp.pinpoint.bootstrap.agentdir;

import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class LogDirCleaner {
    private final BootLogger logger = BootLogger.getLogger(this.getClass());

    private final Path logPath;
    private final int maxSize;

    public LogDirCleaner(Path logPath, int maxSize) {
        this.logPath = Objects.requireNonNull(logPath, "logPath");
        this.maxSize = maxSize;
    }

    public void clean() {
        File file = logPath.toFile();
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            logger.warn(logPath + " is not directory");
            return;
        }
        File[] agentDirectories = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (agentDirectories == null) {
            return;
        }

        if (agentDirectories.length > maxSize) {
            delete(agentDirectories);
        }
    }

    private void delete(File[] agentDirectories) {

        Arrays.sort(agentDirectories, new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                return Long.compare(file1.lastModified(), file2.lastModified());
            }
        });


        int removeSize = agentDirectories.length - maxSize;
        File[] deleteTargets = Arrays.copyOfRange(agentDirectories, 0, removeSize);

        for (File file : deleteTargets) {
            logger.info("delete agent dir:" + file.getAbsolutePath());
            deleteAll(file);
        }

    }


    private void deleteAll(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File curFile : files) {
                    deleteAll(curFile);
                }
            }
        }
        if (!file.delete()) {
            logger.info("delete error :" + file.getPath());
        }
    }

}
