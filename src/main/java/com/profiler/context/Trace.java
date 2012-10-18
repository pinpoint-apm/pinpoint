package com.profiler.context;

import com.profiler.common.util.AnnotationTranscoder;
import com.profiler.common.util.AnnotationTranscoder.Encoded;
import com.profiler.sender.DataSender;
import com.profiler.util.NamedThreadLocal;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public final class Trace {

    private static final Logger logger = Logger.getLogger(Trace.class.getName());

    private static final DeadlineSpanMap spanMap = new DeadlineSpanMap();
    private static final ThreadLocal<TraceIDStack> traceIdLocal = new NamedThreadLocal<TraceIDStack>("TraceId");
    private static volatile boolean tracingEnabled = true;

    private static final AnnotationTranscoder transcoder = new AnnotationTranscoder();

    private Trace() {
    }

    public static void handle(TraceHandler handler) {
        TraceIDStack traceIDStack = traceIdLocal.get();
        if (traceIDStack == null) {
            traceIDStack = new TraceIDStack();
            traceIdLocal.set(traceIDStack);
        }

        try {
            TraceID nextId = getNextTraceId();
            traceIDStack.incr();

            if (traceIDStack.getTraceId() == null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(getCurrentTraceId().toString());
                }
                traceIDStack.setTraceId(nextId);
            }

            handler.handle();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            traceIDStack.decr();
        }
    }

    public static void traceBlockBegin() {
        TraceIDStack traceIDStack = traceIdLocal.get();
        if (traceIDStack == null) {
            traceIDStack = new TraceIDStack();
            traceIdLocal.set(traceIDStack);
        }

        try {
            TraceID nextId = getNextTraceId();
            traceIDStack.incr();

            if (traceIDStack.getTraceId() == null) {
                traceIDStack.setTraceId(nextId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void traceBlockEnd() {
        TraceIDStack traceIDStack = traceIdLocal.get();
        traceIDStack.decr();
    }

    /**
     * Get current TraceID or if's not exists create new one and return it.
     *
     * @return
     */
    public static TraceID getTraceIdOrCreateNew() {
        // TraceID id = traceIdLocal.get();
        TraceIDStack stack = traceIdLocal.get();
        TraceID id = null;
        if (stack != null) {
            id = stack.getTraceId();
        }

        if (id == null) {
            System.out.println("create new traceid");

            id = TraceID.newTraceId();
            // traceIdLocal.set(id);

            if (stack == null) {
                traceIdLocal.set(new TraceIDStack());
            }

            traceIdLocal.get().setTraceId(id);
            return id;
        }

        return id;
    }

    public static boolean removeCurrentTraceIdFromStack() {
        TraceIDStack stack = traceIdLocal.get();
        TraceID traceId = null;

        if (stack != null) {
            traceId = stack.getTraceId();
        } else {
            // TODO : remove this log.
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                        "#############################################################" +
                                "\n# Something's going wrong. Stack is not exists.             #" +
                                "\n#############################################################");
            }

            stack = new TraceIDStack();
            traceIdLocal.set(stack);
        }

        if (traceId != null) {
            stack.clear();
            spanMap.remove(traceId);
            return true;
        }
        return false;
    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
    public static TraceID getCurrentTraceId() {
        // return traceIdLocal.get();
        TraceIDStack stack = traceIdLocal.get();

        if (stack == null) {
            return null;
        }

        return stack.getTraceId();
    }

    public static void enable() {
        tracingEnabled = true;
    }

    public static void disable() {
        tracingEnabled = false;
    }

    public static TraceID getNextTraceId() {
        TraceID current = getTraceIdOrCreateNew();
        return current.getNextTraceId();
    }

    public static void setTraceId(TraceID traceId) {
        // TODO: remove this, just for debugging.
        if (getCurrentTraceId() != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                        "###############################################################################################################" +
                                "\n# [DEBUG MSG] TraceID is overwritten." +
                                "\n#   Before : " + getCurrentTraceId() +
                                "\n#   After  : " + traceId +
                                "\n###############################################################################################################");
                new RuntimeException("TraceID overwritten.").printStackTrace();
            }
        }

        TraceIDStack stack = traceIdLocal.get();

        if (stack == null)
            traceIdLocal.set(new TraceIDStack());

        Trace.traceIdLocal.get().setTraceId(traceId);
        // Trace.traceIdLocal.set(traceId);
    }

    private static void mutate(TraceID traceId, SpanUpdater spanUpdater) {
        Span span = spanMap.update(traceId, spanUpdater);

        if (span.isExistsAnnotationKey(Annotation.ClientRecv.getCode()) || span.isExistsAnnotationKey(Annotation.ServerSend.getCode())) {
            // remove current context threadId from stack
            removeCurrentTraceIdFromStack();
            logSpan(span);
        }
    }

    static void logSpan(Span span) {
        try {
            // TODO: send span to the server.
            System.out.println("\n\n[WRITE SPAN] hashCode=" + span.hashCode() + ",\n\t " + span + ",\n\t SpanMap.size=" + spanMap.size() + ",\n\t CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");

            // TODO: remove this, just for debugging
            // if (spanMap.size() > 0) {
            // System.out.println("##################################################################");
            // System.out.println("# [DEBUG MSG] WARNING SpanMap size > 0 check spanMap.            #");
            // System.out.println("##################################################################");
            // System.out.println("current spamMap=" + spanMap);
            // }

            DataSender.getInstance().addDataToSend(span.toThrift());

            span.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void record(Annotation annotation) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode(), null);
    }

    public static void record(Annotation annotation, long duration) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode(), duration);
    }

    public static void recordAttribute(final String key, final String value) {
        recordAttibute(key, (Object) value);
    }

    public static void recordAttibute(final String key, final Object value) {
        if (!tracingEnabled)
            return;

        try {
            mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    Encoded enc = transcoder.encode(value);
                    span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, enc.getValueType(), enc.getBytes(), null));
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void recordMessage(String message) {
        if (!tracingEnabled)
            return;

        annotate(message, null);
    }

    public static void recordRpcName(final String service, final String rpc) {
        if (!tracingEnabled)
            return;

        try {
            mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    span.setServiceName(service);
                    span.setName(rpc);
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void recordTerminalEndPoint(final String endPoint) {
        recordEndPoint(endPoint, true);
    }

    public static void recordEndPoint(final String endPoint) {
        recordEndPoint(endPoint, false);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    private static void recordEndPoint(final String endPoint, final boolean isTerminal) {
        if (!tracingEnabled)
            return;

        try {
            mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    // set endpoint to both span and annotations
                    span.setEndPoint(endPoint);
                    span.setTerminal(isTerminal);
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static void annotate(final String key, final Long duration) {
        if (!tracingEnabled)
            return;

        try {
            mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
                @Override
                public Span updateSpan(Span span) {
                    span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, duration));
                    return span;
                }
            });
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}