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

package com.navercorp.pinpoint.rpc.security;

import com.navercorp.pinpoint.common.util.Assert;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * @author Taejin Koo
 */
public class MockAuthenticationManager {

    static AuthenticationManager createServer(String key) {
        return new Server(key, 1);
    }

    static AuthenticationManager createServer(String key, long waitingTimeMillis) {
        return new Server(key, waitingTimeMillis);
    }

    static AuthenticationManager createClient() {
        return new Client();
    }

    private static class Server implements AuthenticationManager {

        private final String key;
        private final long waitingTimeMillis;

        public Server(String key) {
            this(key, 1);
        }

        public Server(String key, long waitingTimeMillis) {
            this.key = Assert.requireNonNull(key, "key must not be null");
            this.waitingTimeMillis = waitingTimeMillis;
        }

        @Override
        public AuthenticationResult authenticate(Authentication authentication, ChannelHandlerContext channelHandlerContext) {
            String message = new String(authentication.getPayload());

            try {
                Thread.sleep(waitingTimeMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (key.equals(message)) {
                return new AuthenticationResult(true, "success".getBytes());
            } else {
                return new AuthenticationResult(false, "fail".getBytes());
            }
        }

    }


    private static class Client implements AuthenticationManager {

        @Override
        public AuthenticationResult authenticate(Authentication authentication, ChannelHandlerContext channelHandlerContext) {
            String message = new String(authentication.getPayload());
            if ("success".equals(message)) {
                return new AuthenticationResult(true, new byte[0]);
            } else {
                return new AuthenticationResult(false, new byte[0]);
            }
        }

    }

}
