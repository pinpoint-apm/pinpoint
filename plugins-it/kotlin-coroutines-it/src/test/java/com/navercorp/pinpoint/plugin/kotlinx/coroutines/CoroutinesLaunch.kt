/*
 * Copyright 2021 NAVER Corp.
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
import java.util.*

/**
 * @author Taejin Koo
 */
class CoroutinesLaunch {

    fun execute(coroutineName: String) {
        runBlocking(CoroutineName(coroutineName) + Dispatchers.Default) {
            execute0(coroutineName)
        }
    }

    // Concurrently executes both sections
    suspend fun execute0(firstName: String, secondName: String = UUID.randomUUID().toString()) =
        coroutineScope { // this: CoroutineScope
            val job = async(CoroutineName(firstName)) {
                delay(10L)
                println("Hello World 1")
            }
            launch(CoroutineName(secondName)) {
                delay(5L)
                println("Hello World 2")
            }
            job.join()
            println("Hello World")
        }

    fun execute2(coroutineName: String) {
        runBlocking(CoroutineName(coroutineName) + Dispatchers.Default) {
            execute0(coroutineName, coroutineName)
        }
    }

    fun executeParentDispatcher(coroutineName: String) {
        runBlocking(CoroutineName(coroutineName)) {
            execute0(coroutineName)
        }
    }


}