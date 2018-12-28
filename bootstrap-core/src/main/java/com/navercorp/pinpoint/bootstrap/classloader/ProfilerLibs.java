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

package com.navercorp.pinpoint.bootstrap.classloader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ProfilerLibs {

    public static final List<String> PINPOINT_PROFILER_CLASS;

    static {
        String[] lib = new String[] {
                "com.navercorp.pinpoint.profiler",
                "com.navercorp.pinpoint.thrift",
                "com.navercorp.pinpoint.io",
                "com.navercorp.pinpoint.rpc",
                "com.navercorp.pinpoint.plugins.loader",
                /*
                 * @deprecated javassist
                 */
                "javassist",
                "org.objectweb.asm",
                "org.slf4j",
                "org.apache.thrift",
                "org.jboss.netty",
                "com.google.common",
                // google guice
                "com.google.inject",
                "org.aopalliance",
                // snakeyaml
                "org.yaml.snakeyaml",

                "org.apache.commons.lang",
                "org.apache.log4j",
                "com.nhncorp.nelo2"
        };

        PINPOINT_PROFILER_CLASS = Collections.unmodifiableList(Arrays.asList(lib));
    }

}
