/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.agentdir;


import com.navercorp.pinpoint.bootstrap.BootLogger;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public class AgentDirBaseClassPathResolver implements ClassPathResolver {

    private final BootLogger logger = BootLogger.getLogger(this.getClass().getName());

    static final String VERSION_PATTERN = "(-[0-9]+\\.[0-9]+\\.[0-9]+((\\-SNAPSHOT)|(-RC[0-9]+))?)?";

    static final Pattern DEFAULT_AGENT_BOOTSTRAP_PATTERN = compile("pinpoint-bootstrap" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_COMMONS_PATTERN = compile("pinpoint-commons" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_CORE_PATTERN = compile("pinpoint-bootstrap-core" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_JAVA9_PATTERN = compile("pinpoint-bootstrap-java9" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_AGENT_CORE_OPTIONAL_PATTERN = compile("pinpoint-bootstrap-core-optional" + VERSION_PATTERN + "\\.jar");
    static final Pattern DEFAULT_ANNOTATIONS = compile("pinpoint-annotations" + VERSION_PATTERN + "\\.jar");

    private final Pattern agentBootstrapPattern;
    private final Pattern agentCommonsPattern;
    private final Pattern agentCorePattern;
    private final Pattern agentJava9Pattern;
    private final Pattern agentCoreOptionalPattern;
    private final Pattern annotationsPattern;

    private final String classPath;
    private List<String> fileExtensionList;


    private static Pattern compile(String regex) {
        return Pattern.compile(regex);
    }

    public AgentDirBaseClassPathResolver(String classPath) {
        if (classPath == null) {
            throw new NullPointerException("classPath must not be null");
        }
        this.classPath = classPath;
        this.agentBootstrapPattern = DEFAULT_AGENT_BOOTSTRAP_PATTERN;
        this.agentCommonsPattern = DEFAULT_AGENT_COMMONS_PATTERN;
        this.agentCorePattern = DEFAULT_AGENT_CORE_PATTERN;
        this.agentJava9Pattern = DEFAULT_AGENT_JAVA9_PATTERN;
        this.agentCoreOptionalPattern = DEFAULT_AGENT_CORE_OPTIONAL_PATTERN;
        this.annotationsPattern = DEFAULT_ANNOTATIONS;
        this.fileExtensionList = getDefaultFileExtensionList();
    }

    static List<String> getDefaultFileExtensionList() {
        List<String> extensionList = new ArrayList<String>(3);
        extensionList.add("jar");
        extensionList.add("xml");
        extensionList.add("properties");
        return extensionList;
    }


    @Override
    public AgentDirectory resolve() {

        // find boot-strap.jar
        final String bootstrapJarName = this.findBootstrapJar(this.classPath);
        if (bootstrapJarName == null) {
            throw new IllegalStateException("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar not found.");
        }

        final String agentJarFullPath = parseAgentJarPath(classPath, bootstrapJarName);
        if (agentJarFullPath == null) {
            throw new IllegalStateException("pinpoint-bootstrap-x.x.x(-SNAPSHOT).jar not found. " + classPath);
        }
        final String agentDirPath = getAgentDirPath(agentJarFullPath);

        final BootDir bootDir = resolveBootDir(agentDirPath);

        final String agentLibPath = getAgentLibPath(agentDirPath);
        final List<URL> libs = resolveLib(agentLibPath, bootDir);

        String agentPluginPath = getAgentPluginPath(agentDirPath);
        final List<String> plugins = resolvePlugins(agentPluginPath);

        final AgentDirectory agentDirectory = new AgentDirectory(bootstrapJarName, agentJarFullPath, agentDirPath,
                bootDir, libs, plugins);

        return agentDirectory;
    }

    private String getAgentDirPath(String agentJarFullPath) {
        String agentDirPath = parseAgentDirPath(agentJarFullPath);
        if (agentDirPath == null) {
            throw new IllegalStateException("agentDirPath is null " + classPath);
        }

        logger.info("Agent original-path:" + agentDirPath);
        // defense alias change
        agentDirPath = toCanonicalPath(agentDirPath);
        logger.info("Agent canonical-path:" + agentDirPath);
        return agentDirPath;
    }


    private BootDir resolveBootDir(String agentDirPath) {
        String bootDirPath = agentDirPath + File.separator + "boot";
        String pinpointCommonsJar = find(bootDirPath, "pinpoint-commons.jar", agentCommonsPattern);
        String bootStrapCoreJar = find(bootDirPath, "pinpoint-bootstrap-core.jar", agentCorePattern);
        String bootStrapJava9Jar = find(bootDirPath, "pinpoint-bootstrap-java9.jar", agentJava9Pattern);
        String bootStrapCoreOptionalJar = find(bootDirPath, "pinpoint-bootstrap-core-optional.jar", agentCoreOptionalPattern);
        String annotationsJar = find(bootDirPath,"pinpoint-annotations.jar", annotationsPattern);
        return new BootDir(pinpointCommonsJar, bootStrapCoreJar, bootStrapCoreOptionalJar, bootStrapJava9Jar, annotationsJar);
    }


    String findBootstrapJar(String classPath) {
        final Matcher matcher = agentBootstrapPattern.matcher(classPath);
        if (!matcher.find()) {
            return null;
        }
        return parseAgentJar(matcher, classPath);
    }


    private String toCanonicalPath(String path) {
        final File file = new File(path);
        return toCanonicalPath(file);
    }

    private String toCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.warn(file.getPath() + " getCanonicalPath() error. Error:" + e.getMessage(), e);
            return file.getAbsolutePath();
        }
    }

    private String find(String bootDirPath, final String name, final Pattern pattern) {
        final File[] files = listFiles(name, pattern, bootDirPath);
        if (isEmpty(files)) {
            logger.info(name + " not found.");
            return null;
        } else if (files.length == 1) {
            File file = files[0];
            return toCanonicalPath(file);
        } else {
            logger.info("too many " + name + " found. " + Arrays.toString(files));
            return null;
        }
    }

    private boolean isEmpty(File[] files) {
        return files == null || files.length == 0;
    }

    private File[] listFiles(final String name, final Pattern pattern, String bootDirPath) {
        File bootDir = new File(bootDirPath);
        return bootDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String fileName) {
                Matcher matcher = pattern.matcher(fileName);
                if (matcher.matches()) {

                    logger.info("found " + name + ". " + dir.getAbsolutePath() + File.separator + fileName);
                    return true;
                }
                return false;
            }
        });
    }


    private String parseAgentJar(Matcher matcher, String classpath) {
        int start = matcher.start();
        int end = matcher.end();
        return classPath.substring(start, end);
    }

    private String parseAgentJarPath(String classPath, String agentJar) {
        String[] classPathList = classPath.split(File.pathSeparator);
        for (String findPath : classPathList) {
            boolean find = findPath.contains(agentJar);
            if (find) {
                return findPath;
            }
        }
        return null;
    }


    private String getAgentLibPath(String agentDirPath) {
        return agentDirPath + File.separator + "lib";
    }

    private String getAgentPluginPath(String agentDirPath) {
        return agentDirPath + File.separator + "plugin";
    }

    private List<URL> resolveLib(String agentLibPath, BootDir bootDir) {
        File libDir = new File(agentLibPath);
        if (checkDirectory(libDir)) {
            return Collections.emptyList();
        }
        final List<URL> jarURLList = new ArrayList<URL>();

        final File[] findJarList = findJar(libDir);
        if (findJarList != null) {
            for (File file : findJarList) {
                URL url = toURI(file);
                if (url != null) {
                    jarURLList.add(url);
                }
            }
        }

        URL agentDirUri = toURI(new File(agentLibPath));
        if (agentDirUri != null) {
            jarURLList.add(agentDirUri);
        }

        // hot fix. boot jars not found from classPool ??
//        jarURLList.add(toURI(new File(bootDir.getCommons())));
//        jarURLList.add(toURI(new File(bootDir.getBootstrapCore())));
//        String bootstrapCoreOptionalJar = bootDir.getBootstrapCoreOptional();
//        // bootstrap-core-optional jar is not required and is okay to be null
//        if (bootstrapCoreOptionalJar != null) {
//            jarURLList.add(toURI(new File(bootstrapCoreOptionalJar)));
//        }
        return jarURLList;
    }

    private List<String> resolvePlugins(String agentPluginPath) {
        final File directory = new File(agentPluginPath);

        if (checkDirectory(directory)) {
            logger.warn(directory + " is not a directory");
            return Collections.emptyList();
        }


        final File[] jars = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                  return name.endsWith(".jar");
            }
        });

        if (isEmpty(jars)) {
            return Collections.emptyList();
        }

        List<String> pluginFileList = filterReadPermission(jars);
        for (String pluginJar : pluginFileList) {
            logger.info("Found plugins:" + pluginJar);
        }
        return pluginFileList;
    }

    private boolean checkDirectory(File file) {
        if (!file.exists()) {
            logger.warn(file + " not found");
            return true;
        }
        if (!file.isDirectory()) {
            logger.warn(file + " is not a directory");
            return true;
        }
        return false;
    }

    private List<String> filterReadPermission(File[] jars) {
        List<String> result = new ArrayList<String>();
        for (File pluginJar : jars) {
            if (!pluginJar.canRead()) {
                logger.info("File '" + pluginJar + "' cannot be read");
                continue;
            }

            result.add(pluginJar.getPath());
        }
        return result;
    }

    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            logger.warn(file.getName() + ".toURL() failed.", e);
            return null;
        }
    }

    private File[] findJar(File libDir) {
        return libDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                for (String extension : fileExtensionList) {
                    if (path.lastIndexOf("." + extension) != -1) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int max = Math.max(index1, index2);
        if (max == -1) {
            return null;
        }
        return agentJarFullPath.substring(0, max);
    }

}
