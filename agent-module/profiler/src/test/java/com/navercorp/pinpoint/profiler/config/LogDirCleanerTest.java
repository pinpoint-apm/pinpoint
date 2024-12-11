package com.navercorp.pinpoint.profiler.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


public class LogDirCleanerTest {
    @TempDir
    public Path temp;

    private long time = System.currentTimeMillis();

    private FileTime nextTime() {
        return FileTime.fromMillis(time += 10000);
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
        Path agentDir1 = newFolder("agentDir1");

        Path temp = agentDir1.resolve("tempFile1.txt");
        Files.createFile(temp);

        Path agentDir2 = newFolder("agentDir2");
        Path agentDir3 = newFolder("agentDir3");
    }

    private Path newFolder(String agentDir1) throws IOException {
        Path file = temp.resolve(agentDir1);
        Files.createDirectory(file);
        Files.setLastModifiedTime(file, nextTime());
        return file;
    }

    @Test
    public void clean0() {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 0);
        logDirCleaner.clean();

        String[] files = temp.toFile().list();
        assertThat(files).isEmpty();
    }

    @Test
    public void clean2() throws IOException {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 2);
        logDirCleaner.clean();

        List<Path> files = fileList(temp);
        assertThat(files).hasSize(2)
                .contains(Paths.get("agentDir2"))
                .contains(Paths.get("agentDir3"));
//                .contains("agentDir2")
//                .contains("agentDir3");
    }

    @Test
    public void clean5() throws IOException {
        LogDirCleaner logDirCleaner = new LogDirCleaner(temp, 5);
        logDirCleaner.clean();

        List<Path> files = fileList(temp);
        assertThat(files).hasSize(3);
    }

    public List<Path> fileList(Path path) throws IOException {
        try (Stream<Path> stream = Files.list(path)) {
            return stream
                    .map(Path::getFileName)
                    .collect(Collectors.toList());
        }
    }
}
