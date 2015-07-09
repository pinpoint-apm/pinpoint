/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.test.plugin;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.runners.model.InitializationError;

/**
 * @author Jongho Moon
 *
 */
public class PinpointPluginTestSuite extends AbstractPinpointPluginTestSuite implements PinpointPluginTestConstants {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private final boolean testOnSystemClassLoader;
    private final boolean testOnChildClassLoader;
    private final String[] repositories;
    private final String[] dependencies;
    private final String libraryPath;
    private final String[] librarySubDirs;
    
    public PinpointPluginTestSuite(Class<?> testClass) throws InitializationError, ArtifactResolutionException, DependencyResolutionException {
        super(testClass);

        OnClassLoader onClassLoader = testClass.getAnnotation(OnClassLoader.class);
        this.testOnChildClassLoader = onClassLoader == null ? true : onClassLoader.child();
        this.testOnSystemClassLoader = onClassLoader == null ? false : onClassLoader.system();

        Dependency deps = testClass.getAnnotation(Dependency.class);
        this.dependencies = deps == null ? null : deps.value();
        
        TestRoot lib = testClass.getAnnotation(TestRoot.class);
        
        if (lib == null) {
            this.libraryPath = null;
            this.librarySubDirs = null;
        } else {
            String path = lib.value();
            
            if (path.isEmpty()) {
                path = lib.path();
            }
            
            this.libraryPath = path;
            this.librarySubDirs = lib.libraryDir();
        }
        
        if (deps != null && lib != null) {
            throw new IllegalArgumentException("@Dependency and @TestRoot can not annotate a class at the same time");
        }

        Repository repos = testClass.getAnnotation(Repository.class);
        this.repositories = repos == null ? new String[0] : repos.value();
    }

    @Override
    protected List<PinpointPluginTestInstance> createTestCases(PinpointPluginTestContext context) throws Exception {
        if (dependencies != null) {
            return createCasesWithDependencies(context);
        } else if (libraryPath != null) {
            return createCasesWithLibraryPath(context);
        }
        
        return createCasesWithJdkOnly(context);
    }

    private List<PinpointPluginTestInstance> createCasesWithJdkOnly(PinpointPluginTestContext context) {
        List<PinpointPluginTestInstance> cases = new ArrayList<PinpointPluginTestInstance>();
        
        if (testOnSystemClassLoader) {
            cases.add(new NormalPluginTestCase(context, "", Collections.<String>emptyList(), true));
        }
        
        if (testOnChildClassLoader) {
            cases.add(new NormalPluginTestCase(context, "", Collections.<String>emptyList(), false));
        }
        
        return cases;
    }


    private List<PinpointPluginTestInstance> createCasesWithLibraryPath(PinpointPluginTestContext context) {
        File file = new File(libraryPath);
        
        if (!file.isDirectory()) {
            throw new RuntimeException("value of @TestRoot is not a directory: " + libraryPath);
        }
        File[] children = file.listFiles();
        if (children == null) {
            return Collections.emptyList();
        }
        
        List<PinpointPluginTestInstance> cases = new ArrayList<PinpointPluginTestInstance>();
        for (File child : children) {
            if (!child.isDirectory()) {
                continue;
            }
            
            List<String> libraries = new ArrayList<String>();
            
            if (librarySubDirs.length == 0) {
                addJars(child, libraries);
                libraries.add(child.getAbsolutePath());
            } else {
                for (String subDir : librarySubDirs) {
                    File libDir = new File(child, subDir);
                    addJars(libDir, libraries);
                    libraries.add(libDir.getAbsolutePath());
                }
            }
            
            if (testOnSystemClassLoader) {
                cases.add(new NormalPluginTestCase(context, child.getName(), libraries, true));
            }
            
            if (testOnChildClassLoader) {
                cases.add(new NormalPluginTestCase(context, child.getName(), libraries, false));
            }
        }
        
        return cases;
    }

    private void addJars(File libDir, List<String> libraries) {
        if (!libDir.isDirectory()) {
            return;
        }
        File[] children = libDir.listFiles();
        if (children == null) {
            return;
        }
        for (File f : children) {
            if (f.getName().endsWith(".jar")) {
                libraries.add(f.getAbsolutePath());
            }
        }
    }
    
    private List<PinpointPluginTestInstance> createCasesWithDependencies(PinpointPluginTestContext context) throws ArtifactResolutionException, DependencyResolutionException {
        List<PinpointPluginTestInstance> cases = new ArrayList<PinpointPluginTestInstance>();
        
        DependencyResolver resolver = DependencyResolver.get(repositories);
        Map<String, List<Artifact>> dependencyCases = resolver.resolveDependencySets(dependencies);
        
        for (Map.Entry<String, List<Artifact>> dependencyCase : dependencyCases.entrySet()) {
            List<String> libs = new ArrayList<String>();

            for (File lib : resolver.resolveArtifactsAndDependencies(dependencyCase.getValue())) {
                libs.add(lib.getAbsolutePath());
            }

            if (testOnSystemClassLoader) {
                cases.add(new NormalPluginTestCase(context, dependencyCase.getKey(), libs, true));
            }
            
            if (testOnChildClassLoader) {
                cases.add(new NormalPluginTestCase(context, dependencyCase.getKey(), libs, false));
            }
        }
        
        return cases;
    }
    
    
    
    private class NormalPluginTestCase implements PinpointPluginTestInstance {
        private final PinpointPluginTestContext context;
        private final String testId;
        private final List<String> libs;
        private final boolean onSystemClassLoader;
        
        public NormalPluginTestCase(PinpointPluginTestContext context, String testId, List<String> libs, boolean onSystemClassLoader) {
            this.context = context;
            this.testId = testId + ":" + (onSystemClassLoader ? "system" : "child") + ":" + context.getJvmVersion();
            this.libs = libs;
            this.onSystemClassLoader = onSystemClassLoader;
        }

        @Override
        public String getTestId() {
            return testId;
        }

        @Override
        public List<String> getClassPath() {
            if (onSystemClassLoader) {
                List<String> libs = new ArrayList<String>(context.getRequiredLibraries());
                libs.addAll(this.libs);
                libs.add(context.getTestClassLocation());
                
                return libs;
            } else {
                return context.getRequiredLibraries();
            }
        }

        @Override
        public List<String> getVmArgs() {
            return Arrays.asList("-Dfile.encoding=" + DEFAULT_ENCODING);
        }

        @Override
        public String getMainClass() {
            return ForkedPinpointPluginTest.class.getName();
        }

        @Override
        public List<String> getAppArgs() {
            List<String> args = new ArrayList<String>();
            
            args.add(context.getTestClass().getName());
            
            if (!onSystemClassLoader) {
                StringBuilder classPath = new StringBuilder();
                classPath.append(CHILD_CLASS_PATH_PREFIX);
                
                for (String lib : libs) {
                    classPath.append(lib);
                    classPath.append(File.pathSeparatorChar);
                }
                
                classPath.append(context.getTestClassLocation());
                args.add(classPath.toString());
            }
            
            return args;
        }

        @Override
        public Scanner startTest(Process process) throws Exception {
            InputStream inputStream = process.getInputStream();
            return new Scanner(inputStream, DEFAULT_ENCODING);
        }

        @Override
        public void endTest(Process process) throws Exception {
            // do nothing
        }

        @Override
        public File getWorkingDirectory() {
            return new File(".");
        }
    }
}
