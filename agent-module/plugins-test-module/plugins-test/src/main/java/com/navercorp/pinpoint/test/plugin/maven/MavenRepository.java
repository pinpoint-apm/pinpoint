package com.navercorp.pinpoint.test.plugin.maven;

import com.navercorp.pinpoint.test.plugin.util.TestLogger;
import org.junit.platform.commons.JUnitException;
import org.tinylog.TaggedLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MavenRepository {

    private static final String DEFAULT_LOCAL_REPOSITORY = "target/local-repo";

    private final TaggedLogger logger = TestLogger.getLogger();

    public String resolveLocalRepository() {
        final Path mavenRepository = getMavenRepository();
        if (mavenRepository != null) {
            return mavenRepository.toAbsolutePath().toString();
        }
        // default path
        final String userHome = getUserHome();
        if (userHome == null) {
            logger.warn("Cannot find user.home property. Use default local repository");
            return DEFAULT_LOCAL_REPOSITORY;
        }

        final Path mavenHomeDir = Paths.get(userHome, ".m2");
        if (!isDirectory(mavenHomeDir)) {
            logger.debug("Cannot find maven home directory {}. Use default local repository", mavenHomeDir);
            return DEFAULT_LOCAL_REPOSITORY;
        }

        Path mavenConfigFile = mavenHomeDir.resolve("settings.xml");
        if (isFile(mavenConfigFile)) {
            final List<String> localRepoList = getRepositoryFromSettings(mavenConfigFile);
            for (String localRepo : localRepoList) {
                final Path localRepoPath = Paths.get(localRepo);
                if (isDirectory(localRepoPath)) {
                    logger.info("Use local repository {} configured at {}", localRepoPath, mavenConfigFile);
                    return localRepoPath.toAbsolutePath().toString();
                }
            }
        }
        Path localRepository = mavenHomeDir.resolve("repository");
        if (isDirectory(localRepository)) {
            return localRepository.toAbsolutePath().toString();
        }

        logger.info("Local repository {} is not exists. Use default local repository", localRepository);

        return DEFAULT_LOCAL_REPOSITORY;
    }

    public List<String> getRepositoryFromSettings(Path mavenConfigFile) {
        try {
            Document document = parse(mavenConfigFile);
            NodeList nodeList = document.getElementsByTagName("localRepository");

            List<String> repositorySettings = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(0);
                repositorySettings.add(node.getTextContent());
            }
            return repositorySettings;
        } catch (Exception e) {
            logger.warn(e, "Fail to read maven configuration file: {}. Use default local repository", mavenConfigFile);
        }
        return Collections.emptyList();
    }

    private Document parse(Path mavenConfigFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (InputStream is = Files.newInputStream(mavenConfigFile)) {
            return builder.parse(is);
        }
    }


    private Path getMavenRepository() {
        final String mavenRepo = getMavenRepositoryFromEnv();
        if (mavenRepo == null) {
            return null;
        }

        final Path mavenRepoPath = Paths.get(mavenRepo);
        if (isDirectory(mavenRepoPath)) {
            return mavenRepoPath;
        } else {
            throw new JUnitException("MAVEN_REPOSITORY is not a directory: " + mavenRepo);
        }
    }

    String getUserHome() {
        return System.getProperty("user.home");
    }

    String getMavenRepositoryFromEnv() {
        return System.getenv("MAVEN_REPOSITORY");
    }

    private static boolean isDirectory(Path path) {
        return Files.exists(path) && Files.isDirectory(path);
    }

    private static boolean isFile(Path path) {
        return Files.exists(path) && Files.isRegularFile(path);
    }
}
