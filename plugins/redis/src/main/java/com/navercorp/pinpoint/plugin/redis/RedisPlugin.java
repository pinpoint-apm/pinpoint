package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassCondition;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.plugin.redis.filter.JedisPipelineMethodNames;
import com.navercorp.pinpoint.plugin.redis.filter.NameBasedMethodFilter;
import com.navercorp.pinpoint.profiler.modifier.redis.filter.JedisMethodNames;

public class RedisPlugin implements ProfilerPlugin, RedisConstants {

    private static final String STRING = "java.lang.String";
    private static final String INT = "int";
    private static final String URI = "java.net.URI";

    private static final String JEDIS = "redis.clients.jedis.Jedis";
    private static final String BINARY_JEDIS = "redis.clients.jedis.BinaryJedis";
    private static final String JEDIS_CLIENT = "redis.clients.jedis.Client";
    private static final String JEDIS_PIPELINE_BASE = "redis.clients.jedis.PipelineBase";
    private static final String JEDIS_MULTIKEY_PIPELINE_BASE = "redis.clients.jedis.MultiKeyPipelineBase";
    private static final String JEDIS_PIPELINE = "redis.clients.jedis.Pipeline";
    private static final String JEDIS_SHARD_INFO = "redis.clients.jedis.JedisShardInfo";

    private static final String JEDIS_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor";
    private static final String JEDIS_METHODS_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisMethodInterceptor";
    private static final String JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisClientConstructorInterceptor";
    private static final String JEDIS_PIPELINE_METHODS_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineMethodInterceptor";
    private static final String JEDIS_PIPELINE_SET_CLIENT_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineSetClientMethodInterceptor";
    private static final String JEDIS_PIPELINE_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineConstructorInterceptor";

    @Override
    public void setUp(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();

        if (enabled) {
            addJedisClassEditors(context, config);

            if (pipelineEnabled) {
                addJedisClientClassEditor(context, config);
                addJedisPipelineClassEditors(context, config);
            }
        }
    }

    private void addJedisClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = addJedisExtendedClassEditor(context, config, BINARY_JEDIS);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        addJedisExtendedClassEditor(context, config, JEDIS);
    }

    private ClassEditorBuilder addJedisExtendedClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config, final String targetClassName) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(targetClassName);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { STRING }) != null;
            }
        });
        constructorEditorBuilderArg1.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { STRING, INT }) != null;
            }
        });
        constructorEditorBuilderArg2.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg3 = classEditorBuilder.editConstructor(STRING, INT, INT);
        constructorEditorBuilderArg3.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { STRING, INT, INT }) != null;
            }
        });
        constructorEditorBuilderArg3.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg4 = classEditorBuilder.editConstructor(URI);
        constructorEditorBuilderArg4.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { URI }) != null;
            }
        });
        constructorEditorBuilderArg4.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg5 = classEditorBuilder.editConstructor(JEDIS_SHARD_INFO);
        constructorEditorBuilderArg5.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { JEDIS_SHARD_INFO }) != null;
            }
        });
        constructorEditorBuilderArg5.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(JedisMethodNames.get()));
        methodEditorBuilder.injectInterceptor(JEDIS_METHODS_INTERCEPTOR);

        return classEditorBuilder;
    }

    private void addJedisClientClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(JEDIS_CLIENT);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { STRING }) != null;
            }
        });
        constructorEditorBuilderArg1.injectInterceptor(JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { STRING, INT }) != null;
            }
        });
        constructorEditorBuilderArg2.injectInterceptor(JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR);
    }

    private void addJedisPipelineClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        addJedisPipelineExtendedClassEditor(context, config, JEDIS_PIPELINE_BASE);
        addJedisPipelineExtendedClassEditor(context, config, JEDIS_MULTIKEY_PIPELINE_BASE);

        final ClassEditorBuilder classEditorBuilder = addJedisPipelineExtendedClassEditor(context, config, JEDIS_PIPELINE);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);
        final MethodEditorBuilder setClientMethodEditorBuilder = classEditorBuilder.editMethods(new MethodFilter() {
            @Override
            public boolean filter(MethodInfo method) {
                return !method.getName().equals("setClient");
            }
        });
        setClientMethodEditorBuilder.injectInterceptor(JEDIS_PIPELINE_SET_CLIENT_METHOD_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(JEDIS_CLIENT);
        constructorEditorBuilder.condition(new ClassCondition() {
            @Override
            public boolean check(ClassLoader classLoader, InstrumentClass target) {
                return target.getConstructor(new String[] { JEDIS_CLIENT }) != null;
            }
        });
        constructorEditorBuilder.injectInterceptor(JEDIS_PIPELINE_CONSTRUCTOR_INTERCEPTOR);
    }

    private ClassEditorBuilder addJedisPipelineExtendedClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config, String targetClassName) {
        final ClassEditorBuilder classEditorBuilder = context.newClassEditorBuilder();
        classEditorBuilder.target(targetClassName);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(JedisPipelineMethodNames.get()));
        methodEditorBuilder.injectInterceptor(JEDIS_PIPELINE_METHODS_INTERCEPTOR);

        return classEditorBuilder;
    }
}