package com.nhn.pinpoint.bootstrap;


import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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

    private String classPath;

    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;
    private Pattern agentPattern;
    private List<String> fileExtensionList;

    public ClassPathResolver() {
        this(getClassPathFromSystemProperty());
    }


    public ClassPathResolver(String classPath) {
        this.classPath = classPath;
        this.agentPattern = DEFAULT_AGENT_PATTERN;
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

    public String getAgentLogFilePath() {
        return this.agentDirPath + File.separator + "log";
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

        // agentDir 패스도 넣어야 xml을 찾을 때 해당 패스에서 찾음.
        URL agentDirUri = toURI(new File(agentLibPath));
        if (agentDirUri != null) {
            jarURLList.add(agentDirUri);
        }

        return jarURLList;
    }

    private URL toURI(File file) {
        URI uri = file.toURI();
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, file.getName() + ".toURL() fail. Caused:" + e.getMessage(), e);
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
