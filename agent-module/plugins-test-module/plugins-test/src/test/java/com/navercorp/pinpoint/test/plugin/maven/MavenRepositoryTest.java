package com.navercorp.pinpoint.test.plugin.maven;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class MavenRepositoryTest {

    @Test
    void resolveLocalRepository_MAVEN_REPOSITORY(@TempDir Path temp) throws IOException {
        Path repository = temp.resolve("repository");
        Files.createDirectory(repository);

        MavenRepository mavenRepository = new MavenRepository() {
            @Override
            String getMavenRepositoryFromEnv() {
                return repository.toString();
            }
        };

        String localRepository = mavenRepository.resolveLocalRepository();
        Assertions.assertEquals(repository, Paths.get(localRepository));

    }

    @Test
    void resolveLocalRepository(@TempDir Path temp) throws IOException {
        Path m2 = temp.resolve(".m2");
        Files.createDirectory(m2);
        Path repository = m2.resolve("repository");
        Files.createDirectory(repository);

        MavenRepository mavenRepository = new MavenRepository() {
            @Override
            String getMavenRepositoryFromEnv() {
                return null;
            }

            @Override
            String getUserHome() {
                return temp.toString();
            }
        };

        String localRepository = mavenRepository.resolveLocalRepository();
        Assertions.assertEquals(repository, Paths.get(localRepository));

    }
}