/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;


/**
 * @author Woonduk Kang(emeroad)
 */
public class EmptyDataSender<T> implements EnhancedDataSender<T> {

    private static final DataSender<?> INSTANCE = new EmptyDataSender<>();

    @SuppressWarnings("unchecked")
    public static <T> DataSender<T> instance() {
        return (DataSender<T>) INSTANCE;
    }

    @Override
    public boolean send(T data) {
        return true;
    }


    @Override
    public void stop() {
    }

    @Override
    public boolean request(T data) {
        return true;
    }

    @Override
    public boolean request(T data, int retry) {
        return false;
    }


    @Override
    public boolean request(T data, FutureListener<ResponseMessage> listener) {
        return false;
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return false;
    }

}