/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ClassInputStreamProvider;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.bootstrap.plugin.RequestRecorderFactory;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorDefinitionFactory;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolderIdGenerator;
import com.navercorp.pinpoint.profiler.instrument.mock.BasicInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.factory.ExceptionHandlerFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The agent only adds to a class it instruments, so the rewritten class must keep every constant pool entry of
 * the original at its original index and only append new ones. HotSpot then installs the redefined class without
 * rewriting constant pool references (see ASMClassWriter and JDK-8376185).
 */
public class ASMClassNodeAdapterConstantPoolTest {

    private static final String TARGET = "com/navercorp/pinpoint/profiler/instrument/mock/ArgsClass";

    private final ClassInputStreamProvider classInputStreamProvider = new SimpleClassInputStreamProvider();
    private final ASMClassNodeLoader loader = new ASMClassNodeLoader();

    @Test
    public void keepsConstantPoolWhenAddingFieldGetterAndInterface() throws Exception {
        final byte[] original = readClass(TARGET);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(classInputStreamProvider, classLoader, getClass().getProtectionDomain(), TARGET);
        Assertions.assertNotNull(adapter);

        final ASMFieldNodeAdapter field = adapter.addField("_$PINPOINT$_constantPoolTest", "Ljava/lang/Object;");
        Assertions.assertTrue(adapter.addGetterMethod("_$PINPOINT$_getConstantPoolTest", field));
        Assertions.assertTrue(adapter.addInterface("java/io/Serializable"));
        final byte[] rewritten = adapter.toByteArray();

        assertConstantPoolPrefixKept(original, rewritten);
        assertReadable(rewritten);
    }

    @Test
    public void keepsConstantPoolWhenAddingInterceptor() throws Exception {
        final byte[] original = readClass(TARGET);
        final ASMClass clazz = newASMClass(TARGET);
        final InstrumentMethod method = clazz.getDeclaredMethod("argIntType", "int");
        Assertions.assertNotNull(method);
        method.addInterceptor(BasicInterceptor.class);
        final byte[] rewritten = clazz.toBytecode();

        assertConstantPoolPrefixKept(original, rewritten);
        assertReadable(rewritten);
    }

    @Test
    public void classBuiltFromScratchStillWrites() throws Exception {
        // no reader: the adapter falls back to a fresh constant pool
        final ClassNode classNode = loader.get(TARGET);
        final ASMClassNodeAdapter adapter = new ASMClassNodeAdapter(classInputStreamProvider, Thread.currentThread().getContextClassLoader(), getClass().getProtectionDomain(), classNode);
        final byte[] rewritten = adapter.toByteArray();
        assertReadable(rewritten);
        Assertions.assertEquals(TARGET, new ClassReader(rewritten).getClassName());
    }

    private ASMClass newASMClass(String internalName) {
        final ProfilerConfig profilerConfig = mock(ProfilerConfig.class);
        final Provider<TraceContext> traceContextProvider = Providers.of(mock(TraceContext.class));
        final Provider<ApiMetaDataService> apiMetaDataService = Providers.of(mock(ApiMetaDataService.class));
        final ObjectBinderFactory objectBinderFactory = new ObjectBinderFactory(profilerConfig, traceContextProvider,
                mock(DataSourceMonitorRegistryService.class), mock(CustomMetricRegistryService.class), apiMetaDataService,
                new ASMGuardedInterceptorFactory(null), new ExceptionHandlerFactory(false), mock(RequestRecorderFactory.class));
        final EngineComponent engineComponent = new DefaultEngineComponent(objectBinderFactory, new InterceptorDefinitionFactory(),
                apiMetaDataService, new ScopeFactory(), new InterceptorHolderIdGenerator());
        final InstrumentContext pluginContext = newPluginContext();
        // own loader: addInterceptor defines InterceptorHolder$$<id> into the target loader and the id restarts
        // at 0 per generator, so sharing the app loader with other tests in the same JVM would collide
        final ClassLoader targetLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        final ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(pluginContext, targetLoader, getClass().getProtectionDomain(), internalName);
        return new ASMClass(engineComponent, pluginContext, adapter);
    }

    private static InstrumentContext newPluginContext() {
        final InstrumentContext pluginContext = mock(InstrumentContext.class);
        when(pluginContext.injectClass(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                final ClassLoader classLoader = (ClassLoader) invocation.getArguments()[0];
                final String name = (String) invocation.getArguments()[1];
                return classLoader.loadClass(name);
            }
        });
        when(pluginContext.getResourceAsStream(any(ClassLoader.class), any(String.class))).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader classLoader = (ClassLoader) invocation.getArguments()[0];
                final String name = (String) invocation.getArguments()[1];
                if (classLoader == null) {
                    classLoader = ClassLoader.getSystemClassLoader();
                }
                return classLoader.getResourceAsStream(name);
            }
        });
        return pluginContext;
    }

    private static byte[] readClass(String internalName) throws IOException {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(internalName + ".class")) {
            Assertions.assertNotNull(in, internalName);
            return IOUtils.toByteArray(in);
        }
    }

    private static void assertReadable(byte[] bytes) {
        new ClassReader(bytes).accept(new ClassNode(), 0);
    }

    private static void assertConstantPoolPrefixKept(byte[] original, byte[] rewritten) throws IOException {
        final List<byte[]> before = readConstantPool(original);
        final List<byte[]> after = readConstantPool(rewritten);
        Assertions.assertTrue(after.size() > before.size(), "new entries must be appended, before=" + before.size() + " after=" + after.size());
        for (int i = 1; i < before.size(); i++) {
            Assertions.assertArrayEquals(before.get(i), after.get(i), "constant pool entry #" + i + " moved");
        }
    }

    /**
     * Raw constant pool entries (tag + payload) indexed like the JVM does, index 0 and the second slot of
     * long/double entries are empty.
     */
    private static List<byte[]> readConstantPool(byte[] classFile) throws IOException {
        final DataInputStream in = new DataInputStream(new ByteArrayInputStream(classFile));
        Assertions.assertEquals(0xCAFEBABE, in.readInt());
        in.readUnsignedShort(); // minor
        in.readUnsignedShort(); // major
        final int count = in.readUnsignedShort();
        final List<byte[]> entries = new ArrayList<>(count);
        entries.add(null);
        for (int i = 1; i < count; i++) {
            final int tag = in.readUnsignedByte();
            final byte[] payload;
            switch (tag) {
                case 1: // Utf8
                    payload = readBytes(in, in.readUnsignedShort());
                    break;
                case 3: // Integer
                case 4: // Float
                case 9: // Fieldref
                case 10: // Methodref
                case 11: // InterfaceMethodref
                case 12: // NameAndType
                case 17: // Dynamic
                case 18: // InvokeDynamic
                    payload = readBytes(in, 4);
                    break;
                case 5: // Long
                case 6: // Double
                    payload = readBytes(in, 8);
                    break;
                case 7: // Class
                case 8: // String
                case 16: // MethodType
                case 19: // Module
                case 20: // Package
                    payload = readBytes(in, 2);
                    break;
                case 15: // MethodHandle
                    payload = readBytes(in, 3);
                    break;
                default:
                    throw new IllegalStateException("unknown constant pool tag " + tag + " at #" + i);
            }
            final byte[] entry = new byte[payload.length + 1];
            entry[0] = (byte) tag;
            System.arraycopy(payload, 0, entry, 1, payload.length);
            entries.add(entry);
            if (tag == 5 || tag == 6) {
                entries.add(null);
                i++;
            }
        }
        return entries;
    }

    private static byte[] readBytes(DataInputStream in, int length) throws IOException {
        final byte[] bytes = new byte[length];
        in.readFully(bytes);
        return bytes;
    }

    @SuppressWarnings("unused")
    private static String hex(byte[] bytes) {
        return bytes == null ? "null" : Arrays.toString(bytes);
    }
}
