/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.plugin.classloader;

import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.test.plugin.TranslatorAdaptor;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointBootstrapPluginTestPackage;
import com.navercorp.pinpoint.test.plugin.classloader.predicates.IsPinpointPackage;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// parent: "com.navercorp.pinpoint.bootstrap.plugin.test."
public class PluginTestJunitTestClassLoader extends PluginTestClassLoader {
    public static final IsPinpointPackage isPinpointPackage = new IsPinpointPackage();
    public static final IsPinpointBootstrapPluginTestPackage isPinpointBootstrapPluginTestPackage = new IsPinpointBootstrapPluginTestPackage();

    private PluginAgentTestClassLoader agentClassLoader;
    private TranslatorAdaptor translator;

    private List<String> transformIncludeList;

    public PluginTestJunitTestClassLoader(URL[] urls, ClassLoader parent, TranslatorAdaptor translator) {
        super(urls, parent);
        this.translator = translator;
        setClassLoaderName(getClass().getSimpleName());
    }

    public void setAgentClassLoader(PluginAgentTestClassLoader agentClassLoader) {
        this.agentClassLoader = agentClassLoader;
    }

    public void setTransformIncludeList(List<String> transformIncludeList) {
        this.transformIncludeList = transformIncludeList;
    }

    @Override
    protected boolean isDelegated(String name) {
        if (isTransformInclude(name)) {
            return false;
        }

        return super.isDelegated(name) || isPinpointBootstrapPluginTestPackage.test(name);
    }

    @Override
    public Class<?> loadClassChildFirst(String name) throws ClassNotFoundException {
        if (isPinpointPackage.test(name)) {
            if (agentClassLoader != null) {
                return agentClassLoader.loadClass(name, false);
            }
        }

        final String classInternalName = JavaAssistUtils.javaClassNameToJvmResourceName(name);
        final URL url = getResource(classInternalName);
        if (url != null) {
            try {
                byte[] classfile = translator.transform(this, name, IOUtils.toByteArray(url.openStream()));
                if (classfile != null) {
                    CodeSigner[] signers = null;
                    URLConnection urlConnection = url.openConnection();
                    if (urlConnection instanceof JarURLConnection) {
                        JarFile jarFile = ((JarURLConnection) urlConnection).getJarFile();
                        JarEntry entry = jarFile.getJarEntry(classInternalName);
                        signers = entry.getCodeSigners();
                    }
                    CodeSource cs = new CodeSource(url, signers);
                    return defineClass(name, classfile, 0, classfile.length, cs);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return findClass(name);
    }


    @Override
    protected boolean isChild(String name) {
        return false;
    }

    public boolean isLoadedClass(String name) {
        return findLoadedClass(name) != null;
    }

    boolean isTransformInclude(String name) {
        if (transformIncludeList != null) {
            for (String transformInclude : transformIncludeList) {
                if (transformInclude.endsWith(".")) {
                    if (name.startsWith(transformInclude)) {
                        return true;
                    }
                } else {
                    if (name.equals(transformInclude)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public URL getResource(String name) {
        final String className = JavaAssistUtils.jvmNameToJavaName(name);
        if (isDelegated(className)) {
            return super.getResource(name);
        }

        if (isPinpointPackage.test(className)) {
            if (agentClassLoader != null) {
                return agentClassLoader.getResource(name);
            }
        }

        URL url = findResource(name);
        if (url != null) {
            return url;
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        final String className = JavaAssistUtils.jvmNameToJavaName(name);
        if (isDelegated(className)) {
            return super.getResources(name);
        }

        if (isPinpointPackage.test(className)) {
            if (agentClassLoader != null) {
                return agentClassLoader.getResources(name);
            }
        }

        return findResources(name);
    }

    @Override
    public void clear() {
        super.clear();
        if (this.agentClassLoader != null) {
            this.agentClassLoader.clear();
        }
    }
}
