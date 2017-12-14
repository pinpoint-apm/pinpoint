/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver;

import com.navercorp.pinpoint.profiler.receiver.service.EchoService;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannelContext;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.dto.command.TCommandEcho;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ProfilerCommandServiceLocatorTest {

    @Test(expected = NullPointerException.class)
    public void throwNullTest1() throws Exception {
        ProfilerCommandService commandService = null;

        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(commandService);
    }

    @Test(expected = NullPointerException.class)
    public void throwNullTest2() throws Exception {
        ProfilerCommandServiceGroup commandServiceGroup = null;

        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(commandServiceGroup);
    }

    @Test(expected = NullPointerException.class)
    public void throwNullTest3() throws Exception {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void throwNullTest4() throws Exception {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(TResult.class, null);
    }

    @Test
    public void returnNullTest() throws Exception {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        ProfilerCommandServiceLocator commandServiceLocator = builder.build();

        Assert.assertNull(commandServiceLocator.getService(null));
        Assert.assertNull(commandServiceLocator.getSimpleService(null));
        Assert.assertNull(commandServiceLocator.getRequestService(null));
        Assert.assertNull(commandServiceLocator.getStreamService(null));
    }

    @Test
    public void basicFunctionTest1() throws Exception {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new EchoService());
        builder.addService(new EchoService());
        ProfilerCommandServiceLocator commandServiceLocator = builder.build();

        TCommandEcho commandEcho = new TCommandEcho();

        Assert.assertEquals(1, commandServiceLocator.getCommandServiceClasses().size());
        Assert.assertEquals(1, commandServiceLocator.getCommandServiceCodes().size());
        Assert.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(TCommandType.getType(commandEcho.getClass()).getCode()));

        Assert.assertNotNull(commandServiceLocator.getService(commandEcho));
        Assert.assertNotNull(commandServiceLocator.getRequestService(commandEcho));

        Assert.assertNull(commandServiceLocator.getSimpleService(commandEcho));
        Assert.assertNull(commandServiceLocator.getStreamService(commandEcho));
    }

    @Test
    public void basicFunctionTest2() throws Exception {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new MockCommandServiceGroup());
        ProfilerCommandServiceLocator commandServiceLocator = builder.build();

        TResult commandResult = new TResult();
        TCommandTransfer commandTransfer = new TCommandTransfer();

        Assert.assertEquals(2, commandServiceLocator.getCommandServiceClasses().size());
        Assert.assertEquals(2, commandServiceLocator.getCommandServiceCodes().size());
        Assert.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(TCommandType.getType(commandResult.getClass()).getCode()));
        Assert.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(TCommandType.getType(commandTransfer.getClass()).getCode()));

        Assert.assertNotNull(commandServiceLocator.getService(commandResult));
        Assert.assertNotNull(commandServiceLocator.getSimpleService(commandResult));
        Assert.assertNull(commandServiceLocator.getRequestService(commandResult));
        Assert.assertNull(commandServiceLocator.getStreamService(commandResult));

        Assert.assertNotNull(commandServiceLocator.getService(commandTransfer));
        Assert.assertNotNull(commandServiceLocator.getStreamService(commandTransfer));
        Assert.assertNull(commandServiceLocator.getSimpleService(commandTransfer));
        Assert.assertNull(commandServiceLocator.getRequestService(commandTransfer));
    }

    private static class MockSimpleCommandService implements ProfilerSimpleCommandService {

        @Override
        public void simpleCommandService(TBase<?, ?> tbase) {

        }

        @Override
        public Class<? extends TBase> getCommandClazz() {
            return TResult.class;
        }

    }

    private static class MockStreamCommandService implements ProfilerStreamCommandService {

        @Override
        public StreamCode streamCommandService(TBase tBase, ServerStreamChannelContext streamChannelContext) {
            return StreamCode.OK;
        }

        @Override
        public Class<? extends TBase> getCommandClazz() {
            return TCommandTransfer.class;
        }

    }

    private static class MockCommandServiceGroup implements ProfilerCommandServiceGroup {

        private static final ProfilerCommandService[] REGISTER_SERVICES = {new MockSimpleCommandService(), new MockStreamCommandService()};

        @Override
        public List<ProfilerCommandService> getCommandServiceList() {
            return Arrays.asList(REGISTER_SERVICES);
        }

    }

}
