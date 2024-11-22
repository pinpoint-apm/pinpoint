package com.navercorp.pinpoint.test.plugin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaHomeResolver {

    public static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("JAVA_(\\d+)_HOME");

    public static final int NO_JVM_VERSION = -1;

    private final List<JavaHome> javaHomes;

    public static JavaHomeResolver ofSystemEnv() {
        return new JavaHomeResolver(System.getenv());
    }

    public JavaHomeResolver(Map<String, String> env) {
        Objects.requireNonNull(env, "env");

        List<JavaHome> javaHomes = resolveJavaHomes(env);
        javaHomes.add(defaultJavaHome());
        this.javaHomes = javaHomes;
    }

    private JavaHome defaultJavaHome() {
        String defaultHome = System.getProperty("java.home");;
        return new JavaHome(defaultHome, NO_JVM_VERSION);
    }

    private List<JavaHome> resolveJavaHomes(Map<String, String> env) {
        List<JavaHome> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = entry.getKey();
            Matcher matcher = JAVA_VERSION_PATTERN.matcher(key);
            if (matcher.matches()) {
                int version = Integer.parseInt(matcher.group(1));
                String path = entry.getValue();
                JavaHome javaHome = new JavaHome(path, version);
                result.add(javaHome);
            }
        }
        return result;
    }

    public List<JavaHome> getJavaHomes() {
        return javaHomes;
    }

    public String buildJavaExecutable(int version) {
        final JavaHome javaHome = findJavaHome(version);
        if (javaHome == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(javaHome.home());
        builder.append(File.separatorChar);
        builder.append("bin");
        builder.append(File.separatorChar);
        builder.append("java");

        if (OsUtils.isWindows()) {
            builder.append(".exe");
        }

        return builder.toString();
    }

    private JavaHome findJavaHome(int version) {
        for (JavaHome javaHome : javaHomes) {
            if (javaHome.version() == version) {
                return javaHome;
            }
        }
        return null;
    }

    public static class JavaHome {
        private final String home;
        private final int version;

        public JavaHome(String home, int version) {
            this.home = Objects.requireNonNull(home, "home");
            this.version = version;
        }

        public String home() {
            return home;
        }

        public int version() {
            return version;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            JavaHome javaHome = (JavaHome) o;
            return version == javaHome.version && home.equals(javaHome.home);
        }

        @Override
        public int hashCode() {
            int result = home.hashCode();
            result = 31 * result + version;
            return result;
        }

        @Override
        public String toString() {
            return "JavaHome{" +
                    "home='" + home + '\'' +
                    ", version=" + version +
                    '}';
        }
    }
}
