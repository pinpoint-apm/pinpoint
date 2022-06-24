/*
 * Copyright 2021 NAVER Corp.
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

import jdk.internal.loader.BootLoader;
import jdk.internal.module.Modules;

import java.lang.module.ModuleDescriptor;
import java.net.URI;

final class InternalModules {

    private InternalModules() {
    }

    /**
     * Returns the unnamed module for the bootloader.
     */
    static Module getUnnamedModule() {
        return BootLoader.getUnnamedModule();
    }

    /**
     * Creates a new Module. The module has the given ModuleDescriptor and
     * is defined to the given class loader.
     *
     * The resulting Module is in a larval state in that it does not read
     * any other module and does not have any exports.
     *
     * The URI is for information purposes only.
     */
    static Module defineModule(ClassLoader loader,
                               ModuleDescriptor descriptor,
                               URI uri)
    {
        return Modules.defineModule(loader, descriptor, uri);
    }


    /**
     * Called by the VM to load a system module, typically "java.instrument" or
     * "jdk.management.agent". If the module is not loaded then it is resolved
     * and loaded (along with any dependencies that weren't previously loaded)
     * into a child layer.
     */
    static Module loadModule(String name) {
        return Modules.loadModule(name);
    }
}
