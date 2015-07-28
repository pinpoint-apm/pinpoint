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

package com.navercorp.pinpoint.thrift.io;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TBase;

/**
 * @author koo.taejin
 */
public enum TCommandTypeVersion {

    // Match with agent version
    V_1_0_2_SNAPSHOT("1.0.2-SNAPSHOT", TCommandType.RESULT, TCommandType.THREAD_DUMP),
    V_1_0_2("1.0.2", V_1_0_2_SNAPSHOT),
    V_1_0_3_SNAPSHOT("1.0.3-SNAPSHOT", V_1_0_2, TCommandType.ECHO, TCommandType.TRANSFER, TCommandType.THREAD_DUMP_RESPONSE),
    V_1_0_3("1.0.3", V_1_0_3_SNAPSHOT),
    V_1_0_4_SNAPSHOT("1.0.4-SNAPSHOT", V_1_0_3),
    V_1_0_4("1.0.4", V_1_0_4_SNAPSHOT),
    V_1_1_0_SNAPSHOT("1.1.0-SNAPSHOT", V_1_0_4),
    V_1_1_0("1.1.0", V_1_1_0_SNAPSHOT),
    V_1_5_0_SNAPSHOT("1.5.0-SNAPSHOT", V_1_1_0),
    
    
    UNKNOWN("UNKNOWN");

    private final String versionName;
    private final List<TCommandType> supportCommandList = new ArrayList<TCommandType>();

    private TCommandTypeVersion(String versionName, TCommandTypeVersion version, TCommandType... supportCommandArray) {
        this.versionName = versionName;

        for (TCommandType supportCommand : version.getSupportCommandList()) {
            supportCommandList.add(supportCommand);
        }

        for (TCommandType supportCommand : supportCommandArray) {
            getSupportCommandList().add(supportCommand);
        }
    }

    private TCommandTypeVersion(String versionName, TCommandType... supportCommandArray) {
        this.versionName = versionName;

        for (TCommandType supportCommand : supportCommandArray) {
            getSupportCommandList().add(supportCommand);
        }
    }

    public List<TCommandType> getSupportCommandList() {
        return supportCommandList;
    }

    public boolean isSupportCommand(TBase command) {
        if (command == null) {
            return false;
        }

        for (TCommandType eachCommand : supportCommandList) {
            if (eachCommand == null) {
                continue;
            }

            if (eachCommand.getClazz() == command.getClass()) {
                return true;
            }
        }

        return false;
    }

    public String getVersionName() {
        return versionName;
    }

    public static TCommandTypeVersion getVersion(String version) {
        if (version == null) {
            throw new NullPointerException("version may not be null.");
        }

        for (TCommandTypeVersion versionType : TCommandTypeVersion.values()) {
            if (versionType.getVersionName().equals(version)) {
                return versionType;
            }
        }

        return UNKNOWN;
    }

}
