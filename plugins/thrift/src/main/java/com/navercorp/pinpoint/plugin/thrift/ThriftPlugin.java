/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientReceiveBaseInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientSendBaseInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncClientManagerCallInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallCleanUpAndFireCallbackInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallConstructInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallOnErrorInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferConstructInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferGetInputTransportInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.client.TProtocolWriteFieldStopInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor;
import com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TNonblockingSocketConstructInterceptor;

import java.security.ProtectionDomain;
import java.util.List;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author HyunGil Jeong
 */
public class ThriftPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ThriftPluginConfig config = new ThriftPluginConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        boolean traceClient = config.traceThriftClient();
        boolean traceClientAsync = config.traceThriftClientAsync();
        boolean traceProcessor = config.traceThriftProcessor();
        boolean traceProcessorAsync = config.traceThriftProcessorAsync();
        boolean traceCommon = traceClient || traceProcessor;

        if (traceClient) {
            addInterceptorsForSynchronousClients();
            addTHttpClientEditor();
            if (traceClientAsync) {
                addInterceptorsForAsynchronousClients();
            }
        }

        if (traceProcessor) {
            addInterceptorsForSynchronousProcessors();
            if (traceProcessorAsync) {
                addInterceptorsForAsynchronousProcessors();
            }
        }

        if (traceCommon) {
            addInterceptorsForRetrievingSocketAddresses();
            addTProtocolEditors(config);
        }
    }

    // Client - synchronous

    private void addInterceptorsForSynchronousClients() {
        addTServiceClientEditor();
    }

    private void addTServiceClientEditor() {
        final String targetClassName = "org.apache.thrift.TServiceClient";
        transformTemplate.transform(targetClassName, TServiceClientTransform.class);
    }

    public static class TServiceClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            ThriftPluginConfig config = new ThriftPluginConfig(instrumentor.getProfilerConfig());
            final boolean traceServiceArgs = config.traceThriftServiceArgs();
            final boolean traceServiceResult = config.traceThriftServiceResult();

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // TServiceClient.sendBase(String, TBase)
            final InstrumentMethod sendBase = target.getDeclaredMethod("sendBase", "java.lang.String", "org.apache.thrift.TBase");
            if (sendBase != null) {
                InterceptorScope thriftClientScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_CLIENT_SCOPE);
                sendBase.addScopedInterceptor(TServiceClientSendBaseInterceptor.class, va(traceServiceArgs), thriftClientScope, ExecutionPolicy.BOUNDARY);
            }

            // TServiceClient.receiveBase(TBase, String)
            final InstrumentMethod receiveBase = target.getDeclaredMethod("receiveBase", "org.apache.thrift.TBase", "java.lang.String");
            if (receiveBase != null) {
                receiveBase.addInterceptor(TServiceClientReceiveBaseInterceptor.class, va(traceServiceResult));
            }

            return target.toBytecode();
        }
    }


    // Client - asynchronous

    private void addInterceptorsForAsynchronousClients() {
        addTAsyncClientManagerEditor();
        addTAsyncMethodCallEditor();
    }

    private void addTAsyncClientManagerEditor() {
        final String targetClassName = "org.apache.thrift.async.TAsyncClientManager";
        transformTemplate.transform(targetClassName, TAsyncClientManagerTransform.class);
    }

    public static class TAsyncClientManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // TAsyncClientManager.call(TAsyncMethodCall)
            final InstrumentMethod call = target.getDeclaredMethod("call", "org.apache.thrift.async.TAsyncMethodCall");
            if (call != null) {
                InterceptorScope thriftClientScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_CLIENT_SCOPE);
                call.addScopedInterceptor(TAsyncClientManagerCallInterceptor.class, thriftClientScope, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    private void addTAsyncMethodCallEditor() {
        final String targetClassName = "org.apache.thrift.async.TAsyncMethodCall";
        transformTemplate.transform(targetClassName, TAsyncMethodCallTransform.class);
    }

    public static class TAsyncMethodCallTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(AsyncContextAccessor.class);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET_ADDRESS);
            target.addGetter(ThriftConstants.FIELD_GETTER_T_NON_BLOCKING_TRANSPORT, ThriftConstants.T_ASYNC_METHOD_CALL_FIELD_TRANSPORT);

            // TAsyncMethodCall(TAsyncClient, TProtocolFactory, TNonblockingTransport, AsyncMethodCallback<T>, boolean)
            final InstrumentMethod constructor = target.getConstructor("org.apache.thrift.async.TAsyncClient",
                    "org.apache.thrift.protocol.TProtocolFactory", "org.apache.thrift.transport.TNonblockingTransport",
                    "org.apache.thrift.async.AsyncMethodCallback", "boolean");
            if (constructor != null) {
                constructor.addInterceptor(TAsyncMethodCallConstructInterceptor.class);
            }

            // TAsyncMethodCall.cleanUpAndFireCallback(SelectionKey)
            final InstrumentMethod cleanUpAndFireCallback = target.getDeclaredMethod("cleanUpAndFireCallback", "java.nio.channels.SelectionKey");
            if (cleanUpAndFireCallback != null) {
                cleanUpAndFireCallback.addInterceptor(TAsyncMethodCallCleanUpAndFireCallbackInterceptor.class);
            }

            // TAsyncMethodCall.onError(Exception)
            final InstrumentMethod onError = target.getDeclaredMethod("onError", "java.lang.Exception");
            if (onError != null) {
                onError.addInterceptor(TAsyncMethodCallOnErrorInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    // Processor - synchronous

    private void addInterceptorsForSynchronousProcessors() {
        addTBaseProcessorEditor();
        addProcessFunctionEditor();
    }

    private void addTBaseProcessorEditor() {
        final String targetClassName = "org.apache.thrift.TBaseProcessor";
        transformTemplate.transform(targetClassName, TBaseProcessorTransform.class);
    }

    public static class TBaseProcessorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            // TBaseProcessor.process(TProtocol, TProtocol)
            final InstrumentMethod process = target.getDeclaredMethod("process", "org.apache.thrift.protocol.TProtocol",
                    "org.apache.thrift.protocol.TProtocol");
            if (process != null) {
                InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                process.addScopedInterceptor(TBaseProcessorProcessInterceptor.class, thriftServerScope, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    private void addProcessFunctionEditor() {
        final String targetClassName = "org.apache.thrift.ProcessFunction";
        transformTemplate.transform(targetClassName, ProcessFunctionTransform.class);
    }

    public static class ProcessFunctionTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SERVER_MARKER_FLAG);

            // ProcessFunction.process(int, TProtocol, TProtocol, I)
            final InstrumentMethod process = target.getDeclaredMethod("process", "int", "org.apache.thrift.protocol.TProtocol",
                    "org.apache.thrift.protocol.TProtocol", "java.lang.Object");
            if (process != null) {
                InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                process.addScopedInterceptor(ProcessFunctionProcessInterceptor.class, thriftServerScope, ExecutionPolicy.INTERNAL);
            }

            return target.toBytecode();
        }

    }

    // Processor - asynchronous

    private void addInterceptorsForAsynchronousProcessors() {
        addTBaseAsyncProcessorEditor();
    }

    private void addTBaseAsyncProcessorEditor() {
        final String targetClassName = "org.apache.thrift.TBaseAsyncProcessor";
        transformTemplate.transform(targetClassName, TBaseAsyncProcessorTransform.class);
    }

    public static class TBaseAsyncProcessorTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SERVER_MARKER_FLAG);
            target.addField(ThriftConstants.FIELD_ACCESSOR_ASYNC_MARKER_FLAG);

            // TBaseAsyncProcessor.process(AbstractNonblockingServer$AsyncFrameBuffer)
            final InstrumentMethod process = target.getDeclaredMethod("process", "org.apache.thrift.server.AbstractNonblockingServer$AsyncFrameBuffer");
            if (process != null) {
                InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                process.addScopedInterceptor(TBaseAsyncProcessorProcessInterceptor.class, thriftServerScope, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }

    }

    // THttpClient

    private void addTHttpClientEditor() {
        transformTemplate.transform("org.apache.thrift.transport.THttpClient", THttpClientTransform.class);
    }

    public static class THttpClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addGetter(ThriftConstants.FIELD_GETTER_URL, ThriftConstants.T_HTTP_CLIENT_FIELD_URL_);
            return target.toBytecode();
        }
    }

    // Common

    private void addInterceptorsForRetrievingSocketAddresses() {
        // injector TTranports
        // TSocket(Socket), TSocket(String, int, int)
        addTTransportEditor("org.apache.thrift.transport.TSocket",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TSocketConstructInterceptor", new String[]{"java.net.Socket"}, new String[]{
                        "java.lang.String", "int", "int"});

        // wrapper TTransports
        // TFramedTransport(TTransport), TFramedTransport(TTransport, int)
        addTTransportEditor("org.apache.thrift.transport.TFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFramedTransportConstructInterceptor",
                new String[]{"org.apache.thrift.transport.TTransport"}, new String[]{"org.apache.thrift.transport.TTransport", "int"});
        // TFastFramedTransport(TTransport, int, int)
        addTTransportEditor("org.apache.thrift.transport.TFastFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFastFramedTransportConstructInterceptor", new String[]{
                        "org.apache.thrift.transport.TTransport", "int", "int"});
        // TSaslClientTransport(TTransport), TSaslClientTransport(SaslClient, TTransport)
        addTTransportEditor("org.apache.thrift.transport.TSaslClientTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TSaslTransportConstructInterceptor",
                new String[]{"org.apache.thrift.transport.TTransport"}, new String[]{"javax.security.sasl.SaslClient",
                        "org.apache.thrift.transport.TTransport"});

        // TMemoryInputTransport - simply add socket field
        addTTransportEditor("org.apache.thrift.transport.TMemoryInputTransport");
        // TIOStreamTransport - simply add socket field
        addTTransportEditor("org.apache.thrift.transport.TIOStreamTransport");

        // nonblocking
        addTNonblockingSocketEditor();
        // AbstractNonblockingServer$FrameBuffer(TNonblockingTransport, SelectionKey, AbstractSelectThread)
        addFrameBufferEditor();
    }

    // Common - transports

    private void addTTransportEditor(String tTransportFqcn) {
        final String targetClassName = tTransportFqcn;
        transformTemplate.transform(targetClassName, TTransportTransform.class);
    }

    public static class TTransportTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET);
            return target.toBytecode();
        }

    }

    private void addTTransportEditor(String tTransportClassName, final String tTransportInterceptorFqcn,
                                     final String[]... parameterTypeGroups) {
        final String targetClassName = tTransportClassName;
        transformTemplate.transform(targetClassName, BaseTTransportTransform.class,
                new Object[]{tTransportInterceptorFqcn, parameterTypeGroups},
                new Class[]{String.class, String[][].class}
        );
    }

    public static class BaseTTransportTransform implements TransformCallback {

        private final String tTransportInterceptorFqcn;
        private final String[][] parameterTypeGroups;

        public BaseTTransportTransform(final String tTransportInterceptorFqcn,
                                       final String[][] parameterTypeGroups) {
            this.tTransportInterceptorFqcn = tTransportInterceptorFqcn;
            this.parameterTypeGroups = parameterTypeGroups;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET);

            for (String[] parameterTypeGroup : parameterTypeGroups) {
                final InstrumentMethod constructor = target.getConstructor(parameterTypeGroup);
                if (constructor != null) {
                    constructor.addInterceptor(tTransportInterceptorFqcn);
                }
            }

            return target.toBytecode();
        }

    }

    private void addTNonblockingSocketEditor() {
        final String targetClassName = "org.apache.thrift.transport.TNonblockingSocket";
        transformTemplate.transform(targetClassName, TNonblockingSocketTransform.class);
    }

    public static class TNonblockingSocketTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET_ADDRESS);

            // TNonblockingSocket(SocketChannel, int, SocketAddress)
            final InstrumentMethod constructor = target.getConstructor("java.nio.channels.SocketChannel", "int", "java.net.SocketAddress");
            if (constructor != null) {
                constructor.addInterceptor(TNonblockingSocketConstructInterceptor.class);
            }

            return target.toBytecode();
        }

    }

    private void addFrameBufferEditor() {
        final String targetClassName = "org.apache.thrift.server.AbstractNonblockingServer$FrameBuffer";
        transformTemplate.transform(targetClassName, FrameBufferTransform.class);
    }

    public static class FrameBufferTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ThriftConstants.FIELD_ACCESSOR_SOCKET);
            target.addGetter(ThriftConstants.FIELD_GETTER_T_NON_BLOCKING_TRANSPORT, ThriftConstants.FRAME_BUFFER_FIELD_TRANS_);

            // [THRIFT-1972] - 0.9.1 added a field for the wrapper around trans_ field, while getting rid of getInputTransport() method
            if (target.hasField(ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_)) {
                target.addGetter(ThriftConstants.FIELD_GETTER_T_TRANSPORT, ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_);
                // AbstractNonblockingServer$FrameBuffer(TNonblockingTransport, SelectionKey, AbstractSelectThread)
                final InstrumentMethod constructor = target.getConstructor(
                        "org.apache.thrift.server.AbstractNonblockingServer", // inner class - implicit reference to outer class instance
                        "org.apache.thrift.transport.TNonblockingTransport", "java.nio.channels.SelectionKey",
                        "org.apache.thrift.server.AbstractNonblockingServer$AbstractSelectThread");
                if (constructor != null) {
                    constructor.addInterceptor(FrameBufferConstructInterceptor.class);
                }
            }

            // 0.8.0, 0.9.0 doesn't have a separate trans_ field - hook getInputTransport() method
            if (target.hasMethod("getInputTransport", "org.apache.thrift.transport.TTransport")) {
                // AbstractNonblockingServer$FrameBuffer.getInputTransport(TTransport)
                final InstrumentMethod getInputTransport = target.getDeclaredMethod("getInputTransport", "org.apache.thrift.transport.TTransport");
                if (getInputTransport != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferGetInputTransportInterceptor";
                    getInputTransport.addInterceptor(FrameBufferGetInputTransportInterceptor.class);
                }
            }
            return target.toBytecode();
        }

    }

    // Common - protocols

    private void addTProtocolEditors(ThriftPluginConfig config) {
        addTProtocolInterceptors(config, "org.apache.thrift.protocol.TBinaryProtocol");
        addTProtocolInterceptors(config, "org.apache.thrift.protocol.TCompactProtocol");
        addTProtocolInterceptors(config, "org.apache.thrift.protocol.TJSONProtocol");
        addTProtocolDecoratorEditor();
    }

    private void addTProtocolInterceptors(ThriftPluginConfig config, String tProtocolClassName) {
        final String targetClassName = tProtocolClassName;
        transformTemplate.transform(targetClassName, TProtocolTransform.class);
    }

    public static class TProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            ThriftPluginConfig config = new ThriftPluginConfig(instrumentor.getProfilerConfig());
            final boolean traceThriftClient = config.traceThriftClient();
            final boolean traceThriftProcessor = config.traceThriftProcessor();

            // client
            if (traceThriftClient) {
                // TProtocol.writeFieldStop()
                final InstrumentMethod writeFieldStop = target.getDeclaredMethod("writeFieldStop");
                if (writeFieldStop != null) {
                    InterceptorScope thriftClientScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_CLIENT_SCOPE);
                    writeFieldStop.addScopedInterceptor(TProtocolWriteFieldStopInterceptor.class, thriftClientScope, ExecutionPolicy.INTERNAL);
                }
            }

            // processor
            if (traceThriftProcessor) {
                target.addField(ThriftConstants.FIELD_ACCESSOR_SERVER_MARKER_FLAG);
                // TProtocol.readFieldBegin()
                final InstrumentMethod readFieldBegin = target.getDeclaredMethod("readFieldBegin");
                if (readFieldBegin != null) {
                    InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                    readFieldBegin.addScopedInterceptor(TProtocolReadFieldBeginInterceptor.class, thriftServerScope, ExecutionPolicy.INTERNAL);
                }
                // TProtocol.readBool, TProtocol.readBinary, TProtocol.readI16, TProtocol.readI64
                final List<InstrumentMethod> readTTypes = target.getDeclaredMethods(MethodFilters.name("readBool", "readBinary", "readI16", "readI64"));
                if (readTTypes != null) {
                    InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                    for (InstrumentMethod readTType : readTTypes) {
                        if (readTType != null) {
                            readTType.addScopedInterceptor(TProtocolReadTTypeInterceptor.class, thriftServerScope, ExecutionPolicy.INTERNAL);
                        }
                    }
                }
                // TProtocol.readMessageEnd()
                final InstrumentMethod readMessageEnd = target.getDeclaredMethod("readMessageEnd");
                if (readMessageEnd != null) {
                    InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                    readMessageEnd.addScopedInterceptor(TProtocolReadMessageEndInterceptor.class, thriftServerScope, ExecutionPolicy.INTERNAL);
                }

                // for async processors
                target.addField(ThriftConstants.FIELD_ACCESSOR_ASYNC_MARKER_FLAG);
                // TProtocol.readMessageBegin()
                final InstrumentMethod readMessageBegin = target.getDeclaredMethod("readMessageBegin");
                if (readMessageBegin != null) {
                    InterceptorScope thriftServerScope = instrumentor.getInterceptorScope(ThriftScope.THRIFT_SERVER_SCOPE);
                    readMessageBegin.addScopedInterceptor(TProtocolReadMessageBeginInterceptor.class, thriftServerScope, ExecutionPolicy.INTERNAL);
                }
            }

            return target.toBytecode();
        }

    }

    private void addTProtocolDecoratorEditor() {
        transformTemplate.transform("org.apache.thrift.protocol.TProtocolDecorator", TProtocolDecoratorTransform.class);
    }

    public static class TProtocolDecoratorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addGetter(ThriftConstants.FIELD_GETTER_T_PROTOCOL, "concreteProtocol");
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
