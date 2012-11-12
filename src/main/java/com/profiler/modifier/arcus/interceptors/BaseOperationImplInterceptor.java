package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.AsyncTrace;
import com.profiler.context.GlobalCallTrace;
import com.profiler.context.Trace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;

/**
 *
 */
public class BaseOperationImplInterceptor implements StaticAfterInterceptor {
    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {

        TraceContext traceContext = TraceContext.getTraceContext();
        GlobalCallTrace<AsyncTrace> globalCallTrace = traceContext.getGlobalCallTrace();
        AsyncTrace asyncTrace = globalCallTrace.removeTraceObject(1);

//        com.profiler.context.Trace.setTraceId(__nextTraceId);
//
//
//        /**
//         * After sending command to the Arcus server. now waiting server
//         * response.
//         */
//        code.append("if (newState == net.spy.memcached.ops.OperationState.READING) {");
//
//        code.append("	java.net.SocketAddress socketAddress = handlingNode.getSocketAddress();");
//        code.append("	if (socketAddress instanceof java.net.InetSocketAddress) {");
//        code.append("		java.net.InetSocketAddress addr = (java.net.InetSocketAddress) handlingNode.getSocketAddress();");
//        code.append("		com.profiler.context.Trace.recordTerminalEndPoint(\"ARCUS:\" + addr.getHostName() + \":\" + addr.getPort());");
//        code.append("	}");
//        code.append("	com.profiler.context.Trace.recordRpcName(\"ARCUS\", this.getClass().getSimpleName());");
//        code.append("	com.profiler.context.Trace.recordAttribute(\"arcus.command\", ((cmd == null) ? \"UNKNOWN\" : new String(cmd.array())));");
//        code.append("	com.profiler.StopWatch.start(this.hashCode());");
//        code.append("	com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientSend, System.nanoTime() - __commandCreatedTime);");

        /**
         * Received all response or timed out.
         */
//        code.append("} else if (newState == net.spy.memcached.ops.OperationState.COMPLETE || newState == net.spy.memcached.ops.OperationState.TIMEDOUT) {");
//        code.append("	if (exception != null) { ");
//        code.append("		com.profiler.context.Trace.recordAttribute(\"exception\", com.profiler.util.InterceptorUtils.exceptionToString(exception));");
//        code.append("	}");
//
//        code.append("	if (!cancelled) {");
//        code.append("		com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientRecv, com.profiler.StopWatch.stopAndGetElapsed(this.hashCode()));");
//        code.append("	} else {");
//        code.append("		com.profiler.context.Trace.recordAttribute(\"exception\", \"cancelled by user\");");
//        code.append("		com.profiler.context.Trace.record(com.profiler.context.Annotation.ClientRecv, System.nanoTime() - __cancelledTime);");
//        code.append("	}");
//
//        code.append("}");
//
////		code.append("com.profiler.context.Trace.traceBlockEnd();");
//        code.append("}");
    }
}
