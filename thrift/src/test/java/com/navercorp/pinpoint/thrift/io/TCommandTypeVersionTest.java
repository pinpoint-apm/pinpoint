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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author koo.taejin
 */
public class TCommandTypeVersionTest {

    @Test
    public void versionTest1() {
        TCommandTypeVersion version = TCommandTypeVersion.V_1_0_2_SNAPSHOT;

        List<TCommandType> supportTypeList = version.getSupportCommandList();

        Assertions.assertEquals(2, supportTypeList.size());
        Assertions.assertTrue(supportTypeList.contains(TCommandType.THREAD_DUMP));
        Assertions.assertTrue(supportTypeList.contains(TCommandType.RESULT));
    }

    @Test
    public void versionTest2() {
        TCommandTypeVersion version = TCommandTypeVersion.UNKNOWN;

        List<TCommandType> supportTypeList = version.getSupportCommandList();

        Assertions.assertEquals(0, supportTypeList.size());
    }

    @Test
    public void versionTest3() {
        TCommandTypeVersion version = TCommandTypeVersion.getVersion("1.0.0");
        Assertions.assertEquals(TCommandTypeVersion.UNKNOWN, version);

        version = TCommandTypeVersion.getVersion(TCommandTypeVersion.V_1_0_2_SNAPSHOT.getVersionName());
        Assertions.assertEquals(TCommandTypeVersion.V_1_0_2_SNAPSHOT, version);
    }


}
