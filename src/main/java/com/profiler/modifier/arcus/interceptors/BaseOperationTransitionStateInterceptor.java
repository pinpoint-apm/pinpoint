package com.profiler.modifier.arcus.interceptors;

import com.profiler.context.Annotation;
import com.profiler.context.AsyncTrace;
import com.profiler.context.GlobalCallTrace;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import com.profiler.util.InterceptorUtils;
import com.profiler.util.MetaObject;
import com.profiler.util.StringUtils;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.protocol.BaseOperationImpl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BaseOperationTransitionStateInterceptor implements StaticBeforeInterceptor {

    private final Logger logger = Logger.getLogger(BaseOperationTransitionStateInterceptor.class.getName());
    private MetaObject asyncTraceId = new MetaObject<Integer>("__getAsyncTraceId");

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
        }
        TraceContext traceContext = TraceContext.getTraceContext();
        GlobalCallTrace globalCallTrace = traceContext.getGlobalCallTrace();

        Object asyncId = asyncTraceId.invoke(target);
        if (asyncId == null) {
            logger.fine("asyncId not found");
            return;
        }
        // saynctrace를 제거한다.
        AsyncTrace asyncTrace = globalCallTrace.removeTraceObject((Integer) asyncId);
        if (asyncTrace == null) {
            logger.fine("AsyncTrace already timeout");
            return;
        }
        OperationState newState = (OperationState) args[0];

        BaseOperationImpl baseOperation = (BaseOperationImpl) target;
        if (newState == OperationState.READING) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("event:" + newState + " asyncId:" + asyncId);
            }
            MemcachedNode handlingNode = baseOperation.getHandlingNode();
            SocketAddress socketAddress = handlingNode.getSocketAddress();
            if (socketAddress instanceof InetSocketAddress) {
                InetSocketAddress address = (InetSocketAddress) socketAddress;
                asyncTrace.recordTerminalEndPoint("ARCUS:" + address.getHostName() + ":" + address.getPort());
            }
            asyncTrace.recordRpcName("ARCUS", baseOperation.getClass().getSimpleName());
            String cmd = getCommand(baseOperation);
            asyncTrace.recordAttibute("arcus.command", cmd);

            TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
            timeObject.markSendTime();

            long createTime = asyncTrace.getSpan().getCreateTime();
            asyncTrace.record(Annotation.ClientSend, System.currentTimeMillis() - createTime);
        } else if (newState == OperationState.COMPLETE || newState == OperationState.TIMEDOUT) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("event:" + newState + " asyncId:" + asyncId);
            }
            Exception exception = baseOperation.getException();
            if (exception != null) {
                asyncTrace.recordAttibute("exception", InterceptorUtils.exceptionToString(exception));
            }
            if (!baseOperation.isCancelled()) {
                TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
                asyncTrace.record(Annotation.ClientRecv, timeObject.getSendTime());
            } else {
                asyncTrace.recordAttribute("exception", "cancelled by user");
                TimeObject timeObject = (TimeObject) asyncTrace.getAttachObject();
                asyncTrace.record(Annotation.ClientRecv, timeObject.getCancelTime());
            }
        }
    }

    private String getCommand(BaseOperationImpl baseOperation) {
        ByteBuffer buffer = baseOperation.getBuffer();
        if (buffer == null) {
            return "UNKNOWN";
        }
        // TODO 기본 인코딩은 뭔가? 동시성은 괜찮은건가? buffer 사이즈의 compact는 되어있는것인가.
        return new String(buffer.array());
    }


}
