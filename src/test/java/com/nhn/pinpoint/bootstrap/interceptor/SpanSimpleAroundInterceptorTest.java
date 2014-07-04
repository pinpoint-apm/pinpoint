package com.nhn.pinpoint.bootstrap.interceptor;

import com.nhn.pinpoint.bootstrap.context.RecordableTrace;
import com.nhn.pinpoint.bootstrap.context.Trace;
import junit.framework.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;


public class SpanSimpleAroundInterceptorTest {

    @Test
    public void lifeCycle() throws Exception {

        MockTraceContext context = new MockTraceContext();
        MockTrace mockTrace = new MockTrace();
        context.setTrace(mockTrace);

        TestSpanSimpleAroundInterceptor interceptor = new TestSpanSimpleAroundInterceptor();
        interceptor.setTraceContext(context);

        checkSpanInterceptor(context, interceptor);
    }

    @Test
    public void beforeExceptionLifeCycle() throws Exception {

        MockTraceContext context = new MockTraceContext();
        MockTrace mockTrace = new MockTrace();
        context.setTrace(mockTrace);

        TestSpanSimpleAroundInterceptor interceptor = new TestSpanSimpleAroundInterceptor() {
            @Override
            protected void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
                touchBefore();
                throw new RuntimeException();
            }
        };
        interceptor.setTraceContext(context);

        checkSpanInterceptor(context, interceptor);
    }

    @Test
    public void afterExceptionLifeCycle() throws Exception {

        MockTraceContext context = new MockTraceContext();
        MockTrace mockTrace = new MockTrace();
        context.setTrace(mockTrace);

        TestSpanSimpleAroundInterceptor interceptor = new TestSpanSimpleAroundInterceptor() {
            @Override
            protected void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
                touchAfter();
                throw new RuntimeException();
            }
        };
        interceptor.setTraceContext(context);

        checkSpanInterceptor(context, interceptor);
    }

    @Test
    public void beforeAfterExceptionLifeCycle() throws Exception {

        MockTraceContext context = new MockTraceContext();
        MockTrace mockTrace = new MockTrace();
        context.setTrace(mockTrace);

        TestSpanSimpleAroundInterceptor interceptor = new TestSpanSimpleAroundInterceptor() {
            @Override
            protected void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
                touchBefore();
                throw new RuntimeException();
            }

            @Override
            protected void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
                touchAfter();
                throw new RuntimeException();
            }
        };
        interceptor.setTraceContext(context);

        checkSpanInterceptor(context, interceptor);
    }

    @Test
    public void traceCreateFail() {
        MockTraceContext context = mock(MockTraceContext.class);
        when(context.newTraceObject()).thenReturn(null);

        MockTrace mockTrace = new MockTrace();
        context.setTrace(mockTrace);

        TestSpanSimpleAroundInterceptor interceptor = new TestSpanSimpleAroundInterceptor();
        interceptor.setTraceContext(context);

        checkTraceCreateFailInterceptor(context, interceptor);
    }

    private void checkSpanInterceptor(MockTraceContext context, TestSpanSimpleAroundInterceptor interceptor) {
        Trace createTrace = interceptor.createTrace(null, null);
        interceptor.before(new Object(), null);
        Assert.assertEquals(interceptor.getBeforeTouchCount(), 1);
        Trace before = context.currentRawTraceObject();
        Assert.assertEquals(createTrace, before);

        interceptor.after(new Object(), null, null, null);
        Assert.assertEquals(interceptor.getAfterTouchCount(), 1);
        Trace after = context.currentRawTraceObject();
        Assert.assertNull(after);
    }

    private void checkTraceCreateFailInterceptor(MockTraceContext context, TestSpanSimpleAroundInterceptor interceptor) {
        Trace createTrace = interceptor.createTrace(null, null);
        Assert.assertNull(createTrace);
        interceptor.before(new Object(), null);

        Assert.assertEquals(interceptor.getBeforeTouchCount(), 0);
        Assert.assertNull(context.currentRawTraceObject());

        interceptor.after(new Object(), null, null, null);
        Assert.assertEquals(interceptor.getAfterTouchCount(), 0);
        Assert.assertNull(context.currentRawTraceObject());
    }



    @Test
    public void testCreateTrace() throws Exception {

    }

    @Test
    public void testDoInAfterTrace() throws Exception {

    }

    public static class TestSpanSimpleAroundInterceptor extends SpanSimpleAroundInterceptor {
        private int beforeTouchCount;
        private int afterTouchCount;

        public TestSpanSimpleAroundInterceptor() {
            super(TestSpanSimpleAroundInterceptor.class);
        }

        @Override
        protected Trace createTrace(Object target, Object[] args) {
            return getTraceContext().newTraceObject();
        }

        @Override
        protected void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
            touchBefore();
        }

        protected void touchBefore() {
            beforeTouchCount++;
        }

        public int getAfterTouchCount() {
            return afterTouchCount;
        }

        @Override
        protected void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
            touchAfter();
        }

        protected void touchAfter() {
            afterTouchCount++;
        }

        public int getBeforeTouchCount() {
            return beforeTouchCount;
        }
    }
}