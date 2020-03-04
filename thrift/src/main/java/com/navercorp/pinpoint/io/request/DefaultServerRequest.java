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

package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.io.header.Header;
import com.navercorp.pinpoint.io.header.HeaderEntity;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultServerRequest<T> extends DefaultAttributeMap implements ServerRequest<T> {

    private final Message<T> message;
    private final String remoteAddress;
    private final int remotePort;

    public DefaultServerRequest(Message<T> message, String remoteAddress, int remotePort) {
        if (message == null) {
            throw new NullPointerException("message");
        }
        if (remoteAddress == null) {
            throw new NullPointerException("remoteAddress");
        }
        this.message = message;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }


    @Override
    public Header getHeader() {
        return message.getHeader();
    }

    @Override
    public HeaderEntity getHeaderEntity() {
        return message.getHeaderEntity();
    }

    @Override
    public T getData() {
        return message.getData();
    }

    @Override
    public String getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public int getRemotePort() {
        return remotePort;
    }

    @Override
    public String toString() {
        return "DefaultServerRequest{" + "message=" + message + ", remoteAddress='" + remoteAddress + '\'' + ", remotePort=" + remotePort + '}';
    }
}
