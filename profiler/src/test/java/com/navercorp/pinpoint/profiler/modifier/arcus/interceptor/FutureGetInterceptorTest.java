/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

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

import com.navercorp.pinpoint.profiler.modifier.arcus.interceptor.FutureGetInterceptor;
import com.navercorp.pinpoint.test.BaseInterceptorTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FutureGetInterceptorTest extends BaseInterceptorTest {

    private final Logger logger = LoggerFactory.getLogger(FutureGetInterceptorTest.class)

	FutureGetInterceptor interceptor = new FutureGetIntercepto    ();

    @Before
	public void bef       reEach() {
		setIntercep       or(interceptor)        		s    per.beforeEach();
	}

	@Test       	public void test       uccessful() {
		Long timeout = 100             L;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		
		MockO       erationFuture future = mock(MockOperationFuture.                      lass);
		MockOperation operation = mock(M          ckOperation.class);
		
		try {
			when(ope          ation.getException()).thenReturn(null);
			when(operation.isCancelled()).thenReturn(false);
			when(futu          e.__getOperation()).thenReturn(operation);

                              MemcachedNode node = getMockMemcachedN          de();
			when(operation.getHandlingNode()).thenReturn(node);
			       			interceptor.befo          e(future, new O          ject[] { timeout, unit });
			interceptor.after(future, new Object[] { timeout, unit }, null, null);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

    private MemcachedNode getMockMemcachedNode() throws IOException {
        java.nio.channels.SocketChannel socketChannel = java.nio.channels.SocketChannel.open();
        BlockingQueue<Operation> readQueue = new LinkedBlockingQueue<Operation>();
        BlockingQueue<Operation> writeQueue = new LinkedBlockingQueue<Operation> ();
        BlockingQueue<Operation> inputQueue = new LinkedBlockingQueue<Operat    on> ();

        return new AsciiM       mcachedNodeImpl(n       w InetSocketAddress(11211), socket             hannel, 128, readQueue, writeQueue, inputQueue, 1000L)
    }

    @Test
	public void testTimeoutExcept                      on() {
		Long timeout = 1000L;
		TimeUnit unit = TimeUnit.MILLISECONDS;
		
		MockOperati          nFuture future = mock(MockOperationFuture.class          ;
		MockOperation operation = mock(MockOp          ration.class);
		
		try {
			OperationExceptio                    exception = new OperationExcepti          n(OperationErrorType.GENERAL, "timed out");
	                   	when(operation.getException()).thenReturn(excep          ion);
			when(operation.isCancelled()).thenReturn(true);
			when       future.__getOperati          n()).thenReturn                operation);
			
			MemcachedNode node = getMoc       MemcachedNode();
			when(operation.getHandlingNode()).thenRetu             n(node);

			interceptor.be                   ore(future, new Object[]            timeout, uni                    });
			interceptor.after(          uture,                new Object[] { timeout, unit }, null,        ull);
		} catch (Exception e)
			fail(e.ge                   Message());
	             }
	}
	
	class MockOperatio          Future             extends OperationFuture {
		public           ockOpe             ationFuture(CountDownLatch l, AtomicR          ferenc              oref,
				long opTimeout) {
			sup          r(l, o             ef, opTimeout);
		}
		
		publ          c Stri             g __getServiceCode() {
			return "ME                      CACHED";
		}
		
		p          blic Op             ration __getOperation             ) {
			return null;
		}

	
	cla             s MockOperation implements Operation {

		public String __g             tServiceCode() {
			return "MEMCACHED";
		}

		public void cancel()                {
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
