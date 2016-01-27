package com.navercorp.pinpoint.plugin.dubbo.provider;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.plugin.dubbo.DubboConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.TraceObjectManagable;
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
@TraceObjectManagable
public class DubboProviderIT {

    @Mock
    private RpcInvocation rpcInvocation;
    private URL url;
    @Mock
    private Directory directory;
    @Mock
    private Invoker invoker;

    @Before
    public void setUp() {
        url = new URL("dubbo", "1.2.3.4", 5678);
        MockitoAnnotations.initMocks(this);
        when(directory.getUrl()).thenReturn(url);
        when(rpcInvocation.getMethodName()).thenReturn("toString");
        when(rpcInvocation.getInvoker()).thenReturn(invoker);
        when(invoker.getInterface()).thenReturn(String.class);
        when(rpcInvocation.getAttachment(DubboConstants.META_TRANSACTION_ID)).thenReturn("frontend.agent^1234567890^123321");
        when(rpcInvocation.getAttachment(DubboConstants.META_SPAN_ID)).thenReturn("9876543210");
        when(rpcInvocation.getAttachment(DubboConstants.META_PARENT_SPAN_ID)).thenReturn("1357913579");
        when(rpcInvocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_TYPE)).thenReturn("1000");
        when(rpcInvocation.getAttachment(DubboConstants.META_PARENT_APPLICATION_NAME)).thenReturn("test.dubbo.consumer");
        when(rpcInvocation.getAttachment(DubboConstants.META_FLAGS)).thenReturn("0");
    }

    @Test
    public void testProvider() throws NoSuchMethodException {
        AbstractProxyInvoker abstractProxyInvoker = new AbstractProxyInvoker(new String(), String.class, url) {
            @Override
            protected Object doInvoke(Object proxy, String methodName, Class[] parameterTypes, Object[] arguments) throws Throwable {
                Method method = proxy.getClass().getMethod(methodName, parameterTypes);
                return method.invoke(proxy, arguments);
            }
        };
        try {
            abstractProxyInvoker.invoke(rpcInvocation);
        } catch (RpcException ignore) {
            ignore.printStackTrace();
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTraceCount(1);
    }

    @Test
    public void testDoNotTrace() throws Exception {
        when(rpcInvocation.getAttachment(DubboConstants.META_DO_NOT_TRACE)).thenReturn("1");

        AbstractProxyInvoker abstractProxyInvoker = new AbstractProxyInvoker(new String(), String.class, url) {
            @Override
            protected Object doInvoke(Object proxy, String methodName, Class[] parameterTypes, Object[] arguments) throws Throwable {
                Method method = proxy.getClass().getMethod(methodName, parameterTypes);
                return method.invoke(proxy, arguments);
            }
        };
        try {
            abstractProxyInvoker.invoke(rpcInvocation);
        } catch (RpcException ignore) {
            ignore.printStackTrace();
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(0);
    }
}
