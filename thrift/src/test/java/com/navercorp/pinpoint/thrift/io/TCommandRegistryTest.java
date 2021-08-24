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

package com.navercorp.pinpoint.thrift.io;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.util.TypeLocator;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandThreadDump;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author koo.taejin
 */
public class TCommandRegistryTest {

    @Test
    public void registryTest1() {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.UNKNOWN);

        Assert.assertFalse(registry.isSupport(TCommandType.RESULT.getCode()));
        Assert.assertFalse(registry.isSupport(TCommandType.THREAD_DUMP.getCode()));

        Assert.assertFalse(registry.isSupport(TResult.class));
        Assert.assertFalse(registry.isSupport(TCommandThreadDump.class));
    }

    public void registryTest2() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.UNKNOWN);

        Assert.assertNull(registry.headerLookup(new TResult()));
    }

    public void registryTest3() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.UNKNOWN);

        Assert.assertNull(registry.bodyLookup(TCommandType.RESULT.getCode()));
    }

    @Test
    public void registryTest4() {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

        Assert.assertTrue(registry.isSupport(TCommandType.RESULT.getCode()));
        Assert.assertTrue(registry.isSupport(TCommandType.THREAD_DUMP.getCode()));

        Assert.assertTrue(registry.isSupport(TResult.class));
        Assert.assertTrue(registry.isSupport(TCommandThreadDump.class));
    }

    @Test
    public void registryTest5() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

        Header header = registry.headerLookup(new TResult());
        Assert.assertNotNull(header);
    }

    @Test
    public void registryTest6() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

        TBase<?, ?> tBase = registry.bodyLookup(TCommandType.RESULT.getCode());
        Assert.assertEquals(tBase.getClass(), TResult.class);

        tBase = registry.bodyLookup(TCommandType.THREAD_DUMP.getCode());
        Assert.assertEquals(tBase.getClass(), TCommandThreadDump.class);
    }

    @Test
    public void isSupportTest() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

        boolean isSupport = registry.isSupport(TResult.class);
        Assert.assertTrue(isSupport);

        isSupport = registry.isSupport(TCommandTransferResponse.class);
        Assert.assertFalse(isSupport);
    }

//    @Test
    public void isSupportTest_Inheritance() throws TException {
        TypeLocator<TBase<?, ?>> registry = TCommandRegistry.build(TCommandTypeVersion.V_1_0_2_SNAPSHOT);

        boolean isSupport = registry.isSupport(TResultEx.class);
        Assert.assertTrue(isSupport);

        isSupport = registry.isSupport(TCommandTransferResponse.class);
        Assert.assertFalse(isSupport);
    }

    class TResultEx extends TResult {
    }

}
