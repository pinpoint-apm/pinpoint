/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.request;

/**
 * @author jaehong.kim
 */
public interface ClientRequestTrace {
    void setHeader(String name, String value);

    /**
     * The Host request-header field specifies the Internet host and port number of the resource being requested.
     * e.g., www.w3.org
     *
     * @return If the value does not exist, it should return null.
     */
    String getHost();

    /**
     * The DestinationId is logical name of the destination.
     * <p>
     *
     * @return If the value does not exist, it should return "Unknown".
     */
    String getDestinationId();

    /**
     * URL
     *
     * @return If the value does not exist, it should return null.
     */
    String getUrl();

    /**
     * Entity
     *
     * @return If the value does not exist, it should return null.
     */
    String getEntityValue();

    /**
     * Cookie
     *
     * @return If the value does not exist, it should return null.
     */
    String getCookieValue();
}
