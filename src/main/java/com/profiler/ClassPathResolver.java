package com.profiler;


import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ClassPathResolver {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    //    Pattern.compile("hippo-tomcat-profiler-([0-9]+\\){2}.jar");
    private static final Pattern DEFAULT_AGENT_PATTERN = Pattern.compile("hippo-tomcat-profiler-[0-9]+\\.[0-9]+\\.[0-9]+\\.jar");

    private String classPath;

    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;
    private Pattern agentPattern;

    public ClassPathResolver() {
        this.classPath = getClassPathFromSystemProperty();
        this.agentPattern = DEFAULT_AGENT_PATTERN;
    }

    public ClassPathResolver(String classPath) {
        this.classPath = classPath;
        this.agentPattern = DEFAULT_AGENT_PATTERN;
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

    public String getClassPathFromSystemProperty() {
        return System.getProperty("java.class.path");
    }

    public boolean findAgentJar() {
        Matcher matcher = agentPattern.matcher(classPath);
        if (!matcher.find()) {
            return false;
        }
        this.agentJarName = parseAgentJar(matcher);
        this.agentJarFullPath = parseAgentJarPath(classPath, agentJarName);
        this.agentDirPath = parseAgentDirPath(agentJarFullPath);
        return true;
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
        File[] findJarList = findjar(libDir);
        List<URL> jarURLList = new ArrayList<URL>(findJarList.length);
        for (File file : findJarList) {
            URL url = toURI(file);
            if (url != null) {
                jarURLList.add(url);
            }
        }

        return jarURLList;
    }

    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private File[] findjar(File libDir) {
        return libDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().lastIndexOf(".jar") != -1;
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
        return agentDirPath + File.separator + "hippo.config";
    }

}
