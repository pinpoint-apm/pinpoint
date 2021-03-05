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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriStatRecorderFactory;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.mock.BaseAnnotationInterceptor;
import com.navercorp.pinpoint.profiler.instrument.mock.BaseGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.BaseSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.IntAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.IntArrayAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.IntArraysAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.ObjectAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.ObjectArrayAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.ObjectArraysAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.PublicStrAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.ThrowExceptionAccessor;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldDefaultStaticFinalStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldDefaultStaticStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldDefaultStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldEnumGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldIntArrayGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldIntGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldMapGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldObjectArrayGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldObjectArraysGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldObjectGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldObjectMapGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPrivateStaticFinalStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPrivateStaticStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPrivateStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldProtectedStaticFinalStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldProtectedStaticStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldProtectedStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPublicStaticFinalStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPublicStaticStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldPublicStrGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldStrMapGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldTransientIntGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldVolatileIntGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.getter.FieldWildcardMapGetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldDefaultFinalStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldDefaultStaticStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldDefaultStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldEnumSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldIntArraySetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldIntSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldMapSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldObjectArraySetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldObjectArraysSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldObjectMapSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldObjectSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldPrivateStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldProtectedStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldPublicFinalStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldPublicStrSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldStrMapSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldTransientIntSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldVolatileIntSetter;
import com.navercorp.pinpoint.profiler.instrument.mock.setter.FieldWildcardMapSetter;
import com.navercorp.pinpoint.profiler.interceptor.factory.ExceptionHandlerFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jaehong.kim
 */
public class ASMClassTest {
    private final InterceptorRegistryBinder interceptorRegistryBinder = new DefaultInterceptorRegistryBinder();

    private final ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
    private final Provider<TraceContext> traceContextProvider = Providers.of(mock(TraceContext.class));
    private final DataSourceMonitorRegistryService dataSourceMonitorRegistryService = mock(DataSourceMonitorRegistryService.class);
    private final CustomMetricRegistryService customMetricRegistryService = mock(CustomMetricRegistryService.class);
    private final Provider<ApiMetaDataService> apiMetaDataService = Providers.of(mock(ApiMetaDataService.class));

    private final InstrumentContext pluginContext = mock(InstrumentContext.class);

    private final ExceptionHandlerFactory exceptionHandlerFactory = new ExceptionHandlerFactory(false);
    private final RequestRecorderFactory requestRecorderFactory = mock(RequestRecorderFactory.class);
    private final Provider<UriStatRecorderFactory> uriStatRecorderFactoryProvider = Providers.of(mock(UriStatRecorderFactory.class));

    private final ObjectBinderFactory objectBinderFactory = new ObjectBinderFactory(profilerConfig, traceContextProvider, dataSourceMonitorRegistryService,
            customMetricRegistryService, apiMetaDataService, exceptionHandlerFactory,
            requestRecorderFactory, uriStatRecorderFactoryProvider);
    private final ScopeFactory scopeFactory = new ScopeFactory();
    private final InterceptorDefinitionFactory interceptorDefinitionFactory = new InterceptorDefinitionFactory();

    private final EngineComponent engineComponent = new DefaultEngineComponent(objectBinderFactory, interceptorRegistryBinder, interceptorDefinitionFactory, apiMetaDataService, scopeFactory);


    @Before
    public void setUp() {

        when(pluginContext.injectClass(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<Class<?>>() {

            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];

                return loader.loadClass(name);
            }

        });
        when(pluginContext.getResourceAsStream(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<InputStream>() {

            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];
                if(loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }

                return loader.getResourceAsStream(name);
            }
        });
    }

    @Test
    public void getSuperClass() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertEquals("java.lang.Object", clazz.getSuperClass());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass", clazz.getSuperClass());

        clazz = getClass("java.lang.Object");
        assertEquals(null, clazz.getSuperClass());
    }

    @Test
    public void isInterface() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertEquals(false, clazz.isInterface());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseInterface");
        assertEquals(true, clazz.isInterface());
    }

    @Test
    public void getName() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass", clazz.getName());
    }

    @Test
    public void getInterfaces() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertEquals(0, clazz.getInterfaces().length);

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseInterface");
        assertEquals(0, clazz.getInterfaces().length);

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseImplementClass");
        assertEquals(1, clazz.getInterfaces().length);
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.BaseInterface", clazz.getInterfaces()[0]);
    }

    @Test
    public void getDeclaredMethod() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        assertNull(clazz.getDeclaredMethod("notExists"));

        assertNotNull(clazz.getDeclaredMethod("arg"));
        assertNotNull(clazz.getDeclaredMethod("argByteType", "byte"));
        assertNotNull(clazz.getDeclaredMethod("argShortType", "short"));
        assertNotNull(clazz.getDeclaredMethod("argIntType", "int"));
        assertNotNull(clazz.getDeclaredMethod("argFloatType", "float"));
        assertNotNull(clazz.getDeclaredMethod("argDoubleType", "double"));
        assertNotNull(clazz.getDeclaredMethod("argBooleanType", "boolean"));
        assertNotNull(clazz.getDeclaredMethod("argCharType", "char"));
        assertNotNull(clazz.getDeclaredMethod("argByteArrayType", "byte[]"));
        assertNotNull(clazz.getDeclaredMethod("argShortArrayType", "short[]"));
        assertNotNull(clazz.getDeclaredMethod("argIntArrayType", "int[]"));
        assertNotNull(clazz.getDeclaredMethod("argFloatArrayType", "float[]"));
        assertNotNull(clazz.getDeclaredMethod("argDoubleArrayType", "double[]"));
        assertNotNull(clazz.getDeclaredMethod("argBooleanArrayType", "boolean[]"));
        assertNotNull(clazz.getDeclaredMethod("argCharArrayType", "char[]"));

        assertNotNull(clazz.getDeclaredMethod("argByteArraysType", "byte[][]"));
        assertNotNull(clazz.getDeclaredMethod("argShortArraysType", "short[][]"));
        assertNotNull(clazz.getDeclaredMethod("argIntArraysType", "int[][]"));
        assertNotNull(clazz.getDeclaredMethod("argFloatArraysType", "float[][]"));
        assertNotNull(clazz.getDeclaredMethod("argDoubleArraysType", "double[][]"));
        assertNotNull(clazz.getDeclaredMethod("argBooleanArraysType", "boolean[][]"));
        assertNotNull(clazz.getDeclaredMethod("argCharArraysType", "char[][]"));

        assertNotNull(clazz.getDeclaredMethod("argAllType", "byte", "short", "int", "float", "double", "boolean", "char", "byte[]", "short[]", "int[]", "float[]", "double[]", "boolean[]", "char[]", "byte[][]", "short[][]", "int[][]", "float[][]", "double[][]", "boolean[][]", "char[][]"));
        assertNotNull(clazz.getDeclaredMethod("argObject", "java.lang.String", "java.lang.Object", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Boolean", "java.lang.Character", "java.lang.String[]", "java.lang.Object[]", "java.lang.Byte[]", "java.lang.Short[]", "java.lang.Integer[]", "java.lang.Long[]", "java.lang.Float[]", "java.lang.Double[]", "java.lang.Boolean[]", "java.lang.Character[]", "java.lang.String[][]", "java.lang.Object[][]", "java.lang.Byte[][]", "java.lang.Short[][]", "java.lang.Integer[][]", "java.lang.Long[][]", "java.lang.Float[][]", "java.lang.Double[][]", "java.lang.Boolean[][]", "java.lang.Character[][]"));
        assertNotNull(clazz.getDeclaredMethod("argArgs", "java.lang.Object[]"));

        assertNotNull(clazz.getDeclaredMethod("argInterface", "java.util.Map", "java.util.Map", "java.util.Map"));
        assertNotNull(clazz.getDeclaredMethod("argEnum", "java.lang.Enum"));
        assertNotNull(clazz.getDeclaredMethod("argEnumArray", "java.lang.Enum[]"));
        assertNotNull(clazz.getDeclaredMethod("argEnumArrays", "java.lang.Enum[][]"));

        // find super's method.
        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertNull(clazz.getDeclaredMethod("base"));
    }

    @Test
    public void getDeclaredMethods() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        List<InstrumentMethod> methods = clazz.getDeclaredMethods();
        assertNotNull(methods);

        methods = clazz.getDeclaredMethods(MethodFilters.name("arg"));
        assertEquals(1, methods.size());
        assertEquals("arg", methods.get(0).getName());
    }

    @Test
    public void getDeclaredConstructors() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        List<InstrumentMethod> constructors = clazz.getDeclaredConstructors();
        assertNotNull(constructors);
        assertEquals(2, constructors.size());
        assertEquals("ArgsClass", constructors.get(0).getName());

        assertEquals("ArgsClass", constructors.get(1).getName());
        assertArrayEquals(new String[] {"int"}, constructors.get(1).getParameterTypes());
    }

    @Test
    public void hasDeclaredMethod() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        assertFalse(clazz.hasDeclaredMethod("notExists"));

        assertTrue(clazz.hasDeclaredMethod("arg"));
        assertTrue(clazz.hasDeclaredMethod("argByteType", "byte"));

        // find super's method.
        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertFalse(clazz.hasDeclaredMethod("base"));
    }

    @Test
    public void hasMethod() throws Exception {
        // find not exists method.
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass");
        assertFalse(clazz.hasMethod("notExists"));

        // find method.
        assertTrue(clazz.hasMethod("arg"));
        assertTrue(clazz.hasMethod("argByteType", "byte"));

        // find super's method.
        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertTrue(clazz.hasMethod("base"));
    }

    @Test
    public void hasEnclosingMethod() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertFalse(clazz.hasEnclosingMethod("notExists"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.NestedClass$3LocalInner");
        assertTrue(clazz.hasEnclosingMethod("enclosingMethod", "java.lang.String", "int"));
    }

    @Test
    public void hasConstructor() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertTrue(clazz.hasConstructor());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertTrue(clazz.hasConstructor());
        assertTrue(clazz.hasConstructor("java.lang.String"));
        assertFalse(clazz.hasConstructor("java.lang.String", "int", "byte"));
    }

    @Test
    public void hasField() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertTrue(clazz.hasField("s"));
        assertFalse(clazz.hasField("notExists"));

        assertTrue(clazz.hasField("b", "boolean"));
        assertTrue(clazz.hasField("s", "java.lang.String"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        assertTrue(clazz.hasField("s"));
        assertTrue(clazz.hasField("e"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        assertTrue(clazz.hasField("b"));
        assertTrue(clazz.hasField("s"));
        assertTrue(clazz.hasField("i"));
        assertTrue(clazz.hasField("l"));
        assertTrue(clazz.hasField("f"));
        assertTrue(clazz.hasField("d"));
        assertTrue(clazz.hasField("y"));
        assertTrue(clazz.hasField("c"));

        assertTrue(clazz.hasField("bArray"));
        assertTrue(clazz.hasField("sArray"));
        assertTrue(clazz.hasField("iArray"));
        assertTrue(clazz.hasField("lArray"));
        assertTrue(clazz.hasField("fArray"));
        assertTrue(clazz.hasField("dArray"));
        assertTrue(clazz.hasField("yArray"));
        assertTrue(clazz.hasField("cArray"));

        assertTrue(clazz.hasField("bArrays"));
        assertTrue(clazz.hasField("sArrays"));
        assertTrue(clazz.hasField("iArrays"));
        assertTrue(clazz.hasField("lArrays"));
        assertTrue(clazz.hasField("fArrays"));
        assertTrue(clazz.hasField("dArrays"));
        assertTrue(clazz.hasField("yArrays"));
        assertTrue(clazz.hasField("cArrays"));

        assertTrue(clazz.hasField("str"));
        assertTrue(clazz.hasField("object"));

        assertTrue(clazz.hasField("publicStaticFinalStr"));
        assertTrue(clazz.hasField("volatileInt"));
        assertTrue(clazz.hasField("transientInt"));
    }

    @Test
    public void addDelegatorMethod() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass");
        clazz.addDelegatorMethod("base");
        assertNotNull(clazz.getDeclaredMethod("base"));

        // duplicated.
        try {
            clazz.addDelegatorMethod("extended");
            fail("skip throw exception.");
        } catch(Exception ignored) {
        }

        // not exist.
        try {
            clazz.addDelegatorMethod("notExist");
            fail("skip throw exception.");
        } catch(Exception ignored) {
        }

        clazz.addDelegatorMethod("getInstance");
        assertNotNull(clazz.getDeclaredMethod("getInstance"));
    }

    @Test
    public void addField() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(IntAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceInt", "int"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceInt"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(IntArrayAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceIntArray", "int[]"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceIntArray"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(IntArraysAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceIntArrays", "int[][]"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceIntArrays"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(ObjectAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceObject", "java.lang.Object"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceObject"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(ObjectArrayAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceObjectArray", "java.lang.Object[]"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceObjectArray"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(ObjectArraysAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceObjectArrays", "java.lang.Object[][]"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceObjectArrays"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(PublicStrAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTracePublicStr", "java.lang.String"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTracePublicStr"));

        // skip throw exception.
        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addField(ThrowExceptionAccessor.class);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTraceDefaultStr", "java.lang.String"));
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTraceDefaultStr"));
    }

    @Test
    public void addGetter() throws Exception {
        // TODO super field.
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addGetter(BaseGetter.class, "b");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_isB"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldIntGetter.class, "i");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getInt"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldIntArrayGetter.class, "iArray");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getIntArray"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldObjectGetter.class, "object");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getObject"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldObjectArrayGetter.class, "objectArray");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getObjectArray"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldObjectArraysGetter.class, "objectArrays");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getObjectArrays"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldEnumGetter.class, "e");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getEnum"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldMapGetter.class, "map");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getMap"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldStrMapGetter.class, "strMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getStrMap"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldObjectMapGetter.class, "objectMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getObjectMap"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldWildcardMapGetter.class, "wildcardMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getWildcardMap"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldDefaultStrGetter.class, "defaultStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getDefaultStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldDefaultStaticStrGetter.class, "defaultStaticStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getDefaultStaticStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldDefaultStaticFinalStrGetter.class, "defaultStaticFinalStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getDefaultStaticFinalStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPrivateStrGetter.class, "privateStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPrivateStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPrivateStaticStrGetter.class, "privateStaticStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPrivateStaticStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPrivateStaticFinalStrGetter.class, "privateStaticFinalStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPrivateStaticFinalStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldProtectedStrGetter.class, "protectedStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getProtectedStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldProtectedStaticStrGetter.class, "protectedStaticStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getProtectedStaticStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldProtectedStaticFinalStrGetter.class, "protectedStaticFinalStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getProtectedStaticFinalStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPublicStrGetter.class, "publicStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPublicStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPublicStaticStrGetter.class, "publicStaticStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPublicStaticStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldPublicStaticFinalStrGetter.class, "publicStaticFinalStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getPublicStaticFinalStr"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldVolatileIntGetter.class, "volatileInt");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getVolatileInt"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addGetter(FieldTransientIntGetter.class, "transientInt");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_getTransientInt"));
    }

    @Test
    public void addSetter() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addSetter(BaseSetter.class, "b");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setB", "boolean"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldIntSetter.class, "i");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setInt", "int"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldIntArraySetter.class, "iArray");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setIntArray", "int[]"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldObjectSetter.class, "object");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setObject", "java.lang.Object"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldObjectArraySetter.class, "objectArray");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setObjectArray", "java.lang.Object[]"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldObjectArraysSetter.class, "objectArrays");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setObjectArrays", "java.lang.Object[][]"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldEnumSetter.class, "e");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setEnum", "java.lang.Enum"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldMapSetter.class, "map");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setMap", "java.util.Map"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldStrMapSetter.class, "strMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setStrMap", "java.util.Map"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldObjectMapSetter.class, "objectMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setObjectMap", "java.util.Map"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldWildcardMapSetter.class, "wildcardMap");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setWildcardMap", "java.util.Map"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldDefaultStrSetter.class, "defaultStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setDefaultStr", "java.lang.String"));

        try {
            clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
            clazz.addSetter(FieldDefaultStaticStrSetter.class, "defaultStaticStr");
            assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setDefaultStaticStr", "java.lang.String"));
            fail("can't throw exception");
        } catch(Exception ignored) {
        }

        try {
            clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
            clazz.addSetter(FieldDefaultFinalStrSetter.class, "defaultFinalStr");
            assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setDefaultFinalStr", "java.lang.String"));
            fail("can't throw exception");
        } catch(Exception ignored) {
        }

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldPrivateStrSetter.class, "privateStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setPrivateStr", "java.lang.String"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldProtectedStrSetter.class, "protectedStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setProtectedStr", "java.lang.String"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldPublicStrSetter.class, "publicStr");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setPublicStr", "java.lang.String"));

        try {
            clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
            clazz.addSetter(FieldPublicFinalStrSetter.class, "publicFinalStr");
            assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setPublicFinalStr", "java.lang.String"));
            fail("can't throw exception");
        } catch(Exception ignored) {
        }

        // removeFinal is true
        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldPublicFinalStrSetter.class, "publicFinalStr", true);
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setPublicFinalStr", "java.lang.String"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldVolatileIntSetter.class, "volatileInt");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setVolatileInt", "int"));

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.FieldClass");
        clazz.addSetter(FieldTransientIntSetter.class, "transientInt");
        assertNotNull(clazz.getDeclaredMethod("_$PINPOINT$_setTransientInt", "int"));
    }

    @Test
    public void addInterceptor() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        clazz.addInterceptor(BaseAnnotationInterceptor.class);
    }

    @Test
    public void getNestedClasses() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.NestedClass");

        String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.NestedClass$StaticNested";
        assertEquals(1, clazz.getNestedClasses(ClassFilters.name(targetClassName)).size());
        assertEquals(targetClassName, clazz.getNestedClasses(ClassFilters.name(targetClassName)).get(0).getName());

        targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.NestedClass$InstanceInner";
        assertEquals(1, clazz.getNestedClasses(ClassFilters.name(targetClassName)).size());
        assertEquals(targetClassName, clazz.getNestedClasses(ClassFilters.name(targetClassName)).get(0).getName());

        targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.NestedClass$1LocalInner";
        assertEquals(1, clazz.getNestedClasses(ClassFilters.name(targetClassName)).size());
        assertEquals(targetClassName, clazz.getNestedClasses(ClassFilters.name(targetClassName)).get(0).getName());

        targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.NestedClass$1";
        assertEquals(1, clazz.getNestedClasses(ClassFilters.name(targetClassName)).size());
        assertEquals(targetClassName, clazz.getNestedClasses(ClassFilters.name(targetClassName)).get(0).getName());

        // find enclosing method condition.
        assertEquals(2, clazz.getNestedClasses(ClassFilters.enclosingMethod("annonymousInnerClass")).size());

        // find interface condition.
        assertEquals(2, clazz.getNestedClasses(ClassFilters.interfaze("java.util.concurrent.Callable")).size());

        // find enclosing method & interface condition.
        assertEquals(1, clazz.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("annonymousInnerClass"), ClassFilters.interfaze("java.util.concurrent.Callable"))).size());
    }

    @Test
    public void isInterceptorable() throws Exception {
        ASMClass clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseInterface");
        assertFalse(clazz.isInterceptable());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass");
        assertTrue(clazz.isInterceptable());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseEnum");
        assertTrue(clazz.isInterceptable());

        clazz = getClass("com.navercorp.pinpoint.profiler.instrument.mock.BaseEnum");
        assertTrue(clazz.isInterceptable());
    }


    private ASMClass getClass(final String targetClassName) throws Exception {
        ClassNode classNode = ASMClassNodeLoader.get(targetClassName);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return new ASMClass(engineComponent, pluginContext, classLoader, getClass().getProtectionDomain(), classNode);
    }
}