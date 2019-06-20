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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.apache.dubbo.interceptor.ApacheDubboConsumerInterceptor;
import com.navercorp.pinpoint.plugin.apache.dubbo.interceptor.ApacheDubboProviderInterceptor;

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
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        if (ServiceType.UNDEFINED.equals(context.getConfiguredApplicationType())) {
            final ApacheDubboProviderDetector dubboProviderDetector = new ApacheDubboProviderDetector(config.getDubboBootstrapMains());
            if (dubboProviderDetector.detect()) {
                logger.info("Detected application type : {}", ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
                if (!context.registerApplicationType(ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE)) {
                    logger.info("Application type [{}] already set, skipping [{}] registration.", context.getApplicationType(), ApacheDubboConstants.DUBBO_PROVIDER_SERVICE_TYPE);
                }
            }
        }

        logger.info("Adding Apache Dubbo transformers");
        this.addTransformers();
    }

    private void addTransformers() {
        transformTemplate.transform("org.apache.dubbo.rpc.protocol.AbstractInvoker", AbstractInvokerTransform.class);
        transformTemplate.transform("org.apache.dubbo.rpc.proxy.AbstractProxyInvoker", AbstractProxyInvokerTransform.class);
    }


    public static class AbstractInvokerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "org.apache.dubbo.rpc.Invocation");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(ApacheDubboConsumerInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    public static class AbstractProxyInvokerTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            InstrumentMethod invokeMethod = target.getDeclaredMethod("invoke", "org.apache.dubbo.rpc.Invocation");
            if (invokeMethod != null) {
                invokeMethod.addInterceptor(ApacheDubboProviderInterceptor.class);
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
