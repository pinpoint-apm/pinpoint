package com.navercorp.pinpoint.plugin.apache.dubbo;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

/**
 * @author K
 * @date 2019-06-14-14:00
 */
public class ApacheDubboPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ApacheDubboConfiguration config = new ApacheDubboConfiguration(context.getConfig());
        if (!config.isDubboEnabled()) {
            logger.info("ApacheDubboPlugin disabled");
            return;
        }

        this.addApplicationTypeDetector(context, config);
        this.addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("org.apache.dubbo.rpc.protocol.AbstractInvoker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "org.apache.dubbo.rpc.Invocation");
                if (invokeMethod != null) {
                    invokeMethod.addInterceptor("com.navercorp.pinpoint.plugin.apache.dubbo.interceptor.ApacheDubboConsumerInterceptor");
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("org.apache.dubbo.rpc.proxy.AbstractProxyInvoker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "org.apache.dubbo.rpc.Invocation");
                if (invokeMethod != null) {
                    invokeMethod.addInterceptor("com.navercorp.pinpoint.plugin.apache.dubbo.interceptor.ApacheDubboProviderInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    /**
     * Pinpoint profiler agent uses this detector to find out the service type of current application.
     */
    private void addApplicationTypeDetector(ProfilerPluginSetupContext context, ApacheDubboConfiguration config) {
        context.addApplicationTypeDetector(new ApacheDubboProviderDetector(config.getDubboBootstrapMains()));
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
