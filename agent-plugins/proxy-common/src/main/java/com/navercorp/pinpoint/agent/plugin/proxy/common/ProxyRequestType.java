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

package com.navercorp.pinpoint.agent.plugin.proxy.common;

/**
 * <h3>Proxy Request Type (0 ~ 999)</h3>
 *
 * <table>
 * <tr><td>0</td><td>UNDEFINED</td></tr>
 * <tr><td>1</td><td>APP</td></tr>
 * <tr><td>2</td><td>NGINX</td></tr>
 * <tr><td>3</td><td>APACHE</td></tr>
 * </table>
 *
 * @author jaehong.kim
 */
public interface ProxyRequestType {
    /**
     * HTTP Header Name
     *
     * @return String
     */
    String getHttpHeaderName();

    /**
     * Web Display Name
     *
     * @return String
     */
    String getDisplayName();

    /**
     * Type Code
     *
     * @return int
     */
    int getCode();
}