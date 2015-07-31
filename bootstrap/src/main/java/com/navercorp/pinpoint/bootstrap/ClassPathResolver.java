/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap;


import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emeroad
 */
public class ClassPathResolver {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private static final Pattern DEFAULT_AGENT_PATTERN = Pattern.compile("pinpoint-bootstrap(-[0-9]+\\.[0-9]+\\.[0-9]+(\\-SNAPSHOT)?)?\\.jar");
    private static final Pattern DEFAULT_AGENT_CORE_PATTERN = Pattern.compile("pinpoint-bootstrap-core(-[0-9]+\\.[0-9]+\\.[0-9]+(\\-SNAPSHOT)?)?\\.jar");

    private String classPath;

    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;
    private Pattern agentPattern;
    private Pattern agentCorePattern;
    private List<String> fileExtensionList;
    private String bootStrapCoreJar;

    public ClassPathResolver() {
        this(getClassPathFromSystemProperty());
    }


    public ClassPathResolver(String classPath) {
        this.classPath = classPath;
        this.agentPattern = DEFAULT_AGENT_PATTERN;
        this.agentCorePattern = DEFAULT_AGENT_CORE_PATTERN;
        this.fileExtensionList = getDefaultFileExtensionList();
    }

    public List<String> getDefaultFileExtensionList() {
        List<String> extensionList = new ArrayList<String>();
        extensionList.add("jar");
        extensionList.add("xml");
        extensionList.add("properties");
        return extensionList;
    }

    public ClassPathResolver(String classPath, String agentPattern) {
        this.classPath = classPath;
        this.agentPattern = Pattern.compile(agentPattern);
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    public void setClassPathFromSystemProperty() {
        this.classPath = getClassPathFromSystemProperty();
    }

    public static String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

    public boolean findAgentJar() {
        Matcher matcher = agentPattern.matcher(classPath);
        if (!matcher.find()) {
            return false;
        }
        this.agentJarName = parseAgentJar(matcher);
        this.agentJarFullPath = parseAgentJarPath(classPath, agentJarName);
        if (agentJarFullPath == null) {
            return false;
        }
        this.agentDirPath = parseAgentDirPath(agentJarFullPath);

        this.bootStrapCoreJar = findBootStrapCore();
        return true;
    }

    private String findBootStrapCore() {
        String bootDir = agentDirPath + File.separator + "boot";
        File file = new File(bootDir);
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = agentCorePattern.matcher(name);
                if (matcher.matches()) {
                    logger.info("found bootStrapCore. " + name);
                    return true;
                }
                return false;
            }
        });
        if (files== null || files.length == 0) {
            logger.info("bootStrapCore not found.");
            return null;
        } else if (files.length == 1) {
            return files[0].getAbsolutePath();
        } else {
            logger.info("too many bootStrapCore found. " + Arrays.toString(files));
            return null;
        }
    }

    public String getBootStrapCoreJar() {
        return bootStrapCoreJar;
    }

    private String parseAgentJar(Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();
        return this.classPath.substring(start, end);
    }


    public String getAgentJarName() {
        return this.agentJarName;
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

    public String getAgentJarFullPath() {
        return agentJarFullPath;
    }

    public String getAgentLibPath() {
        return this.agentDirPath + File.separator + "lib";
    }

    public String getAgentLogFilePath() {
        return this.agentDirPath + File.separator + "log";
    }

    public String getAgentPluginPath() {
        return this.agentDirPath + File.separator + "plugin";
    }

    public List<URL> resolveLib() {
        String agentLibPath = getAgentLibPath();
        File libDir = new File(agentLibPath);
        if (!libDir.exists()) {
            logger.warning(agentLibPath + " not found");
            return Collections.emptyList();
        }
        if (!libDir.isDirectory()) {
            logger.warning(agentLibPath + " not Directory");
            return Collections.emptyList();
        }
        final List<URL> jarURLList =  new ArrayList<URL>();

        final File[] findJarList = findjar(libDir);
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

        // hot fix. boot-strap-core.jar not found from classPool ??
        jarURLList.add(toURI(new File(getBootStrapCoreJar())));

        return jarURLList;
    }
    
    public URL[] resolvePlugins() {
        final File file = new File(getAgentPluginPath());
        
        if (!file.exists()) {
            logger.warning(file + " not found");
            return new URL[0];
        }
        
        if (!file.isDirectory()) {
            logger.warning(file + " is not a directory");
            return new URL[0];
        }
        
        
        final File[] jars = file.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (jars == null || jars.length == 0) {
            return new URL[0];
        }
        
        final URL[] urls = new URL[jars.length];
        
        
        for (int i = 0; i < jars.length; i++) {
            try {
                urls[i] = jars[i].toURI().toURL();
            } catch (MalformedURLException e) {
                // TODO have to change to PinpointException AFTER moving the exception to pinpoint-common
                throw new RuntimeException("Fail to load plugin jars", e);
            }
        }
        
        logger.info("Found plugins: " + Arrays.deepToString(jars));

        return urls;
    }

    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, file.getName() + ".toURL() failed. Error:" + e.getMessage(), e);
            return null;
        }
    }

    private File[] findjar(File libDir) {
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

    public String parseAgentDirPath(String agentJarFullPath) {
        int index1 = agentJarFullPath.lastIndexOf("/");
        int index2 = agentJarFullPath.lastIndexOf("\\");
        int max = Math.max(index1, index2);
        if (max == -1) {
            return null;
        }
        return agentJarFullPath.substring(0, max);
    }

    public String getAgentDirPath() {
        return agentDirPath;
    }

    public String getAgentConfigPath() {
        return agentDirPath + File.separator + "pinpoint.config";
    }

}
