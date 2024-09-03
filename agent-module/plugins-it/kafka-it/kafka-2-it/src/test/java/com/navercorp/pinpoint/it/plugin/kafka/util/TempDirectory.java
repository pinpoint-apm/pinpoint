package com.navercorp.pinpoint.it.plugin.kafka.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class TempDirectory {
    protected static final Logger logger = LogManager.getLogger(TempDirectory.class);

    public static void deleteTempDirectory(Path temp) {
        if (temp != null) {
            try {
                Files.walk(temp)
                        .sorted(Comparator.reverseOrder())
                        .forEach(TempDirectory::deleteTemp);
            } catch (IOException ioe) {
                logger.warn("deleteTempDirectory failed", ioe);
            }

        }
    }

    public static void deleteTemp(Path path) {
        try {
            Files.delete(path);
        } catch (IOException ioe) {
            logger.warn("deleteTemp failed", ioe);
        }
    }
}
