package com.profiler;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ClassPathResolver {
//    Pattern.compile("hippo-tomcat-profiler-([0-9]+\\){2}.jar");
    private static final Pattern agentPattern = Pattern.compile("hippo-tomcat-profiler-[0-9]+\\.[0-9]+\\.[0-9]+\\.jar");

    private String classPath;

    private String agentJarName;
    private String agentJarFullPath;
    private String agentDirPath;

    public ClassPathResolver(String classPath) {
        this.classPath = classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
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
        this.agentDirPath =  parseAgentDirPath(agentJarFullPath);
        return true;
    }




    private String parseAgentJar(Matcher matcher) {
        int start = matcher.start();
        int end = matcher.end();
        return this.classPath.substring(start,  end);
    }


    public String getAgentJarName() {
        return this.agentJarName;
    }


    private String parseAgentJarPath(String classPath, String agentJar) {
        String[] classPathList = classPath.split(File.pathSeparator);
        for(String findPath : classPathList) {
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

    public  String getAgentLibPath() {
        return this.agentDirPath + File.separator + "lib";
    }

    public List<String> resolveLib() {


        return new ArrayList<String>();
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
}
