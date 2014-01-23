package com.nhn.pinpoint.profiler.modifier.arcus.interceptor;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.nhn.pinpoint.rpc.server.SocketChannel;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationErrorType;
import net.spy.memcached.ops.OperationException;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.protocol.ascii.AsciiMemcachedNodeImpl;

import org.junit.Before;
import org.junit.Test;

import com.nhn.pinpoint.profiler.modifier.BaseInterceptorTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureGetInterceptorTest extends BaseInterceptorTest {

	private final Logger logger = LoggerFactory.getLogger(FutureGetInterceptorTest.class);

	FutureGetInterceptor interceptor = new FutureGetInterceptor();

	@Before
	public void beforeEach() {
		setInterceptor(interceptor);
		super.beforeEach();
	}

	@Test
	public void testSuccessful() {
		Long timeout = 1000L;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		
		MockOperationFuture future = mock(MockOperationFuture.class);
		MockOperation operation = mock(MockOperation.class);
		
		try {
			when(operation.getException()).thenReturn(null);
			when(operation.isCancelled()).thenReturn(false);
			when(future.__getOperation()).thenReturn(operation);

            MemcachedNode node = getMockMemcachedNode();
			when(operation.getHandlingNode()).thenReturn(node);
			
			interceptor.before(future, new Object[] { timeout, unit });
			interceptor.after(future, new Object[] { timeout, unit }, null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

    private MemcachedNode getMockMemcachedNode() throws IOException {
        java.nio.channels.SocketChannel socketChannel = java.nio.channels.SocketChannel.open();
        BlockingQueue<Operation> readQueue = new LinkedBlockingQueue<Operation>();
        BlockingQueue<Operation> writeQueue = new LinkedBlockingQueue<Operation> ();
        BlockingQueue<Operation> inputQueue = new LinkedBlockingQueue<Operation> ();

        return new AsciiMemcachedNodeImpl(new InetSocketAddress(11211), socketChannel, 128, readQueue, writeQueue, inputQueue, 1000L);
    }

    @Test
	public void testTimeoutException() {
		Long timeout = 1000L;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		
		MockOperationFuture future = mock(MockOperationFuture.class);
		MockOperation operation = mock(MockOperation.class);
		
		try {
			OperationException exception = new OperationException(OperationErrorType.GENERAL, "timed out");
			when(operation.getException()).thenReturn(exception);
			when(operation.isCancelled()).thenReturn(true);
			when(future.__getOperation()).thenReturn(operation);
			
			MemcachedNode node = getMockMemcachedNode();
			when(operation.getHandlingNode()).thenReturn(node);
			
			interceptor.before(future, new Object[] { timeout, unit });
			interceptor.after(future, new Object[] { timeout, unit }, null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	class MockOperationFuture extends OperationFuture {
		public MockOperationFuture(CountDownLatch l, AtomicReference oref,
				long opTimeout) {
			super(l, oref, opTimeout);
		}
		
		public String __getServiceCode() {
			return "MEMCACHED";
		}
		
		public Operation __getOperation() {
			return null;
		}
	}
	
	class MockOperation implements Operation {

		public String __getServiceCode() {
			return "MEMCACHED";
		}
		
		public void cancel() {
		}

		public ByteBuffer getBuffer() {
			return null;
		}

		public OperationCallback getCallback() {
			return null;
		}

		public OperationException getException() {
			return null;
		}

		public MemcachedNode getHandlingNode() {
			return null;
		}

		public OperationState getState() {
			return null;
		}

		public void handleRead(ByteBuffer arg0) {
			
		}

		public boolean hasErrored() {
			return false;
		}

		public void initialize() {
		}

		public boolean isCancelled() {
			return false;
		}

		public void readFromBuffer(ByteBuffer arg0) throws IOException {
		}

		public void setHandlingNode(MemcachedNode arg0) {
		}

		public void writeComplete() {
		}
		
	}

}
