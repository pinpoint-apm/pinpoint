/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.thrift;

import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.profiler.sender.ResultResponse;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;

/**
 * @author jaehong.kim
 */
public class ThriftMessageToResultConverter implements MessageConverter<ResultResponse> {
    @Override
    public ResultResponse toMessage(Object object) {
        if (object instanceof ResponseMessage) {
            final ResponseMessage responseMessage = (ResponseMessage) object;
            final byte[] byteMessage = responseMessage.getMessage();
            final Message<TBase<?, ?>> message = SerializationUtils.deserialize(byteMessage, HeaderTBaseDeserializerFactory.DEFAULT_FACTORY, null);
            if (message == null) {
                throw new IllegalArgumentException("message is null. response message=" + responseMessage);
            }

            final TBase<?, ?> tbase = message.getData();
            if (!(tbase instanceof TResult)) {
                throw new IllegalArgumentException("invalid message data. response message=" + responseMessage + ", data=" + tbase.getClass());
            }

            final TResult result = (TResult) tbase;
            return new ResultResponse() {
                @Override
                public boolean isSuccess() {
                    return result.isSuccess();
                }

                @Override
                public String getMessage() {
                    return result.getMessage();
                }
            };
        }
        return null;
    }
}