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

package com.navercorp.pinpoint.bootstrap.module;

import com.navercorp.pinpoint.common.util.Assert;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Woonduk Kang(emeroad)
 */
public class Java9ModuleSupport implements ModuleSupport {

    private final Instrumentation instrumentation;

    public Java9ModuleSupport(Instrumentation instrumentation) {
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation must not be null");
    }

    @Override
    public void setup() {
        // pinpoint module name : unnamed
        Module pinpointModule = getPinpointModule();
        info("pinpoint Module.isNamed:" + pinpointModule.isNamed());
        info("pinpoint Module.name:" + pinpointModule.getName());
        Set<Module> moduleSet = Set.of(pinpointModule);

        Map<String, Set<Module>> exports = new HashMap<>();
//        exports.put("jdk.internal.misc", moduleSet);
        exports.put("jdk.internal.loader", moduleSet);

        final Module baseModule = Object.class.getModule();
        addExports(baseModule, exports);

        forTest();
    }

    private void forTest() {


        // for jdk http connector
        Set<Module> pinpointModule = Set.of(getPinpointModule());
//        Map<String, Set<Module>> exports = new HashMap<>();
//        // test http connector
//        exports.put("sun.net.www.protocol.http", pinpointModule);
//        info("export sun.net.www.protocol.http");
//        addExports(Object.class.getModule(), exports);
//        info("opens sun.net.www.protocol.http");
//        addOpens(Object.class.getModule(), exports);
        addReads(Object.class.getModule(), pinpointModule);
    }

    private Module getPinpointModule() {
        return this.getClass().getModule();
    }


    private void addExports(Module redefineModule, Map<String, Set<Module>> extraExports) {
        instrumentation.redefineModule(redefineModule, Set.of(), extraExports, Map.of(), Set.of(), Map.of());
    }

    private void addOpens(Module redefineModule, Map<String, Set<Module>> extraOpens) {
        instrumentation.redefineModule(redefineModule, Set.of(), Map.of(), extraOpens, Set.of(), Map.of());
    }

    private void addReads(Module redefineModule, Set<Module> extraReads) {
        info("redefineModule:" + redefineModule.getName() +" extraReads:" + extraReads);
        instrumentation.redefineModule(redefineModule, extraReads, Map.of(), Map.of(), Set.of(), Map.of());
    }

    public void info(String string) {
        System.out.println(string);
    }
}
