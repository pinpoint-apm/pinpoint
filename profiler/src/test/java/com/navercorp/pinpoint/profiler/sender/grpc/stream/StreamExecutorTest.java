/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.profiler.sender.grpc.StreamId;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StreamExecutorTest {

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private StreamId id = StreamId.newStreamId("test");

    @Before
    public void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(2);
    }

    @After
    public void tearDown() throws Exception {
        if (executor != null) {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
            executor.shutdownNow();
        }
    }

//    @Test
//    public void start() throws InterruptedException {
//
//        BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
//        StreamState streamState = new StreamState(10, 100);
//        Reconnector reconnector = mock(Reconnector.class);
//        MessageDispatcherFactory dispatcherFactory = new MessageDispatcherFactory();
//        MockStreamMessageDispatcher<String> dispatcher = new MockStreamMessageDispatcher<String>();
//        StreamExecutorFactory<String, Object> streamFactory = new StreamExecutorFactory<String, Object>(executor, queue, streamState, reconnector);
//
//
//        StreamDecorator decorator = new StreamDecorator(id, streamFactory, dispatcherFactory, );
//
//        ClientCallStreamObserver<String> streamObserver = mock(ClientCallStreamObserver.class);
//        when(streamObserver.isReady()).thenReturn(true);
//
//        StreamExecutor<String, Object> streamExecutor = new StreamExecutor<String, Object>(id, executor, queue, streamState, dispatcher);
//        streamExecutor.start(streamObserver);
//        queue.add("abc");
//
//        Assert.assertTrue(await(dispatcher.onDispatch));
//        streamExecutor.cancel();
//
//        Assert.assertTrue(await(dispatcher.start));
//        Assert.assertTrue(await(dispatcher.stop));
//
//    }
//
//    private boolean await(CountDownLatch latch) throws InterruptedException {
//        return latch.await(3, TimeUnit.SECONDS);
//    }
//
//    static class MockStreamMessageDispatcher<ReqT, ResT> implements StreamMessageDispatcher<ReqT, ResT> {
//        private final StreamMessageDispatcher<ReqT, ResT> delegate;
//        public final CountDownLatch onDispatch = new CountDownLatch(1);
//        public final CountDownLatch start =  new CountDownLatch(1);
//        public final CountDownLatch stop = new CountDownLatch(1);
//
//        public MockStreamMessageDispatcher(StreamMessageDispatcher<ReqT, ResT> delegate) {
//            this.delegate = delegate;
//        }
//
//        @Override
//        public void onDispatch(ClientCallStreamObserver<ReqT>stream, Object message) {
//            this.delegate.onDispatch(stream, message);
//            onDispatch.countDown();
//        }
//
//        @Override
//        public void start(StreamExecutorFactory<ReqT, ResT> streamExecutorFactory) {
//            this.delegate.start(streamExecutorFactory);
//            start.countDown();
//        }
//        @Override
//        public void stop() {
//            delegate.stop();
//            stop.countDown();
//        }
//
//    };
}