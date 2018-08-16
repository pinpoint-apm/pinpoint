package com.navercorp.pinpoint.plugin.fastjson;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ApplicationTypeDetector;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import org.junit.Test;

import java.io.InputStream;

public class FastjsonPluginTest {

    private FastjsonPlugin plugin = new FastjsonPlugin();

    @Test
    public void setTransformTemplate() {

        plugin.setTransformTemplate(new TransformTemplate(new InstrumentContext() {

            @Override
            public InstrumentClass getInstrumentClass(ClassLoader classLoader, String s, byte[] bytes) {
                return null;
            }

            @Override
            public boolean exist(ClassLoader classLoader, String s) {
                return false;
            }

            @Override
            public InterceptorScope getInterceptorScope(String s) {
                return null;
            }

            @Override
            public <T> Class<? extends T> injectClass(ClassLoader classLoader, String s) {
                return null;
            }

            @Override
            public InputStream getResourceAsStream(ClassLoader classLoader, String s) {
                return null;
            }

            @Override
            public void addClassFileTransformer(ClassLoader classLoader, String s, TransformCallback transformCallback) {
            }

            @Override
            public void addClassFileTransformer(Matcher matcher, TransformCallback transformCallback) {
            }

            @Override
            public void retransform(Class<?> aClass, TransformCallback transformCallback) {
            }
        }));
    }

    @Test
    public void setup() {

        plugin.setup(new ProfilerPluginSetupContext() {
            
            @Override
            public ProfilerConfig getConfig() {
                return new DefaultProfilerConfig();
            }

            @Override
            public void addApplicationTypeDetector(ApplicationTypeDetector... applicationTypeDetectors) {

            }

            @Override
            public void addJdbcUrlParser(JdbcUrlParserV2 jdbcUrlParserV2) {

            }
        });
    }
}