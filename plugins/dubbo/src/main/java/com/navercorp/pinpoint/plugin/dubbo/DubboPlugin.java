package com.navercorp.pinpoint.plugin.dubbo;

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
import com.navercorp.pinpoint.bootstrap.resolver.OverrideApplicationTypeResolver;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.security.ProtectionDomain;

/**
 * @author Jinkai.Ma
 */
public class DubboPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        DubboConfiguration config = new DubboConfiguration(context.getConfig());
        if (!config.isDubboEnabled()) {
            logger.info("DubboPlugin disabled");
            return;
        }

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final DubboProviderDetector dubboProviderDetector = new DubboProviderDetector(config.getDubboBootstrapMains());
            if (dubboProviderDetector.detect()) {
                logger.info("Detected application type : {}", DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
                context.setApplicationType(DubboConstants.DUBBO_PROVIDER_SERVICE_TYPE, OverrideApplicationTypeResolver.INSTANCE);
            }
        }

        logger.info("Adding Dubbo transformers");
        this.addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("com.alibaba.dubbo.rpc.protocol.AbstractInvoker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "com.alibaba.dubbo.rpc.Invocation");
                if (invokeMethod != null) {
                    invokeMethod.addInterceptor("com.navercorp.pinpoint.plugin.dubbo.interceptor.DubboConsumerInterceptor");
                }
                return target.toBytecode();
            }
        });
        transformTemplate.transform("com.alibaba.dubbo.rpc.proxy.AbstractProxyInvoker", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "com.alibaba.dubbo.rpc.Invocation");
                if (invokeMethod != null) {
                    invokeMethod.addInterceptor("com.navercorp.pinpoint.plugin.dubbo.interceptor.DubboProviderInterceptor");
                }
                return target.toBytecode();
            }
        });
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
