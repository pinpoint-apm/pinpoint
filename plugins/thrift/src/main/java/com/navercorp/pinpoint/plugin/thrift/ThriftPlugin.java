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

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;

/**
 * @author HyunGil Jeong
 */
public class ThriftPlugin implements ProfilerPlugin, ThriftConstants {
    
    @Override
    public void setup(ProfilerPluginContext context) {
        ThriftPluginConfig config = new ThriftPluginConfig(context.getConfig());
        context.setAttribute(ATTRIBUTE_CONFIG, config);
        
        boolean traceClient = config.traceThriftClient();
        boolean traceProcessor = config.traceThriftProcessor();
        boolean traceCommon = traceClient || traceProcessor;
        
        if (traceClient) {
            addInterceptorsForSynchronousClients(context);
            addInterceptorsForAsynchronousClients(context);
        }
        
        if (traceProcessor) {
            addInterceptorsForSynchronousProcessors(context);
            addInterceptorsForAsynchronousProcessors(context);
        }
        
        if (traceCommon) {
            addInterceptorsForRetrievingSocketAddresses(context);
            addTProtocolEditors(context);
        }
    }
    
    // Client - synchronous
    
    private void addInterceptorsForSynchronousClients(ProfilerPluginContext context) {
        addTServiceClientEditor(context);
    }
    
    private void addTServiceClientEditor(ProfilerPluginContext context) {
        ThriftPluginConfig config = (ThriftPluginConfig)context.getAttribute(ATTRIBUTE_CONFIG);
        boolean traceServiceArgs = config.traceThriftServiceArgs();
        boolean traceServiceResult = config.traceThriftServiceResult();
        
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.TServiceClient");
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        
        // TServiceClient.sendBase(String, TBase)
        final MethodTransformerBuilder sendBaseMethodTransformerBuilder = classTransformerBuilder.editMethod("sendBase", "java.lang.String", "org.apache.thrift.TBase");
        sendBaseMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        sendBaseMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientSendBaseInterceptor", traceServiceArgs);
        
        // TServiceClient.receiveBase(TBase, String)
        final MethodTransformerBuilder receiveBaseMethodTransformerBuilder = classTransformerBuilder.editMethod("receiveBase", "org.apache.thrift.TBase", "java.lang.String");
        receiveBaseMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        receiveBaseMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.TServiceClientReceiveBaseInterceptor", traceServiceResult);
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    // Client - asynchronous
    
    private void addInterceptorsForAsynchronousClients(ProfilerPluginContext context) {
        addTAsyncClientManagerEditor(context);
        addTAsyncMethodCallEditor(context);
    }
    
    private void addTAsyncClientManagerEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.async.TAsyncClientManager");
        classTransformerBuilder.injectMetadata(METADATA_NONBLOCKING_SOCKET_ADDRESS);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_NEXT_SPAN_ID);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_CALL_REMOTE_ADDRESS);
        
        // TAsyncClientManager.call(TAsyncMethodCall)
        final MethodTransformerBuilder callMethodTransformerBuilder = classTransformerBuilder.editMethod("call", "org.apache.thrift.async.TAsyncMethodCall");
        callMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        callMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncClientManagerCallInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    private void addTAsyncMethodCallEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.async.TAsyncMethodCall");
        classTransformerBuilder.injectMetadata(METADATA_NONBLOCKING_SOCKET_ADDRESS);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_MARKER);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_TRACE_ID);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_NEXT_SPAN_ID);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_CALL_REMOTE_ADDRESS);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_CALL_END_FLAG);
        classTransformerBuilder.injectFieldAccessor(FIELD_TRANSPORT_ASYNC_METHOD_CALL);
        
        // TAsyncMethodCall(TAsyncClient, TProtocolFactory, TNonblockingTransport, AsyncMethodCallback<T>, boolean)
        final ConstructorTransformerBuilder constructorTransformerBuilder = classTransformerBuilder.editConstructor(
                "org.apache.thrift.async.TAsyncClient",
                "org.apache.thrift.protocol.TProtocolFactory",
                "org.apache.thrift.transport.TNonblockingTransport",
                "org.apache.thrift.async.AsyncMethodCallback",
                "boolean");
        constructorTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallConstructInterceptor");
        
        // TAsyncMethodCall.start(Selector)
        final MethodTransformerBuilder startMethodTransformerBuilder = classTransformerBuilder.editMethod("start", "java.nio.channels.Selector");
        startMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        startMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallStartInterceptor");
        
        // TAsyncMethodCall.doConnecting(SelectionKey)
        final MethodTransformerBuilder doConnectingMethodTransformerBuilder = classTransformerBuilder.editMethod("doConnecting", "java.nio.channels.SelectionKey");
        doConnectingMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        doConnectingMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor");
        
        // TAsyncMethodCall.doWritingRequestSize()
        final MethodTransformerBuilder doWritingRequestSizeMethodTransformerBuilder = classTransformerBuilder.editMethod("doWritingRequestSize");
        doWritingRequestSizeMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        doWritingRequestSizeMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor");
        
        // TAsyncMethodCall.doWritingRequestBody(SelectionKey)
        final MethodTransformerBuilder doWritingRequestBodyMethodTransformerBuilder = classTransformerBuilder.editMethod("doWritingRequestBody", "java.nio.channels.SelectionKey");
        doWritingRequestBodyMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        doWritingRequestBodyMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallDoWritingRequestBodyInterceptor");
        
        // TAsyncMethodCall.doReadingResponseSize()
        final MethodTransformerBuilder doReadingResponseSizeMethodTransformerBuilder = classTransformerBuilder.editMethod("doReadingResponseSize");
        doReadingResponseSizeMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        doReadingResponseSizeMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallInternalMethodInterceptor");
        
        // TAsyncMethodCall.doReadingResponseBody(SelectionKey)
        final MethodTransformerBuilder doReadingResponseBodyMethodTransformerBuilder = classTransformerBuilder.editMethod("doReadingResponseBody", "java.nio.channels.SelectionKey");
        doReadingResponseBodyMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        doReadingResponseBodyMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallDoReadingResponseBodyInterceptor");
        
        // TAsyncMethodCall.cleanUpAndFireCallback(SelectionKey)
        final MethodTransformerBuilder cleanUpAndFireCallbackMethodTransformerBuilder = classTransformerBuilder.editMethod("cleanUpAndFireCallback", "java.nio.channels.SelectionKey");
        cleanUpAndFireCallbackMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        cleanUpAndFireCallbackMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallCleanUpAndFireCallbackInterceptor");
        
        // TAsyncMethodCall.onError(Exception)
        final MethodTransformerBuilder onErrorMethodTransformerBuilder = classTransformerBuilder.editMethod("onError", "java.lang.Exception");
        onErrorMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        onErrorMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.client.async.TAsyncMethodCallOnErrorInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    // Processor - synchronous
    
    private void addInterceptorsForSynchronousProcessors(ProfilerPluginContext context) {
        addTBaseProcessorEditor(context);
        addProcessFunctionEditor(context);
    }
    
    private void addTBaseProcessorEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.TBaseProcessor");
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        
        // TBaseProcessor.process(TProtocol, TProtocol)
        final MethodTransformerBuilder processMethodTransformerBuilder = classTransformerBuilder.editMethod("process", "org.apache.thrift.protocol.TProtocol", "org.apache.thrift.protocol.TProtocol");
        processMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        processMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }

    private void addProcessFunctionEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.ProcessFunction");
        classTransformerBuilder.injectMetadata(METADATA_SERVER_MARKER);
        
        // ProcessFunction.process(int, TProtocol, TProtocol, I)
        final MethodTransformerBuilder processMethodTransformerBuilder = classTransformerBuilder.editMethod("process", "int", "org.apache.thrift.protocol.TProtocol", "org.apache.thrift.protocol.TProtocol", "java.lang.Object");
        processMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        processMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    // Processor - asynchronous
    
    private void addInterceptorsForAsynchronousProcessors(ProfilerPluginContext context) {
        addTBaseAsyncProcessorEditor(context);
    }
    
    private void addTBaseAsyncProcessorEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.TBaseAsyncProcessor");
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        classTransformerBuilder.injectMetadata(METADATA_SERVER_MARKER);
        classTransformerBuilder.injectMetadata(METADATA_ASYNC_MARKER);
        
        // TBaseAsyncProcessor.process(AbstractNonblockingServer$AsyncFrameBuffer)
        final MethodTransformerBuilder processMethodTransformerBuilder = classTransformerBuilder.editMethod("process", "org.apache.thrift.server.AbstractNonblockingServer$AsyncFrameBuffer");
        processMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        processMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    // Common
    
    private void addInterceptorsForRetrievingSocketAddresses(ProfilerPluginContext context) {
        // injector TTranports
        // TSocket(Socket), TSocket(String, int, int)
        addTTransportEditor(context,"org.apache.thrift.transport.TSocket",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TSocketConstructInterceptor",
                new String[] {"java.net.Socket"},
                new String[] {"java.lang.String", "int", "int"});
        
        // wrapper TTransports
        // TFramedTransport(TTransport), TFramedTransport(TTransport, int)
        addTTransportEditor(context, "org.apache.thrift.transport.TFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFramedTransportConstructInterceptor",
                new String[] {"org.apache.thrift.transport.TTransport"},
                new String[] {"org.apache.thrift.transport.TTransport", "int"});
        // TFastFramedTransport(TTransport, int, int)
        addTTransportEditor(context, "org.apache.thrift.transport.TFastFramedTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TFastFramedTransportConstructInterceptor",
                new String[] {"org.apache.thrift.transport.TTransport", "int", "int"});
        // TSaslClientTransport(TTransport), TSaslClientTransport(SaslClient, TTransport)
        addTTransportEditor(context, "org.apache.thrift.transport.TSaslClientTransport",
                "com.navercorp.pinpoint.plugin.thrift.interceptor.transport.wrapper.TSaslTransportConstructInterceptor",
                new String[] {"org.apache.thrift.transport.TTransport"},
                new String[] {"javax.security.sasl.SaslClient", "org.apache.thrift.transport.TTransport"});
        
        // TMemoryInputTransport - simply inject socket metadata
        addTTransportEditor(context, "org.apache.thrift.transport.TMemoryInputTransport");
        
        // nonblocking
        addTNonblockingSocketEditor(context);
        // AbstractNonblockingServer$FrameBuffer(TNonblockingTransport, SelectionKey, AbstractSelectThread)
        addFrameBufferEditor(context);
    }
    
    // Common - transports
    
    private void addTTransportEditor(ProfilerPluginContext context, String tTransportClassName) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder(tTransportClassName);
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    private void addTTransportEditor(ProfilerPluginContext context, String tTransportClassName, String tTransportInterceptorClassName, String[] ... parameterTypeGroups ) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder(tTransportClassName);
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        
        for (String[] parameterTypeGroup : parameterTypeGroups) {
            final ConstructorTransformerBuilder constructorTransformerBuilder = classTransformerBuilder.editConstructor(parameterTypeGroup);
            constructorTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            constructorTransformerBuilder.injectInterceptor(tTransportInterceptorClassName);
        }
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    private void addTNonblockingSocketEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.transport.TNonblockingSocket");
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        classTransformerBuilder.injectMetadata(METADATA_NONBLOCKING_SOCKET_ADDRESS);

        // TNonblockingSocket(SocketChannel, int, SocketAddress)
        final ConstructorTransformerBuilder constructorTransformerBuilder = classTransformerBuilder.editConstructor("java.nio.channels.SocketChannel", "int", "java.net.SocketAddress");
        constructorTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.transport.TNonblockingSocketConstructInterceptor");
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    private void addFrameBufferEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder("org.apache.thrift.server.AbstractNonblockingServer$FrameBuffer");
        classTransformerBuilder.injectMetadata(METADATA_SOCKET);
        classTransformerBuilder.injectFieldAccessor(FIELD_FRAME_BUFFER_IN_TRANSPORT);

        final String[] parameterTypeNames = new String[] {
                "org.apache.thrift.server.AbstractNonblockingServer",   // inner class - implicit reference to outer class instance
                "org.apache.thrift.transport.TNonblockingTransport",
                "java.nio.channels.SelectionKey",
                "org.apache.thrift.server.AbstractNonblockingServer$AbstractSelectThread"
        };
        
        // [THRIFT-1972] - 0.9.1 added a field for the wrapper around trans_ field, while getting rid of getInputTransport() method
        classTransformerBuilder.conditional(ClassConditions.hasField(FIELD_FRAME_BUFFER_IN_TRANSPORT_WRAPPER), 
                new ConditionalClassFileTransformerSetup() {
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        conditional.injectFieldAccessor(FIELD_FRAME_BUFFER_IN_TRANSPORT_WRAPPER);
                        final ConstructorTransformerBuilder constructorTransformerBuilder = conditional.editConstructor(parameterTypeNames);
                        constructorTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
                        constructorTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferConstructInterceptor");
                    }
                }
        );
        // 0.8.0, 0.9.0 doesn't have a separate trans_ field - hooking getInputTransport() method
        classTransformerBuilder.conditional(ClassConditions.hasMethod("getInputTransport", "org.apache.thrift.transport.TTransport"),
                new ConditionalClassFileTransformerSetup() {
                    @Override
                    public void setup(ConditionalClassFileTransformerBuilder conditional) {
                        final MethodTransformerBuilder getInputTransportMethodTransformerBuilder = conditional.editMethod("getInputTransport");
                        getInputTransportMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
                        getInputTransportMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.server.nonblocking.FrameBufferGetInputTransportInterceptor");
                    }
                }
        );
        
        context.addClassFileTransformer(classTransformerBuilder.build());
    }
    
    // Common - protocols
    
    private void addTProtocolEditors(ProfilerPluginContext context) {
        addTProtocolInterceptors(context, "org.apache.thrift.protocol.TBinaryProtocol");
        addTProtocolInterceptors(context, "org.apache.thrift.protocol.TCompactProtocol");
        addTProtocolInterceptors(context, "org.apache.thrift.protocol.TJSONProtocol");
    }
    
    private void addTProtocolInterceptors(ProfilerPluginContext context, String tProtocolClassName) {
        ThriftPluginConfig config = (ThriftPluginConfig)context.getAttribute(ATTRIBUTE_CONFIG);
        
        final ClassFileTransformerBuilder classTransformerBuilder = context.getClassFileTransformerBuilder(tProtocolClassName);
        
        // client
        if (config.traceThriftClient()) {
            // TProtocol.writeFieldStop()
            final MethodTransformerBuilder writeFieldStopMethodTransformerBuilder = classTransformerBuilder.editMethod("writeFieldStop");
            writeFieldStopMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            writeFieldStopMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.client.TProtocolWriteFieldStopInterceptor");
        }
        
        // processor
        if (config.traceThriftProcessor()) {
            classTransformerBuilder.injectMetadata(METADATA_SERVER_MARKER);
            // TProtocol.readFieldBegin()
            final MethodTransformerBuilder readFieldBeginMethodTransformerBuilder = classTransformerBuilder.editMethod("readFieldBegin");
            readFieldBeginMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            readFieldBeginMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor");
            
            // TProtocol.readBool, TProtocol.readBinary, TProtocol.readI16, TProtocol.readI64
            final MethodTransformerBuilder readTTypeMethodTransformerBuilder = classTransformerBuilder.editMethods(MethodFilters.name("readBool", "readBinary", "readI16", "readI64"));
            readTTypeMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            readTTypeMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor");
            
            // TProtocol.readMessageEnd()
            final MethodTransformerBuilder readMessageEndMethodTransformerBuilder = classTransformerBuilder.editMethod("readMessageEnd");
            readMessageEndMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            readMessageEndMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor");
            
            // for async processors
            classTransformerBuilder.injectMetadata(METADATA_ASYNC_MARKER);
            // TProtocol.readMessageBegin()
            final MethodTransformerBuilder readMessageBeginMethodTransformerBuilder = classTransformerBuilder.editMethod("readMessageBegin");
            readMessageBeginMethodTransformerBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
            readMessageBeginMethodTransformerBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor");
        }
        context.addClassFileTransformer(classTransformerBuilder.build());
    }

}
