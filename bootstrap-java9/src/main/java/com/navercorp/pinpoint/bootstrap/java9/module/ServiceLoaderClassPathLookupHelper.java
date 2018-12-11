/*
 * Copyright (c) 2005, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import jdk.internal.loader.BootLoader;
import jdk.internal.loader.ClassLoaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.ServiceConfigurationError;
import java.util.Set;

/**
 * Copy from java.util.ServiceLoader
 *
 * @author Mark Reinhold
 */
public class ServiceLoaderClassPathLookupHelper {
    private static final String PREFIX = "META-INF/services/";

    private final ModuleLogger logger = ModuleLogger.getLogger(this.getClass().getName());

    private final ClassLoader classLoader;

    public ServiceLoaderClassPathLookupHelper(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Set<String> lookup(final String serviceClassName) {
        try {
            // Lookup service provider file
            final Set<String> providerNameSet = lookupProviderNameSet(serviceClassName);
            if (CollectionUtils.hasLength(providerNameSet)) {
                return providerNameSet;
            }
        } catch (Throwable t) {
            logger.info("Failed to lookup service providers " + t.getMessage());
        }

        return Collections.emptySet();
    }

    Set<String> lookupProviderNameSet(final String configFileName) {
        final Set<String> providerNames = new LinkedHashSet<>();  // to avoid duplicates
        final String fullName = PREFIX + configFileName;
        Enumeration<URL> configs = null;
        try {
            if (classLoader == null) {
                configs = ClassLoader.getSystemResources(fullName);
            } else if (classLoader == ClassLoaders.platformClassLoader()) {
                // The platform classloader doesn't have a class path,
                // but the boot loader might.
                if (BootLoader.hasClassPath()) {
                    configs = BootLoader.findResources(fullName);
                } else {
                    configs = Collections.emptyEnumeration();
                }
            } else {
                configs = classLoader.getResources(fullName);
            }
        } catch (IOException x) {
            fail(configFileName, "Error locating configuration files ", x);
        }

        if (configs == null) {
            // Defensive code
            return providerNames;
        }

        while (configs.hasMoreElements()) {
            final URL url = configs.nextElement();
            parse(configFileName, url, providerNames);
        }

        return providerNames;
    }

    /**
     * Parse the content of the given URL as a provider-configuration file.
     */
    private void parse(final String serviceName, final URL u, final Set<String> providerNames) throws ServiceConfigurationError {
        try {
            final URLConnection uc = u.openConnection();
            uc.setUseCaches(false);
            try (InputStream in = uc.getInputStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"))) {
                int lc = 1;
                while ((lc = parseLine(serviceName, u, r, lc, providerNames)) >= 0) ;
            }
        } catch (IOException x) {
            fail(serviceName, "Error accessing configuration file", x);
        }
    }


    // Parse a single line from the given configuration file, adding the name
    // on the line to the names list.
    private int parseLine(final String serviceName, URL u, BufferedReader r, int lc, Set<String> names) throws IOException {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(serviceName, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(serviceName, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(serviceName, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!names.contains(ln))
                names.add(ln);
        }
        return lc + 1;
    }

    private void fail(final String serviceName, String msg, Throwable cause) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(serviceName + ": " + msg, cause);
    }

    private void fail(final String serviceName, String msg) throws ServiceConfigurationError {
        throw new ServiceConfigurationError(serviceName + ": " + msg);
    }

    private void fail(final String serviceName, URL u, int line, String msg) throws ServiceConfigurationError {
        fail(serviceName, u + ":" + line + ": " + msg);
    }
}