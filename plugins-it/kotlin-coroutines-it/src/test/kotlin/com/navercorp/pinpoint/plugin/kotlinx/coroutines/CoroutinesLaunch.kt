/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author Taejin Koo
 */
class CoroutinesLaunch {

    @JvmOverloads
    fun executeWithRunBlocking(context: CoroutineContext = EmptyCoroutineContext) {
        runBlocking(context) {
            val job1 = async(CoroutineName("first")) {
                delay(10L)
                println("Hello World 1")
            }
            val job2 = launch(CoroutineName("second")) {
                delay(5L)
                println("Hello World 2")
            }
            joinAll(job1, job2)
            println("Hello all of jobs")
        }
    }

}