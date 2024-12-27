package com.navercorp.pinpoint.bootstrap.agentdir;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

    @Test
    void subpathAfterLast() {
        Path path = Paths.get("1", "2", "3", "4", "5");
        Path lastPath = FileUtils.subpathAfterLast(path, 2);
        assertEquals(Paths.get("4", "5"), lastPath);
    }

    @Test
    void subpathAfter() {
        Path path = Paths.get("1", "2", "3", "4", "5");
        Path lastPath = FileUtils.subpathAfter(path, 2);
        assertEquals(Paths.get("3", "4", "5"), lastPath);
    }
}