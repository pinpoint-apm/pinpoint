/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.process.interceptor;

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.common.util.OsType;
import com.navercorp.pinpoint.common.util.OsUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PidProviderFactory {

    private static final OsType[] UNIX_Platform = new OsType[] {
            OsType.LINUX, OsType.BSD,
            OsType.SOLARIS, OsType.AIX,
            OsType.MAC,
            OsType.HP_UX};

    public PidProviderFactory() {
    }

    public PidProvider newPidProvider() {
        final JvmVersion version = JvmUtils.getVersion();
        if (version.onOrAfter(JvmVersion.JAVA_9)) {
            return new Java9PidProvider();
        }
        if (isUnixProcess()) {
            return new UNIXProcessPidProvider();
        }
        if (existUnixProcessClass()) {
            return new UNIXProcessPidProvider();
        }
        if (isWindows()) {
            return new ProcessImplProvider();
        }
        return new UnsupportPidProvider();
    }

    private static boolean existUnixProcessClass() {
        try {
            // heuristic
            UNIXProcessPidProvider.getUnixProcess();
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }


    private static boolean isWindows() {
        OsType type = OsUtils.getType();
        return OsType.WINDOW == type;
    }

    private static boolean isUnixProcess() {
//        https://github.com/frohoff/jdk8u-jdk/blob/master/src/solaris/classes/java/lang/UNIXProcess.java
//        LINUX(LaunchMechanism.VFORK, LaunchMechanism.FORK),
//        BSD(LaunchMechanism.POSIX_SPAWN, LaunchMechanism.FORK),
//        SOLARIS(LaunchMechanism.POSIX_SPAWN, LaunchMechanism.FORK),
//        AIX(LaunchMechanism.POSIX_SPAWN, LaunchMechanism.FORK);
//      HP_UX??
        final OsType os = OsUtils.getType();
        for (OsType osType : UNIX_Platform) {
            if (osType == os) {
                return true;
            }
        }
        return false;
    }
}
