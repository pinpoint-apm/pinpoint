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

package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.loader.WebappLoader;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author hyungil.jeong
 */
public class WebappLoaderStartInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    
    private final TraceContext traceContext;

    public WebappLoaderStartInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target, Object[] args) {
        // Do Nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // target should be an instance of WebappLoader.
        if (target instanceof WebappLoader) {
            WebappLoader webappLoader = (WebappLoader)target;
            try {
                String contextKey = extractContextKey(webappLoader);
                List<String> loadedJarNames = extractLibJars(webappLoader);
                dispatchLibJars(contextKey, loadedJarNames);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(e.getMessage(), e);
                }
            }
        } else {
            logger.warn("Webapp loader is not an instance of org.apache.catalina.loader.WebappLoader. Found [{}]", target.getClass().toString());
        }
    }
    
    private String extractContextKey(WebappLoader webappLoader) {
        final String defaultContextName = "";
        try {
            Container container = extractContext(webappLoader);
            // WebappLoader's associated Container should be a Context.
            if (container instanceof Context) {
                Context context = (Context)container;
                String contextName = context.getName();
                Host host = (Host)container.getParent();
                Engine engine = (Engine)host.getParent();
                StringBuilder sb = new StringBuilder();
                sb.append(engine.getName()).append("/").append(host.getName());
                if (!contextName.startsWith("/")) {
                    sb.append('/');
                }
                sb.append(contextName);
                return sb.toString();
            }
        } catch (Exception e) {
            // Same action for any and all exceptions.
            logger.warn("Error extracting context name.", e);
        }
        return defaultContextName;
    }

    // FIXME Use reflection until we provide separate packages for instrumented libraries.
    // Tomcat 8's WebappLoader does not have getContainer() method.
    // Providing an optional package that calls WebappLoader.getContext() method could be an option.
    private Container extractContext(WebappLoader webappLoader) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method m;
        try {
            // Tomcat 6, 7 - org.apache.catalina.loader.getContainer() 
            m = webappLoader.getClass().getDeclaredMethod("getContainer");
        } catch (NoSuchMethodException e1) {
            try {
                // Tomcat 8 - org.apache.catalina.loader.getContainer()
                m = webappLoader.getClass().getDeclaredMethod("getContext");
            } catch (NoSuchMethodException e2) {
                logger.warn("Webapp loader does not have access to its container.");
                return null;
            }
        }
        Object container = m.invoke(webappLoader);
        if (container instanceof Container) {
            return (Container)container;
        }
        return null;
    }
    
    private List<String> extractLibJars(WebappLoader webappLoader) {
        ClassLoader classLoader = webappLoader.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader webappClassLoader = (URLClassLoader)classLoader;
            URL[] urls = webappClassLoader.getURLs();
            return extractLibJarNamesFromURLs(urls);
        } else {
            logger.warn("Webapp class loader is not an instance of URLClassLoader. Found [{}]", classLoader.getClass().toString());
            return Collections.emptyList();
        } 
    }
    
    private List<String> extractLibJarNamesFromURLs(URL[] urls) {
        if (urls == null) {
            return Collections.emptyList();
        }
        List<String> libJarNames = new ArrayList<String>(urls.length);
        for (URL url : urls) {
            try {
                URI uri =  url.toURI();
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
        if (jarName == null) {
            return "";
        }
        int lastIndexOfSeparator = jarName.lastIndexOf("/");
        if (lastIndexOfSeparator < 0) {
            return jarName;
        } else {
            return jarName.substring(lastIndexOfSeparator + 1);
        }
    }
    
    private void dispatchLibJars(String contextKey, List<String> libJars) {
        ServerMetaDataHolder holder = this.traceContext.getServerMetaDataHolder();
        holder.addServiceInfo(contextKey, libJars);
        holder.notifyListeners();
    }

}
