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

import com.navercorp.pinpoint.common.util.OsType;
import com.navercorp.pinpoint.common.util.OsUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.mock;


/**
 * @author Woonduk Kang(emeroad)
 */
public class PidProviderFactoryTest {
    @Test
    public void newProvider() throws IOException {
        PidProviderFactory factory = new PidProviderFactory();
        PidProvider pidProvider = factory.newPidProvider();

        if (OsUtils.getType() == OsType.WINDOW) {
            // unsupported
            return;
        }

        ProcessBuilder pb = new ProcessBuilder("echo");
        Process start = pb.start();
        Assert.assertNotEquals(pidProvider.getPid(start), null);

    }

}