package com.navercorp.pinpoint.bootstrap.agentdir;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class LogDirCleanerTest {
    @TempDir
    public Path temp;

    private long time = System.currentTimeMillis();

    private long nextTime() {
        return time += 10000;
    }

//    private static File getRootDir(Class<?> clazz) {
//        String classPath = clazz.getName().replace('.', '/') + ".class";
//        URL resource = clazz.getClassLoader().getResource(classPath);
//        int index = resource.getPath().indexOf(classPath);
//        if (index == -1) {
//            throw new RuntimeException("RootDir error " + resource);
//        }
//        String rootPath = resource.getPath().substring(0, index);
//        return new File(rootPath);
//    }


    @BeforeEach
    public void setUp() throws Exception {
        File agentDir1 = newFolder("agentDir1");

        Path temp = Paths.get(agentDir1.getPath(), "tempFile1.txt");
        Files.createFile(temp);

        File agentDir2 = newFolder("agentDir2");
        File agentDir3 = newFolder("agentDir3");
    }

    private File newFolder(String agentDir1) throws IOException {
        File file = new File(temp.resolve(agentDir1).toString());
        file.mkdir();
        file.setLastModified(nextTime());
        return file;
    }

    @Test
    public void clean0() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 0);
        logDirCleaner.clean();

        String[] files = temp.toFile().list();
        Assertions.assertEquals(0, files.length);
    }

    @Test
    public void clean2() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 2);
        logDirCleaner.clean();

        List<String> files = Arrays.asList(temp.toFile().list());
        Assertions.assertEquals(2, files.size());
        Assertions.assertTrue(files.contains("agentDir2"));
        Assertions.assertTrue(files.contains("agentDir3"));
    }

    @Test
    public void clean5() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 5);
        logDirCleaner.clean();

        String[] files = temp.toFile().list();
        Assertions.assertEquals(3, files.length);
    }
}