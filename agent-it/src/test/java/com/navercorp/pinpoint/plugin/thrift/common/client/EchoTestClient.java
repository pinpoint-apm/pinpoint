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

package com.navercorp.pinpoint.plugin.thrift.common.client;

import org.apache.thrift.TException;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;

/**
 * @author HyunGil Jeong
 */
public interface EchoTestClient {

    public String echo(String message) throws TException;

    public void verifyTraces(PluginTestVerifier verifier, String expectedMessage) throws Exception;

    public void close();

}
