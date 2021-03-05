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

package com.navercorp.pinpoint.bootstrap;

/**
 * @author emeroad
 * @author hyungil.jeong
 */
public interface Agent {

    /**
     * // caution
     * stop is registered together to run when exit the jvm.
     * If inherit this method, should implement it to shut down automatically
     */
    void start();

    void stop();

    void registerStopHandler();
}
