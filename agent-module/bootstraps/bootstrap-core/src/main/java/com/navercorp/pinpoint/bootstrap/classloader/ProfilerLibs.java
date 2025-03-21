/*
 * Copyright 2019 NAVER Corp.
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
        String[] lib = new String[]{
                "com.navercorp.pinpoint.common.profiler",
                "com.navercorp.pinpoint.profiler",
                "com.navercorp.pinpoint.thrift",
                "com.navercorp.pinpoint.io",
                "com.navercorp.pinpoint.rpc",
                "com.navercorp.pinpoint.loader.plugins",

                // grpc
                "com.navercorp.pinpoint.grpc",
                "com.navercorp.pinpoint.io",
                "io.grpc",
                "io.netty",
                "com.google.protobuf",
                "com.google.gson",
                "google.protobuf",
                "com.google.grpc",
                // proto-google-common-protos-x.x.x.jar
                "com.google.api",
                "com.google.cloud",
                "com.google.geo",
                "com.google.logging",
                "com.google.longrunning",
                "com.google.rpc",
                "com.google.type",
                "com.google.errorprone.annotations",

                "com.github.benmanes.caffeine",
                "org.objectweb.asm",
                "org.slf4j",
                "org.apache.thrift",
                "org.jboss.netty",
                "com.google.common",
                "com.google.j2objc.annotations",
                // google guice
                "com.google.inject",
                "org.aopalliance",

                // jackson
                "com.fasterxml.jackson",

                "org.apache.commons.lang",
                "org.apache.commons.logging",
                "org.apache.log4j",
                "org.apache.logging.log4j",
                "com.nhncorp.nelo2",

                "io.perfmark",
                "javax.annotation",
                "jakarta.annotation",
                "jakarta.inject",
                "org.codehaus.mojo.animal_sniffer",
                "org.checkerframework",

                "io.github.resilience4j",
                "io.vavr"
        };

        PINPOINT_PROFILER_CLASS = Collections.unmodifiableList(Arrays.asList(lib));
    }

}
