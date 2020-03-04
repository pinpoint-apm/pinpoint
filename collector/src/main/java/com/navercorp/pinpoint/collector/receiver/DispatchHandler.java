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

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;

/**
 * @author emeroad
 * @author koo.taejin
 */
public interface DispatchHandler {

    // Separating Send and Request. That dose not be satisfied but try to change that later.
    void dispatchSendMessage(ServerRequest serverRequest);

    void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse);

}
