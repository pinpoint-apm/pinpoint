package com.navercorp.pinpoint.bootstrap.agentdir;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class LogDirCleanerTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder(getRootDir(LogDirCleanerTest.class));

    private long time = System.currentTimeMillis();

    private long nextTime() {
        return time += 10000;
    }

    private static File getRootDir(Class clazz) {
        String classPath = clazz.getName().replace('.', '/') + ".class";
        URL resource = clazz.getClassLoader().getResource(classPath);
        int index = resource.getPath().indexOf(classPath);
        if (index == -1) {
            throw new RuntimeException("RootDir error " + resource);
        }
        String rootPath = resource.getPath().substring(0, index);
        return new File(rootPath);
    }


    @Before
    public void setUp() throws Exception {
        File agentDir1 = newFolder("agentDir1");

        File temp = new File(agentDir1.getPath() + File.separator + "tempFile1.txt");
        temp.createNewFile();

        File agentDir2 = newFolder("agentDir2");
        File agentDir3 = newFolder("agentDir3");
    }

    private File newFolder(String agentDir1) throws IOException {
        File file = temp.newFolder(agentDir1);
        file.setLastModified(nextTime());
        return file;
    }

    @Test
    public void clean0() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp.getRoot().getPath(), 0);
        logDirCleaner.clean();

        String[] files = temp.getRoot().list();
        org.junit.Assert.assertEquals(0, files.length);
    }

    @Test
    public void clean2() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp.getRoot().getPath(), 2);
        logDirCleaner.clean();

        List<String> files = Arrays.asList(temp.getRoot().list());
        org.junit.Assert.assertEquals(2, files.size());
        org.junit.Assert.assertTrue(files.contains("agentDir2"));
        org.junit.Assert.assertTrue(files.contains("agentDir3"));
    }

    @Test
    public void clean5() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp.getRoot().getPath(), 5);
        logDirCleaner.clean();

        String[] files = temp.getRoot().list();
        org.junit.Assert.assertEquals(3, files.length);
    }
}