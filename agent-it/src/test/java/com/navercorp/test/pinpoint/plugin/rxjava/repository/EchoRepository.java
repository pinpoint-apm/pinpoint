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

package com.navercorp.test.pinpoint.plugin.rxjava.repository;

/**
 * @author HyunGil Jeong
 */
public class EchoRepository {

    public String echo(String message) {
        System.out.println("echo : " + message);
        return message;
    }

    public String echo(String message, Exception exception) throws Exception {
        if (exception == null) {
            throw new NullPointerException("exception");
        }
        System.out.println("echo : " + message + ", with exception : " + exception);
        throw exception;
    }

    public void shout(String message) {
        System.out.println("shout : " + message);
    }

    public void shout(String message, Exception exception) throws Exception {
        if (exception == null) {
            throw new NullPointerException("exception");
        }
        System.out.println("shout : " + message + ", with exception : " + exception);
        throw exception;
    }
}
