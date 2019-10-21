/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.java9.module;

import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.module.Providers;

import java.lang.instrument.Instrumentation;
import java.lang.module.ModuleDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim - Add addProvides()
 */
public class Java9Module implements JavaModule {

//    private final ModuleLogger logger = ModuleLogger.getLogger(Java9Module.class.getName());
    private final Instrumentation instrumentation;
    private final Module module;


    Java9Module(Instrumentation instrumentation, Module module) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation");
        }
        this.instrumentation = instrumentation;
        this.module = module;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isNamed() {
        return this.module.isNamed();
    }

    @Override
    public String getName() {
        return this.module.getName();

    }

    @Override
    public List<Providers> getProviders() {
        List<Providers> result = new ArrayList<>();
        Set<ModuleDescriptor.Provides> providesSet = this.module.getDescriptor().provides();
        for (ModuleDescriptor.Provides provides : providesSet) {
            String service = provides.service();
            List<String> providers = provides.providers();
            Providers newProviders = new Providers(service, providers);
            result.add(newProviders);
        }
        return result;
    }

    @Override
    public void addReads(JavaModule targetJavaModule) {
        final Java9Module target = checkJavaModule(targetJavaModule);

//        logger.info("addReads module:" + module.getName() +" target:" + target);
        // for debug
        final Set<Module> readModules = Set.of(target.module);
        RedefineModuleUtils.addReads(instrumentation, module, readModules);
    }

    @Override
    public void addExports(String packageName, JavaModule targetJavaModule) {
        if (packageName == null) {
            throw new NullPointerException("packageName");
        }
         final Java9Module target = checkJavaModule(targetJavaModule);

//        logger.info("addExports module:" + module.getName() + " pkg:" + packageName + " target:" + target);
        final Map<String, Set<Module>> extraModules = Map.of(packageName, Set.of(target.module));
        RedefineModuleUtils.addExports(instrumentation, module, extraModules);
    }

    private Java9Module checkJavaModule(JavaModule targetJavaModule) {
        if (targetJavaModule == null) {
            throw new NullPointerException("targetJavaModule");
        }
        if (targetJavaModule instanceof Java9Module) {
            return (Java9Module) targetJavaModule;
        }
        throw new ModuleException("invalid JavaModule: " + targetJavaModule.getClass());
    }

    @Override
    public void addOpens(String packageName, JavaModule javaModule) {
        if (packageName == null) {
            throw new NullPointerException("packageName");
        }
        final Java9Module target = checkJavaModule(javaModule);

//        logger.info("addExports module:" + module.getName() + " pkg:" + packageName + " target:" + target);

        final Map<String, Set<Module>> extraOpens = Map.of(packageName, Set.of(target.module));
        RedefineModuleUtils.addOpens(instrumentation, module, extraOpens);
    }


    @Override
    public void addUses(Class<?> target) {
        if (target == null) {
            throw new NullPointerException("target");
        }
//        logger.info("addUses module:" + module.getName() +" target:" + target);
        // for debug
        final Set<Class<?>> extraUses = Set.of(target);
        RedefineModuleUtils.addUses(instrumentation, module, extraUses);
    }

    @Override
    public void addProvides(Class<?> service, List<Class<?>> providerList) {
        if (service == null) {
            throw new NullPointerException("target");
        }

        if (providerList == null) {
            throw new NullPointerException("list");
        }

//        logger.info("addProvides module:" + module.getName() +" service:" + service + " providerList:" + providerList);
        // for debug
        final Map<Class<?>, List<Class<?>>> extraProvides = Map.of(service, providerList);
        RedefineModuleUtils.addProvides(instrumentation, module, extraProvides);
    }

    @Override
    public boolean isExported(String packageName, JavaModule targetJavaModule) {
        if (packageName == null) {
            throw new NullPointerException("packageName");
        }
        final Java9Module target = checkJavaModule(targetJavaModule);
        return module.isExported(packageName, target.module);
    }

    @Override
    public boolean isOpen(String packageName, JavaModule targetJavaModule) {
        if (packageName == null) {
            throw new NullPointerException("packageName");
        }
        final Java9Module target = checkJavaModule(targetJavaModule);
        return module.isOpen(packageName, target.module);
    }

    @Override
    public boolean canRead(JavaModule targetJavaModule) {
        final Java9Module target = checkJavaModule(targetJavaModule);
        return this.module.canRead(target.module);
    }

    @Override
    public boolean canRead(Class<?> targetClazz) {
        return this.module.canUse(targetClazz);
    }


    @Override
    public ClassLoader getClassLoader() {
        return module.getClassLoader();
    }

    @Override
    public String toString() {
        return module.toString();
    }
}
