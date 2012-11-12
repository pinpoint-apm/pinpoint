package com.profiler.modifier.arcus;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.modifier.arcus.interceptors.ConstructInterceptor;

/**
 * @author netspider
 */
public class ArcusClientModifier extends AbstractModifier {

    private final Logger logger = Logger.getLogger(ArcusClientModifier.class.getName());

    public ArcusClientModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        super(byteCodeInstrumentor);
    }

    public String getTargetClass() {
        return "net/spy/memcached/protocol/BaseOperationImpl";
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Modifing. " + javassistClassName);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

            aClass.addTraceVariable("__asyncTraceId", "__setAsyncTraceId", "__getAsyncTraceId", "int");
            aClass.addConstructorInterceptor(null, new ConstructInterceptor());

//            /**
//             * inject both current and next traceId.
//             */
//            aClass.addTraceVariable("__traceId", "__setTraceId", "__getTraceId", "com.profiler.context.TraceID");
//            aClass.addTraceVariable("__nextTraceId", "__setNextTraceId", "__getNextTraceId", "com.profiler.context.TraceID");
//            aClass.insertCodeAfterConstructor(null, "{ __setTraceId(com.profiler.context.Trace.getCurrentTraceId()); __setNextTraceId(com.profiler.context.Trace.getNextTraceId()); }");
//
//            /**
//             * inject nano time for checking send time.
//             */
//            aClass.addTraceVariable("__commandCreatedTime", "__setCommandCreatedTime", "__getCommandCreatedTime", "long");
//            aClass.insertCodeAfterConstructor(null, "{ __setCommandCreatedTime(System.nanoTime()); }");
//
//            /**
//             * inject cancelled time
//             */
//            aClass.addTraceVariable("__cancelledTime", "__setCancelledTime", "__getCancelledTime", "long");
//
//            /**
//             * insert trace code.
//             */
//            aClass.insertCodeBeforeMethod("transitionState", new String[]{"net.spy.memcached.ops.OperationState"}, getTransitionStateAfterCode());
//            aClass.insertCodeBeforeMethod("cancel", null, getCancelBeforeCode());

            return aClass.toBytecode();
        } catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            return null;
        }
    }

    private String getCancelBeforeCode() {
        StringBuilder code = new StringBuilder();

        code.append("{");
        code.append("	if (!cancelled) {");
        code.append("		__setCancelledTime(System.nanoTime());");
        code.append("	}");
        code.append("}");

        return code.toString();
    }

    private String getTransitionStateAfterCode() {
        StringBuilder code = new StringBuilder();

        code.append("{");
//		code.append("com.profiler.context.Trace.traceBlockBegin();");

        /**
         * always override traceid
         */
        code.append("com.profiler.context.Trace.setTraceId(__nextTraceId);");

        /**
         * After sending command to the Arcus server. now waiting server
         * response.
         */
        code.append("if (newState == net.spy.memcached.ops.OperationState.READING) {");

        code.append("	java.net.SocketAddress socketAddress = handlingNode.getSocketAddress();");
        code.append("	if (socketAddress instanceof java.net.InetSocketAddress) {");
        code.append("		java.net.InetSocketAddress addr = (java.net.InetSocketAddress) handlingNode.getSocketAddress();");
        code.append("		com.profiler.context.Trace.recordTerminalEndPoint(\"ARCUS:\" + addr.getHostName() + \":\" + addr.getPort());");
        code.append("	}");
        code.append("	com.profiler.context.Trace.recordRpcName(\"ARCUS\", this.getClass().getSimpleName());");
        code.append("	com.profiler.context.Trace.recordAttribute(\"arcus.command\", ((cmd == null) ? \"UNKNOWN\" : new String(cmd.array())));");
        code.append("	com.profiler.StopWatch.start(this.hashCode());");
        code.append("	com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientSend, System.nanoTime() - __commandCreatedTime);");

        /**
         * Received all response or timed out.
         */
        code.append("} else if (newState == net.spy.memcached.ops.OperationState.COMPLETE || newState == net.spy.memcached.ops.OperationState.TIMEDOUT) {");
        code.append("	if (exception != null) { ");
        code.append("		com.profiler.context.Trace.recordAttribute(\"exception\", com.profiler.util.InterceptorUtils.exceptionToString(exception));");
        code.append("	}");

        code.append("	if (!cancelled) {");
        code.append("		com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientRecv, com.profiler.StopWatch.stopAndGetElapsed(this.hashCode()));");
        code.append("	} else {");
        code.append("		com.profiler.context.Trace.recordAttribute(\"exception\", \"cancelled by user\");");
        code.append("		com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientRecv, System.nanoTime() - __cancelledTime);");
        code.append("	}");

        code.append("}");

//		code.append("com.profiler.context.Trace.traceBlockEnd();");
        code.append("}");

        return code.toString();
    }
}