/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.mock;

/**
 * @author jaehong.kim
 */
public class NormalClass {

    private String s;

    public static String staticCall() {
        return "staticCall";
    }

    public NormalClass() {
        this("foo");
    }

    public NormalClass(String s) {
        this.s = s;
    }

    public void loop() {
        for(int i = 0; i < 100; i++) {
        }
    }


    public int sum(int i) {
        if(i <= 0) {
            return i;
        }

        return i + sum(i - 1);
    }

    public void print(final String s) {
        System.out.println(s);
    }

    public void innerCall() {

    }

    public void call() {
        staticCall();
        innerCall();
    }

    public void dynamicCall() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };
        runnable.run();
    }
}
