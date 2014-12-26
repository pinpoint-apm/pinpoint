/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster.zookeeper.exception;

/**
 * @author koo.taejin
 */
public class NoNodeException extends PinpointZookeeperException {

    public NoNodeException() {
    }

    public NoNodeException(String message) {
        super(message);
    }

    public NoNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoNodeException(Throwable cause) {
        super(cause);
    } 

}
