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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class AgentInfoSenderListener implements FutureListener<ResponseMessage> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean isSuccessful;

    public AgentInfoSenderListener(AtomicBoolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    @Override
    public void onComplete(Future<ResponseMessage> future) {
        try {
            if (future != null && future.isSuccess()) {
                TBase<?, ?> tbase = deserialize(future);
                if (tbase instanceof TResult) {
                    TResult result = (TResult) tbase;
                    if (result.isSuccess()) {
                        logger.debug("result success");
                        this.isSuccessful.set(true);
                        return;
                    } else {
                        logger.warn("request fail. Caused:{}", result.getMessage());
                    }
                } else {
                    logger.warn("Invalid Class. {}", tbase);
                }
            }
        } catch(Exception e) {
            logger.warn("request fail. caused:{}", e.getMessage());
        }
    }

    private TBase<?, ?> deserialize(Future<ResponseMessage> future) {
        final ResponseMessage responseMessage = future.getResult();

        // TODO Should we change this to thread local cache? This object's life cycle is different because it could be created many times.
        // Should we cache this?
        byte[] message = responseMessage.getMessage();
        return SerializationUtils.deserialize(message, HeaderTBaseDeserializerFactory.DEFAULT_FACTORY, null);
        
    }

}
