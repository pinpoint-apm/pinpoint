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

package com.navercorp.pinpoint.profiler.receiver;

import com.navercorp.pinpoint.profiler.receiver.service.EchoService;
import com.navercorp.pinpoint.rpc.packet.stream.StreamCode;
import com.navercorp.pinpoint.rpc.stream.ServerStreamChannel;
import com.navercorp.pinpoint.thrift.io.TCommandType;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class ProfilerCommandServiceLocatorTest {

    @Test
    public void throwNullTest1() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ProfilerCommandService commandService = null;

            ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
            builder.addService(commandService);
        });
    }

    @Test
    public void throwNullTest2() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ProfilerCommandServiceGroup commandServiceGroup = null;

            ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
            builder.addService(commandServiceGroup);
        });
    }

    @Test
    public void throwNullTest3() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
            builder.addService((short) -1, null);
        });
    }

    @Test
    public void throwNullTest4() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
            builder.addService(TCommandType.RESULT.getCode(), null);
        });
    }

    @Test
    public void returnNullTest() {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        ProfilerCommandServiceLocator commandServiceLocator = builder.build();

        Assertions.assertNull(commandServiceLocator.getService((short) -1));
        Assertions.assertNull(commandServiceLocator.getSimpleService((short) -1));
        Assertions.assertNull(commandServiceLocator.getRequestService((short) -1));
        Assertions.assertNull(commandServiceLocator.getStreamService((short) -1));
    }

    @Test
    public void basicFunctionTest1() {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new EchoService());
        builder.addService(new EchoService());
        DefaultProfilerCommandServiceLocator commandServiceLocator = (DefaultProfilerCommandServiceLocator) builder.build();

        short commandEcho = TCommandType.ECHO.getCode();

        Assertions.assertEquals(1, commandServiceLocator.getCommandServiceSize());
        Assertions.assertEquals(1, commandServiceLocator.getCommandServiceCodes().size());
        Assertions.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(commandEcho));

        Assertions.assertNotNull(commandServiceLocator.getService(commandEcho));
        Assertions.assertNotNull(commandServiceLocator.getRequestService(commandEcho));

        Assertions.assertNull(commandServiceLocator.getSimpleService(commandEcho));
        Assertions.assertNull(commandServiceLocator.getStreamService(commandEcho));
    }

    @Test
    public void basicFunctionTest2() {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new MockCommandServiceGroup());
        DefaultProfilerCommandServiceLocator commandServiceLocator = (DefaultProfilerCommandServiceLocator) builder.build();

        short commandResult = TCommandType.RESULT.getCode();

        short commandTransfer = TCommandType.TRANSFER.getCode();

        Assertions.assertEquals(2, commandServiceLocator.getCommandServiceSize());
        Assertions.assertEquals(2, commandServiceLocator.getCommandServiceCodes().size());
        Assertions.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(commandResult));
        Assertions.assertTrue(commandServiceLocator.getCommandServiceCodes().contains(commandTransfer));

        Assertions.assertNotNull(commandServiceLocator.getService(commandResult));
        Assertions.assertNotNull(commandServiceLocator.getSimpleService(commandResult));
        Assertions.assertNull(commandServiceLocator.getRequestService(commandResult));
        Assertions.assertNull(commandServiceLocator.getStreamService(commandResult));

        Assertions.assertNotNull(commandServiceLocator.getService(commandTransfer));
        Assertions.assertNotNull(commandServiceLocator.getStreamService(commandTransfer));
        Assertions.assertNull(commandServiceLocator.getSimpleService(commandTransfer));
        Assertions.assertNull(commandServiceLocator.getRequestService(commandTransfer));
    }

    private static class MockSimpleCommandService implements ProfilerSimpleCommandService<TBase<?, ?>> {

        @Override
        public void simpleCommandService(TBase<?, ?> tbase) {

        }

        @Override
        public short getCommandServiceCode() {
            return TCommandType.RESULT.getCode();
        }

    }

    private static class MockStreamCommandService implements ProfilerStreamCommandService<TBase<?, ?>> {

        @Override
        public StreamCode streamCommandService(TBase<?, ?> tBase, ServerStreamChannel serverStreamChannel) {
            return StreamCode.OK;
        }

        @Override
        public short getCommandServiceCode() {
            return TCommandType.TRANSFER.getCode();
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
