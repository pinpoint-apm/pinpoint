/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.common.util.ByteSizeUnit;
import com.navercorp.pinpoint.profiler.context.provider.thrift.AbstractClientFactoryProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class ClientFactoryProviderTest {

    @Test
    public void getByteSizeTest() {
        final int defaultSize = Integer.MAX_VALUE / 2;

        TestClientFactoryproviderTest testClientFactoryproviderTest = new TestClientFactoryproviderTest();
        int byteSize = testClientFactoryproviderTest.getByteSize("2g", defaultSize);
        Assert.assertEquals(Integer.MAX_VALUE, byteSize);

        byteSize = testClientFactoryproviderTest.getByteSize("100m", defaultSize);
        Assert.assertEquals(ByteSizeUnit.MEGA_BYTES.toBytesSizeAsInt(100), byteSize);

        byteSize = testClientFactoryproviderTest.getByteSize("-100", defaultSize);
        Assert.assertEquals(defaultSize, byteSize);

        byteSize = testClientFactoryproviderTest.getByteSize("-100m", defaultSize);
        Assert.assertEquals(defaultSize, byteSize);
    }

    private static class TestClientFactoryproviderTest extends AbstractClientFactoryProvider {

        @Override
        protected int getByteSize(String value, int defaultSize) {
            return super.getByteSize(value, defaultSize);
        }
    }

}
