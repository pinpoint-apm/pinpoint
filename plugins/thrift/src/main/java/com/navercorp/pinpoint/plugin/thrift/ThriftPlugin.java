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

import java.security.ProtectionDomain;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncCallEndFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncCallRemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncMarkerFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncNextSpanIdFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncTraceIdFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.ServerMarkerFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.getter.TNonblockingTransportFieldGetter;
import com.navercorp.pinpoint.plugin.thrift.field.getter.TTransportFieldGetter;

/**
 * @author HyunGil Jeong
 */
public class ThriftPlugin implements ProfilerPlugin {

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        ThriftPluginConfig config = new ThriftPluginConfig(context.getConfig());

        boolean traceClient = config.traceThriftClient();
        boolean traceProcessor = config.traceThriftProcessor();
        boolean traceCommon = traceClient || traceProcessor;

        if (traceClient) {
            addInterceptorsForSynchronousClients(context, config);
            addInterceptorsForAsynchronousClients(context);
        }

        if (traceProcessor) {
            addInterceptorsForSynchronousProcessors(context);
            addInterceptorsForAsynchronousProcessors(context);
        }

        if (traceCommon) {
            addInterceptorsForRetrievingSocketAddresses(context);
            addTProtocolEditors(context, config);
        }
    }

    // Client - synchronous

    private void addInterceptorsForSynchronousClients(ProfilerPluginSetupContext context, ThriftPluginConfig config) {
        addTServiceClientEditor(context, config);
    }

    private void addTServiceClientEditor(ProfilerPluginSetupContext context, ThriftPluginConfig config) {
        final boolean traceServiceArgs = config.traceThriftServiceArgs();
        final boolean traceServiceResult = config.traceThriftServiceResult();

        final String targetClassName = "org.apache.thrift.TServiceClient";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                // TServiceClient.sendBase(String, TBase)
                final InstrumentMethod sendBase = target.getDeclaredMethod("sendBase", "java.lang.String", "org.apache.thrift.TBase");
                if (sendBase != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientSendBaseInterceptor";
                    sendBase.addInterceptor(interceptor, traceServiceArgs);
                }

                // TServiceClient.receiveBase(TBase, String)
                final InstrumentMethod receiveBase = target.getDeclaredMethod("receiveBase", "org.apache.thrift.TBase", "java.lang.String");
                if (receiveBase != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientReceiveBaseInterceptor";
                    receiveBase.addInterceptor(interceptor, traceServiceResult);
                }

                return target.toBytecode();
            }
        });
    }

    // Client - asynchronous

    private void addInterceptorsForAsynchronousClients(ProfilerPluginSetupContext context) {
        addTAsyncClientManagerEditor(context);
        addTAsyncMethodCallEditor(context);
    }

    private void addTAsyncClientManagerEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.async.TAsyncClientManager";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                // TAsyncClientManager.call(TAsyncMethodCall)
                final InstrumentMethod call = target.getDeclaredMethod("call", "org.apache.thrift.async.TAsyncMethodCall");
                if (call != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncClientManagerCallInterceptor";
                    call.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    private void addTAsyncMethodCallEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.async.TAsyncMethodCall";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(SocketAddressFieldAccessor.class.getName());
                target.addField(AsyncMarkerFlagFieldAccessor.class.getName());
                target.addField(AsyncTraceIdFieldAccessor.class.getName());
                target.addField(AsyncNextSpanIdFieldAccessor.class.getName());
                target.addField(AsyncCallEndFlagFieldAccessor.class.getName());
                target.addField(AsyncCallRemoteAddressFieldAccessor.class.getName());
                target.addGetter(TNonblockingTransportFieldGetter.class.getName(), ThriftConstants.T_ASYNC_METHOD_CALL_FIELD_TRANSPORT);

                // TAsyncMethodCall(TAsyncClient, TProtocolFactory, TNonblockingTransport, AsyncMethodCallback<T>, boolean)
                final InstrumentMethod constructor = target.getConstructor("org.apache.thrift.async.TAsyncClient",
                        "org.apache.thrift.protocol.TProtocolFactory", "org.apache.thrift.transport.TNonblockingTransport",
                        "org.apache.thrift.async.AsyncMethodCallback", "boolean");
                if (constructor != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallConstructInterceptor";
                    constructor.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.start(Selector)
                final InstrumentMethod start = target.getDeclaredMethod("start", "java.nio.channels.Selector");
                if (start != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallStartInterceptor";
                    start.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.doConnecting(SelectionKey)
                final InstrumentMethod doConnecting = target.getDeclaredMethod("doConnecting", "java.nio.channels.SelectionKey");
                if (doConnecting != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor";
                    doConnecting.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.doWritingRequestSize()
                final InstrumentMethod doWritingRequestSize = target.getDeclaredMethod("doWritingRequestSize");
                if (doWritingRequestSize != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor";
                    doWritingRequestSize.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.doWritingRequestBody(SelectionKey)
                final InstrumentMethod doWritingRequestBody = target.getDeclaredMethod("doWritingRequestBody", "java.nio.channels.SelectionKey");
                if (doWritingRequestBody != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallDoWritingRequestBodyInterceptor";
                    doWritingRequestBody.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.doReadingResponseSize()
                final InstrumentMethod doReadingResponseSize = target.getDeclaredMethod("doReadingResponseSize");
                if (doReadingResponseSize != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor";
                    doReadingResponseSize.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.doReadingResponseBody(SelectionKey)
                final InstrumentMethod doReadingResponseBody = target.getDeclaredMethod("doReadingResponseBody", "java.nio.channels.SelectionKey");
                if (doReadingResponseBody != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallDoReadingResponseBodyInterceptor";
                    doReadingResponseBody.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.cleanUpAndFireCallback(SelectionKey)
                final InstrumentMethod cleanUpAndFireCallback = target.getDeclaredMethod("cleanUpAndFireCallback", "java.nio.channels.SelectionKey");
                if (cleanUpAndFireCallback != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallCleanUpAndFireCallbackInterceptor";
                    cleanUpAndFireCallback.addInterceptor(interceptor);
                }

                // TAsyncMethodCall.onError(Exception)
                final InstrumentMethod onError = target.getDeclaredMethod("onError", "java.lang.Exception");
                if (onError != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallOnErrorInterceptor";
                    onError.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    // Processor - synchronous

    private void addInterceptorsForSynchronousProcessors(ProfilerPluginSetupContext context) {
        addTBaseProcessorEditor(context);
        addProcessFunctionEditor(context);
    }

    private void addTBaseProcessorEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.TBaseProcessor";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                // TBaseProcessor.process(TProtocol, TProtocol)
                final InstrumentMethod process = target.getDeclaredMethod("process", "org.apache.thrift.protocol.TProtocol",
                        "org.apache.thrift.protocol.TProtocol");
                if (process != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor";
                    process.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    private void addProcessFunctionEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.ProcessFunction";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(ServerMarkerFlagFieldAccessor.class.getName());

                // ProcessFunction.process(int, TProtocol, TProtocol, I)
                final InstrumentMethod process = target.getDeclaredMethod("process", "int", "org.apache.thrift.protocol.TProtocol",
                        "org.apache.thrift.protocol.TProtocol", "java.lang.Object");
                if (process != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor";
                    process.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    // Processor - asynchronous

    private void addInterceptorsForAsynchronousProcessors(ProfilerPluginSetupContext context) {
        addTBaseAsyncProcessorEditor(context);
    }

    private void addTBaseAsyncProcessorEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.TBaseAsyncProcessor";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(ServerMarkerFlagFieldAccessor.class.getName());
                target.addField(AsyncMarkerFlagFieldAccessor.class.getName());

                // TBaseAsyncProcessor.process(AbstractNonblockingServer$AsyncFrameBuffer)
                final InstrumentMethod process = target.getDeclaredMethod("process", "org.apache.thrift.server.AbstractNonblockingServer$AsyncFrameBuffer");
                if (process != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor";
                    process.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    // Common

    private void addInterceptorsForRetrievingSocketAddresses(ProfilerPluginSetupContext context) {
        // injector TTranports
        // TSocket(Socket), TSocket(String, int, int)
        addTTransportEditor(context, "org.apache.thrift.transport.TSocket",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TSocketConstructInterceptor", new String[] { "java.net.Socket" }, new String[] {
                        "java.lang.String", "int", "int" });

        // wrapper TTransports
        // TFramedTransport(TTransport), TFramedTransport(TTransport, int)
        addTTransportEditor(context, "org.apache.thrift.transport.TFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFramedTransportConstructInterceptor",
                new String[] { "org.apache.thrift.transport.TTransport" }, new String[] { "org.apache.thrift.transport.TTransport", "int" });
        // TFastFramedTransport(TTransport, int, int)
        addTTransportEditor(context, "org.apache.thrift.transport.TFastFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFastFramedTransportConstructInterceptor", new String[] {
                        "org.apache.thrift.transport.TTransport", "int", "int" });
        // TSaslClientTransport(TTransport), TSaslClientTransport(SaslClient, TTransport)
        addTTransportEditor(context, "org.apache.thrift.transport.TSaslClientTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TSaslTransportConstructInterceptor",
                new String[] { "org.apache.thrift.transport.TTransport" }, new String[] { "javax.security.sasl.SaslClient",
                        "org.apache.thrift.transport.TTransport" });

        // TMemoryInputTransport - simply add socket field
        addTTransportEditor(context, "org.apache.thrift.transport.TMemoryInputTransport");
        // TIOStreamTransport - simply add socket field
        addTTransportEditor(context, "org.apache.thrift.transport.TIOStreamTransport");

        // nonblocking
        addTNonblockingSocketEditor(context);
        // AbstractNonblockingServer$FrameBuffer(TNonblockingTransport, SelectionKey, AbstractSelectThread)
        addFrameBufferEditor(context);
    }

    // Common - transports

    private void addTTransportEditor(ProfilerPluginSetupContext context, String tTransportFqcn) {
        final String targetClassName = tTransportFqcn;
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(SocketFieldAccessor.class.getName());
                return target.toBytecode();
            }

        });
    }

    private void addTTransportEditor(ProfilerPluginSetupContext context, String tTransportClassName, final String tTransportInterceptorFqcn,
            final String[]... parameterTypeGroups) {
        final String targetClassName = tTransportClassName;
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(SocketFieldAccessor.class.getName());

                for (String[] parameterTypeGroup : parameterTypeGroups) {
                    final InstrumentMethod constructor = target.getConstructor(parameterTypeGroup);
                    if (constructor != null) {
                        constructor.addInterceptor(tTransportInterceptorFqcn);
                    }
                }

                return target.toBytecode();
            }

        });
    }

    private void addTNonblockingSocketEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.transport.TNonblockingSocket";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(SocketFieldAccessor.class.getName());
                target.addField(SocketAddressFieldAccessor.class.getName());

                // TNonblockingSocket(SocketChannel, int, SocketAddress)
                final InstrumentMethod constructor = target.getConstructor("java.nio.channels.SocketChannel", "int", "java.net.SocketAddress");
                if (constructor != null) {
                    String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TNonblockingSocketConstructInterceptor";
                    constructor.addInterceptor(interceptor);
                }

                return target.toBytecode();
            }

        });
    }

    private void addFrameBufferEditor(ProfilerPluginSetupContext context) {
        final String targetClassName = "org.apache.thrift.server.AbstractNonblockingServer$FrameBuffer";
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);
                target.addField(SocketFieldAccessor.class.getName());
                target.addGetter(TNonblockingTransportFieldGetter.class.getName(), ThriftConstants.FRAME_BUFFER_FIELD_TRANS_);

                // [THRIFT-1972] - 0.9.1 added a field for the wrapper around trans_ field, while getting rid of getInputTransport() method
                if (target.hasField(ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_)) {
                    target.addGetter(TTransportFieldGetter.class.getName(), ThriftConstants.FRAME_BUFFER_FIELD_IN_TRANS_);
                    // AbstractNonblockingServer$FrameBuffer(TNonblockingTransport, SelectionKey, AbstractSelectThread)
                    final InstrumentMethod constructor = target.getConstructor(
                            "org.apache.thrift.server.AbstractNonblockingServer", // inner class - implicit reference to outer class instance
                            "org.apache.thrift.transport.TNonblockingTransport", "java.nio.channels.SelectionKey",
                            "org.apache.thrift.server.AbstractNonblockingServer$AbstractSelectThread");
                    if (constructor != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferConstructInterceptor";
                        constructor.addInterceptor(interceptor);
                    }
                }

                // 0.8.0, 0.9.0 doesn't have a separate trans_ field - hook getInputTransport() method
                if (target.hasMethod("getInputTransport", "org.apache.thrift.transport.TTransport")) {
                    // AbstractNonblockingServer$FrameBuffer.getInputTransport(TTransport)
                    final InstrumentMethod getInputTransport = target.getDeclaredMethod("getInputTransport", "org.apache.thrift.transport.TTransport");
                    if (getInputTransport != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferGetInputTransportInterceptor";
                        getInputTransport.addInterceptor(interceptor);
                    }
                }

                return target.toBytecode();
            }

        });
    }

    // Common - protocols

    private void addTProtocolEditors(ProfilerPluginSetupContext context, ThriftPluginConfig config) {
        addTProtocolInterceptors(context, config, "org.apache.thrift.protocol.TBinaryProtocol");
        addTProtocolInterceptors(context, config, "org.apache.thrift.protocol.TCompactProtocol");
        addTProtocolInterceptors(context, config, "org.apache.thrift.protocol.TJSONProtocol");
    }

    private void addTProtocolInterceptors(ProfilerPluginSetupContext context, ThriftPluginConfig config, String tProtocolClassName) {
        final boolean traceThriftClient = config.traceThriftClient();
        final boolean traceThriftProcessor = config.traceThriftProcessor();

        final String targetClassName = tProtocolClassName;
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(Instrumentor instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {

                final InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

                // client
                if (traceThriftClient) {
                    // TProtocol.writeFieldStop()
                    final InstrumentMethod writeFieldStop = target.getDeclaredMethod("writeFieldStop");
                    if (writeFieldStop != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.client.TProtocolWriteFieldStopInterceptor";
                        writeFieldStop.addInterceptor(interceptor);
                    }
                }

                // processor
                if (traceThriftProcessor) {
                    target.addField(ServerMarkerFlagFieldAccessor.class.getName());
                    // TProtocol.readFieldBegin()
                    final InstrumentMethod readFieldBegin = target.getDeclaredMethod("readFieldBegin");
                    if (readFieldBegin != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor";
                        readFieldBegin.addInterceptor(interceptor);
                    }
                    // TProtocol.readBool, TProtocol.readBinary, TProtocol.readI16, TProtocol.readI64
                    final List<InstrumentMethod> readTTypes = target.getDeclaredMethods(MethodFilters.name("readBool", "readBinary", "readI16", "readI64"));
                    if (readTTypes != null) {
                        String tTypeCommonInterceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor";
                        for (InstrumentMethod readTType : readTTypes) {
                            if (readTType != null) {
                                readTType.addInterceptor(tTypeCommonInterceptor);
                            }
                        }
                    }
                    // TProtocol.readMessageEnd()
                    final InstrumentMethod readMessageEnd = target.getDeclaredMethod("readMessageEnd");
                    if (readMessageEnd != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor";
                        readMessageEnd.addInterceptor(interceptor);
                    }

                    // for async processors
                    target.addField(AsyncMarkerFlagFieldAccessor.class.getName());
                    // TProtocol.readMessageBegin()
                    final InstrumentMethod readMessageBegin = target.getDeclaredMethod("readMessageBegin");
                    if (readMessageBegin != null) {
                        String interceptor = "com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor";
                        readMessageBegin.addInterceptor(interceptor);
                    }
                }

                return target.toBytecode();
            }

        });
    }
}
