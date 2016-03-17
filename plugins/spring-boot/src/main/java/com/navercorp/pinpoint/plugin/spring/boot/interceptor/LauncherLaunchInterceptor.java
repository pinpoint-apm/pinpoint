/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.boot.interceptor;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.boot.SpringBootConstants;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author HyunGil Jeong
 */
public class LauncherLaunchInterceptor implements AroundInterceptor {

    private final static String PATH_SEPARATOR = "/";
    private final static String JAR_SEPARATOR = "!";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public LauncherLaunchInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        if (!validate(target, args)) {
            return;
        }
        String serviceName = createServiceName(target);
        URLClassLoader classLoader = (URLClassLoader) args[2];
        URL[] urls = classLoader.getURLs();
        List<String> loadedJarNames = new ArrayList<String>(extractLibJarNamesFromURLs(urls));
        ServerMetaDataHolder holder = this.traceContext.getServerMetaDataHolder();
        holder.addServiceInfo(serviceName, loadedJarNames);
        holder.notifyListeners();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // Do nothing
    }

    private String createServiceName(Object target) {
        StringBuilder sb = new StringBuilder(SpringBootConstants.ROOT_CONTEXT_KEY);
        sb.append(" (").append(target.getClass().getSimpleName()).append(")");
        return sb.toString();
    }

    private boolean validate(Object target, Object[] args) {
        if (target == null || args == null) {
            return false;
        }
        if (args.length < 3) {
            return false;
        }
        if (!(args[2] instanceof URLClassLoader)) {
            return false;
        }
        return true;
    }

    private Set<String> extractLibJarNamesFromURLs(URL[] urls) {
        if (urls == null) {
            return Collections.emptySet();
        }
        Set<String> libJarNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (URL url : urls) {
            try {
                String libJarName = extractLibJarName(url);
                if (libJarName.length() > 0) {
                    libJarNames.add(libJarName);
                }
            } catch (Exception e) {
                // safety net
                logger.warn("Error extracting library name", e);
            }
        }
        return libJarNames;
    }

    private String extractLibJarName(URL url) {
        String protocol = url.getProtocol();
        String path = url.getPath().replaceAll("\\\\", PATH_SEPARATOR);
        if (protocol.equals("file")) {
            return extractNameFromFile(path);
        } else if (protocol.equals("jar")) {
            return extractNameFromJar(path);
        } else {
            return "";
        }
    }

    private String extractNameFromFile(String fileUri) {
        int lastIndexOfSeparator = fileUri.lastIndexOf(PATH_SEPARATOR);
        if (lastIndexOfSeparator < 0) {
            return fileUri;
        } else {
            return fileUri.substring(lastIndexOfSeparator + 1);
        }
    }

    private String extractNameFromJar(String jarUri) {
        String uri = jarUri.substring(0, jarUri.lastIndexOf(JAR_SEPARATOR));
        int rootJarEndIndex = uri.indexOf(JAR_SEPARATOR);
        if (rootJarEndIndex < 0) {
            return extractNameFromFile(uri);
        }
        String rootJarFileUri = extractNameFromFile(uri.substring(0, rootJarEndIndex));
        StringBuilder sb = new StringBuilder(rootJarFileUri);
        sb.append(uri.substring(rootJarEndIndex));
        return sb.toString();
    }

}
