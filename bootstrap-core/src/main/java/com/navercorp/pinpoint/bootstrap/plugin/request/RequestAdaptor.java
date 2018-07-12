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

package com.navercorp.pinpoint.bootstrap.plugin.request;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface RequestAdaptor<T> {

    String getHeader(T request, String name);


    /**
     * Procedure name(optional)
     *
     * @return
     */
    String getRpcName(T request);

    /**
     * Server address
     *
     * @return
     */
    String getEndPoint(T request);

    /**
     * Client address
     *
     * @return
     */
    String getRemoteAddress(T request);

    /**
     * Server address that the client used
     *
     * @return
     */
    String getAcceptorHost(T request);
}
