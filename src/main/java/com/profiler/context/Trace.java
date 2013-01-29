package com.profiler.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.Agent;
import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.SqlMetaData;
import com.profiler.common.util.ParsingResult;
import com.profiler.common.util.SqlParser;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.metadata.LRUCache;
import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;

/**
 * @author netspider
 */
public final class Trace {

    private final Logger logger = Logger.getLogger(Trace.class.getName());

    private static final DataSender DEFULT_DATA_SENDER = new LoggingDataSender();

    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    private short sequence;

    private boolean tracingEnabled = true;

    private CallStack callStack;

    private Storage storage;

    // TODO 아래 관련 핸들링 로직은 traceContext로 빼야지 이쁠뜻하다.
    private LRUCache sqlCache;
    private SqlParser sqlParser;

    // use for calculating depth of each Span.
    private Integer latestStackIndex = null;

    public Trace() {
        // traceObject에서 spanid의 유효성을 히스토리를 관리한다면 같은 thread에서는 span랜덤생성아이디의 충돌을 방지할수 있기는 함.
        TraceID traceId = TraceID.newTraceId();
        this.callStack = new CallStack(traceId);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public Trace(TraceID continueRoot) {
        // this.root = continueRoot;
        this.callStack = new CallStack(continueRoot);
        latestStackIndex = this.callStack.push();
        // StackFrame stackFrame = createStackFrame(ROOT_STACKID);
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public CallStack getCallStack() {
        return callStack;
    }


    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public DataSender getDataSender() {
        return storage.getDataSender();
    }

    public short getSequence() {
        return sequence++;
    }

    public AsyncTrace createAsyncTrace() {
        // 경우에 따라 별도 timeout 처리가 있어야 될수도 있음.
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        subSpan.setSequence(getSequence());
        AsyncTrace asyncTrace = new AsyncTrace(subSpan);
        // asyncTrace.setDataSender(this.getDataSender());
        asyncTrace.setStorage(this.storage);
        return asyncTrace;
    }

    private StackFrame createSubStackFrame(int stackId) {
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        SubStackFrame stackFrame = new SubStackFrame(subSpan);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSequence(getSequence());
        return stackFrame;
    }

    private StackFrame createRootStackFrame(int stackId, Span span) {
        RootStackFrame stackFrame = new RootStackFrame(span);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSpan(span);
        stackFrame.setStackFrameId(ROOT_STACKID);
        return stackFrame;
    }

    public void traceBlockBegin() {
        traceBlockBegin(NOCHECK_STACKID);
    }

    public void markBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markBeforeTime();
    }

    public long getBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getBeforeTime();
    }

    public void markAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markAfterTime();
    }

    public long getAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getAfterTime();
    }

//    public void attachObject(Object object) {
//        StackFrame stackFrame = getCurrentStackFrame();
//        stackFrame.attachObject(object);
//    }

    public void traceBlockBegin(int stackId) {
        int currentStackIndex = callStack.push();
        StackFrame stackFrame = createSubStackFrame(stackId);

        if (latestStackIndex != currentStackIndex) {
            latestStackIndex = currentStackIndex;
            SubSpan span = ((SubStackFrame) stackFrame).getSubSpan();
            span.setDepth(latestStackIndex);
        }

        callStack.setStackFrame(stackFrame);
    }

    public void traceBlockEnd() {
        traceBlockEnd(NOCHECK_STACKID);
    }

//    public void traceBlockFinalEnd() {
//        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
//        if (currentStackFrame.getStackFrameId() != ROOT_STACKID) {
//            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨.
//            logger.warning("Corrupted RootCallStack found. StackId not matched");
//        }
//        logSpan(currentStackFrame);
//    }

    public void traceBlockEnd(int stackId) {
        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
        if (currentStackFrame.getStackFrameId() != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨.
            logger.warning("Corrupted CallStack found. StackId not matched");
        }
        if (currentStackFrame instanceof RootStackFrame) {
            logSpan(((RootStackFrame) currentStackFrame).getSpan());
        } else {
            logSpan(((SubStackFrame) currentStackFrame).getSubSpan());
        }
        callStack.pop();
    }

    public StackFrame getCurrentStackFrame() {
        return callStack.getCurrentStackFrame();
    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
    public TraceID getTraceId() {
        return callStack.getSpan().getTraceID();
    }

    public void enable() {
        tracingEnabled = true;
    }

    public void disable() {
        tracingEnabled = false;
    }

    void logSpan(SubSpan subSpan) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SubSPAN]" + subSpan + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }
//            if (flushType == 0) {
//                storage.store(subSpan);
//            } else if(flushType == 1) {
//                dataSender.send(subSpan);
            this.storage.store(subSpan);
//            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void logSpan(Span span) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }

            // dataSender.send(span);
            this.storage.store(span);
            // subSpan.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Deprecated
    public void record(Annotation annotation) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode());
    }

    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            recordAttribute(AnnotationNames.EXCEPTION, th.getMessage());

            try {
                StackFrame currentStackFrame = getCurrentStackFrame();
                if (currentStackFrame instanceof RootStackFrame) {
                    ((RootStackFrame) currentStackFrame).getSpan().setException(true);
                } else {
                    ((SubStackFrame) currentStackFrame).getSubSpan().setException(true);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute(AnnotationNames.API, method);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // API 저장 방법의 개선 필요.
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute(AnnotationNames.API, method);
        recocordArgs(args);
    }

    public void recordApi(int apiId) {
        recordAttribute(AnnotationNames.API_ID, apiId);
    }

    public void recordApi(int apiId, Object[] args) {
        recordAttribute(AnnotationNames.API_ID, apiId);
        recocordArgs(args);
    }

    private void recocordArgs(Object[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                recordAttribute("args[" + i + "]", args[i]);
            }
        }
    }

    public void recordAttribute(final String key, final String value) {
        recordAttribute(key, (Object) value);
    }

    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = parseSql(sql);
        recordSqlParsingResult(parsingResult);
        return parsingResult;
    }

    public void recordSqlParsingResult(ParsingResult parsingResult) {
        recordAttribute(AnnotationNames.SQL_ID, parsingResult.getSql().hashCode());
        String output = parsingResult.getOutput();
        if (output != null && output.length() != 0) {
            recordAttribute(AnnotationNames.SQL_PARAM, output);
        }
    }

    public ParsingResult parseSql(String sql) {

        // 해당 api의 구현을 그냥 tarceContext api에 만들어야 될듯 하다.
        ParsingResult parsingResult = this.sqlParser.normalizedSql(sql);
        String normalizedSql = parsingResult.getSql();
        // 파싱시 변경되지 않았다면 동일 객체를 리턴하므로 그냥 ==비교를 하면 됨
        boolean newValue = this.sqlCache.put(normalizedSql);
        if (newValue) {
            if (logger.isLoggable(Level.FINE)) {
                // TODO hit% 로그를 남겨야 문제 발생시 도움이 될듯 하다.
                logger.fine("NewSQLParsingResult:" + parsingResult);
            }
            // newValue란 의미는 cache에 인입됬다는 의미이고 이는 신규 sql문일 가능성이 있다는 의미임.
            // 그러므로 메타데이터를 서버로 전송해야 한다.

            // 프로파일 데이터를 보내는데 사용되는 queue가 아니고,
            // 좀더 급한 메시지만 별도 처리할수 있는 상대적으로 더 한가한 queue와 datasender를 별도로 가지고 있는게 좋을듯 하다.
            SqlMetaData sqlMetaData = new SqlMetaData();
            sqlMetaData.setAgentId(Agent.getInstance().getAgentId());
            sqlMetaData.setStartTime(Agent.getInstance().getStartTime());
            sqlMetaData.setHashCode(normalizedSql.hashCode());
            sqlMetaData.setSql(normalizedSql);
            // 다른 우선순위가 더 높은 sender가 존재하면 좋을듯 하다.
            this.getStorage().getDataSender().send(sqlMetaData);
        }
        // hashId그냥 return String에서 까보면 됨.
        return parsingResult;
    }

    public void recordAttribute(final String key, final Object value) {
        if (!tracingEnabled)
            return;

        try {
            // TODO API 단일화 필요.
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new HippoAnnotation(key, value));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new HippoAnnotation(key, value));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordMessage(String message) {
        if (!tracingEnabled)
            return;

        annotate(message);
    }

    public void recordRpcName(final ServiceType serviceType, final String serviceName, final String rpc) {
        if (!tracingEnabled)
            return;

        try {
            // TODO API 단일화 필요.
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.setServiceType(serviceType);
                span.setServiceName(serviceName);
                span.setRpc(rpc);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setServiceType(serviceType);
                span.setServiceName(serviceName);
                span.setRpc(rpc);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordEndPoint(final String endPoint) {
        if (!tracingEnabled)
            return;
        // TODO API 단일화 필요.
        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.setEndPoint(endPoint);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setEndPoint(endPoint);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordNextSpanId(long spanId) {
        if (!tracingEnabled)
            return;

        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                logger.log(Level.WARNING, "OMG. Something's going wrong. Current stackframe is root Span. nextSpanId={}", spanId);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setNextSpanId(spanId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final String key) {
        if (!tracingEnabled)
            return;

        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new HippoAnnotation(key));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new HippoAnnotation(key));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void setSqlCache(LRUCache sqlCache) {
        this.sqlCache = sqlCache;
    }

    public void setSqlParser(SqlParser sqlParser) {
        this.sqlParser = sqlParser;
    }


}