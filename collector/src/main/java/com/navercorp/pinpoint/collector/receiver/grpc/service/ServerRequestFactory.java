/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.UidFetcher;
import com.navercorp.pinpoint.io.util.MessageType;
import io.grpc.Context;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface ServerRequestFactory {

    <T> ServerRequest<T> newServerRequest(Context context, MessageType messageType, T data);

    <T> ServerRequest<T> newServerRequest(Context context, UidFetcher uidFetcher, MessageType messageType, T data);
}