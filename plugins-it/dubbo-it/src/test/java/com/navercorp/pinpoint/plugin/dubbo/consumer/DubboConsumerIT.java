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

package com.navercorp.pinpoint.plugin.dubbo.consumer;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jinkai.Ma
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-dubbo-plugin")
@Dependency({"com.alibaba:dubbo:[2.5.x,]", "org.mockito:mockito-all:1.8.4"})
public class DubboConsumerIT {

    @Mock
    private RpcInvocation rpcInvocation;
    private URL url;

    private AbstractInvoker abstractClusterInvoker;

    @Before
    public void setUp() {
        url = new URL("dubbo", "1.2.3.4", 5678);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConsumer() throws NoSuchMethodException {
        abstractClusterInvoker = new MockInvoker<Demo>(Demo.class, url);
        when(rpcInvocation.getInvoker()).thenReturn(abstractClusterInvoker);
        try {
            abstractClusterInvoker.invoke(rpcInvocation);
        } catch (RpcException e) {
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method invoke = AbstractInvoker.class.getMethod("invoke", Invocation.class);
        verifier.verifyTraceCount(1);
    }

    @Test
    public void testConsumerMonitor() {
        abstractClusterInvoker = mock(AbstractInvoker.class);
        when(rpcInvocation.getInvoker()).thenReturn(abstractClusterInvoker);
        when(abstractClusterInvoker.getInterface()).thenReturn(MonitorService.class);
        try {
            abstractClusterInvoker.invoke(rpcInvocation);
        } catch (RpcException ignore) {
            // ignore
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTraceCount(0);
    }

    public static interface Demo {
    }

    public static class MockInvoker<T> extends AbstractInvoker<T> {
        URL url;
        boolean available = true;
        boolean destoryed = false;
        Result result;
        RpcException exception;
        Callable<?> callable;

        public MockInvoker(Class<T> type, URL url) {
            super(type, url);
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public void setException(RpcException exception) {
            this.exception = exception;
        }

        public void setCallable(Callable<?> callable) {
            this.callable = callable;
        }

        @Override
        protected Result doInvoke(Invocation invocation) throws Throwable {
            if (callable != null) {
                try {
                    callable.call();
                } catch (Exception e) {
                    throw new RpcException(e);
                }
            }
            if (exception != null) {
                throw exception;
            } else {
                return result;
            }
        }
    }
}


