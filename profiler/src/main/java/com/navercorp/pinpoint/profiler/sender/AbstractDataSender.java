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

package com.navercorp.pinpoint.profiler.sender;

import java.util.Collection;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;

/**
 * 
 * @author koo.taejin
 */
public abstract class AbstractDataSender implements DataSender {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    abstract protected void sendPacket(Object dto);

    protected void sendPacketN(Collection<Object> messageList) {
        // Cannot use toArray(T[] array) because passed messageList doesn't implement it properly. 
        Object[] dataList = messageList.toArray();
        
        // No need to copy because this runs with single thread.
        // Object[] copy = Arrays.copyOf(original, original.length);

        final int size = messageList.size();
        for (int i = 0; i < size; i++) {
            try {
                sendPacket(dataList[i]);
            } catch (Throwable th) {
                logger.warn("Unexpected Error. Cause:{}", th.getMessage(), th);
            }
        }
    }

    protected AsyncQueueingExecutor<Object> createAsyncQueueingExecutor(int queueSize, String executorName) {
        final AsyncQueueingExecutor<Object> executor = new AsyncQueueingExecutor<Object>(queueSize, executorName);
        executor.setListener(new AsyncQueueingExecutorListener<Object>() {
            @Override
            public void execute(Collection<Object> messageList) {
                sendPacketN(messageList);
            }

            @Override
            public void execute(Object message) {
                sendPacket(message);
            }
        });
        return executor;
    }

    protected byte[] serialize(HeaderTBaseSerializer serializer, TBase tBase) {
        return SerializationUtils.serialize(tBase, serializer, null);
    }

    protected TBase<?, ?> deserialize(HeaderTBaseDeserializer deserializer, ResponseMessage responseMessage) {
        byte[] message = responseMessage.getMessage();
        return SerializationUtils.deserialize(message, deserializer, null);
    }

    protected static class RequestMarker {
        private final TBase tBase;
        private final int retryCount;
        private final FutureListener futureListener;

        protected RequestMarker(TBase tBase, int retryCount) {
            this.tBase = tBase;
            this.retryCount = retryCount;
            this.futureListener = null;
        }

        protected RequestMarker(TBase tBase, FutureListener futureListener) {
            this.tBase = tBase;
            this.retryCount = 3;
            this.futureListener = futureListener;
        }

        protected TBase getTBase() {
            return tBase;
        }

        protected int getRetryCount() {
            return retryCount;
        }

        protected FutureListener getFutureListener() {
            return futureListener;
        }
    }

}
