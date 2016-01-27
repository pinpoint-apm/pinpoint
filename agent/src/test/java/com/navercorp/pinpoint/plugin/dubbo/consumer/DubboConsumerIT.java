package com.navercorp.pinpoint.plugin.dubbo.consumer;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.monitor.MonitorService;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import com.alibaba.dubbo.rpc.cluster.support.FailoverClusterInvoker;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

/**
 * @author Jinkai.Ma
 */
@RunWith(PinpointPluginTestSuite.class)
@Dependency({ "com.alibaba:dubbo:2.5.3", "org.mockito:mockito-all:1.8.4" })
public class DubboConsumerIT {

    @Mock
    private RpcInvocation rpcInvocation;
    private URL url;
    @Mock
    private Directory directory;

    private AbstractClusterInvoker abstractClusterInvoker;

    @Before
    public void setUp() {
        url = new URL("dubbo", "1.2.3.4", 5678);
        MockitoAnnotations.initMocks(this);
        when(directory.getUrl()).thenReturn(url);
    }

    @Test
    public void testConsumer() throws NoSuchMethodException {
        abstractClusterInvoker = new FailoverClusterInvoker(directory);
        when(abstractClusterInvoker.getInterface()).thenReturn(String.class);

        try {
            abstractClusterInvoker.invoke(rpcInvocation);
        } catch (RpcException ignore) {
            // ignore
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        Method invoke = AbstractClusterInvoker.class.getMethod("invoke", Invocation.class);
        verifier.verifyTrace(Expectations.event("DUBBO_CONSUMER", invoke));

        verifier.verifyTraceCount(0);
    }

    @Test
    public void testConsumerMonitor() {
        abstractClusterInvoker = new FailoverClusterInvoker(directory);
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
}
