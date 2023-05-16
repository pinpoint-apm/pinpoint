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

package com.navercorp.pinpoint.common.server.cluster.zookeeper.exception;

/**
 * @author koo.taejin
 */
public class PinpointZookeeperException extends Exception {

    public PinpointZookeeperException() {
    }

    public PinpointZookeeperException(String message) {
        super(message);
    }

    public PinpointZookeeperException(String message, Throwable cause) {
        super(message, cause);
    }

    public PinpointZookeeperException(Throwable cause) {
        super(cause);
    }

}
