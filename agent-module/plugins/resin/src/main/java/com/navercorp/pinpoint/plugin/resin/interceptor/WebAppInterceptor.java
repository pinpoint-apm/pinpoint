package com.navercorp.pinpoint.plugin.resin.interceptor;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.StringUtils;

import javax.servlet.ServletContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author huangpengjie@fang.com
 */
public class WebAppInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public WebAppInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        // DO nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        try {
            if (target instanceof ServletContext) {
                final ServletContext servletContext = (ServletContext) target;
                final String contextKey = extractContextKey(servletContext);
                final List<String> loadedJarNames = extractLibJars(servletContext);
                if (isDebug) {
                    logger.debug("{}  jars : {}", contextKey, Arrays.toString(loadedJarNames.toArray()));
                }
                dispatchLibJars(contextKey, loadedJarNames, servletContext);
            } else {
                logger.warn("Webapp loader is not an instance of javax.servlet.ServletContext , target={}", target);
            }
        } catch (Exception e) {
            logger.warn("Failed to dispatch lib jars", e);
        }
    }

    private String extractContextKey(ServletContext webapp) {
        String context = webapp.getContextPath();
        return StringUtils.isEmpty(context) ? "/ROOT" : context;
    }

    private List<String> extractLibJarNamesFromURLs(URL[] urls) {
        if (urls == null) {
            return Collections.emptyList();
        }
        List<String> libJarNames = new ArrayList<>(urls.length);
        for (URL url : urls) {
            try {
                URI uri = url.toURI();
                String libJarName = extractLibJarName(uri);
                if (libJarName.length() > 0) {
                    libJarNames.add(libJarName);
                }
            } catch (URISyntaxException e) {
                // ignore invalid formats
                logger.warn("Invalid library url found : [{}]", url, e);
            } catch (Exception e) {
                logger.warn("Error extracting library name", e);
            }
        }
        return libJarNames;
    }

    private String extractLibJarName(URI uri) {
        String jarName = uri.toString();
        int lastIndexOfSeparator = jarName.lastIndexOf("/");
        if (lastIndexOfSeparator < 0) {
            return jarName;
        } else {
            int lastIndexOfExclamatory = jarName.lastIndexOf("!");
            if (lastIndexOfExclamatory == lastIndexOfSeparator - 1) {
                jarName = jarName.substring(0, lastIndexOfExclamatory);
                return jarName.substring(jarName.lastIndexOf("/") + 1);
            } else if (jarName.startsWith("jar:") || jarName.endsWith(".jar")) {
                return jarName.substring(lastIndexOfSeparator + 1);
            } else {
                return "";
            }
        }
    }

    private void dispatchLibJars(String contextKey, List<String> libJars, ServletContext webapp) {
        ServerMetaDataHolder holder = this.traceContext.getServerMetaDataHolder();
        holder.addServiceInfo(contextKey, libJars);
        holder.setServerName(webapp.getServerInfo());
        holder.notifyListeners();
    }

    private List<String> extractLibJars(ServletContext webapp) {
        ClassLoader classLoader = webapp.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) classLoader).getURLs();
            return extractLibJarNamesFromURLs(urls);
        } else {
            return Collections.emptyList();
        }
    }
}