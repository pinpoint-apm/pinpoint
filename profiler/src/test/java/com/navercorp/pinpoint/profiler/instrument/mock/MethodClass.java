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
public class MethodClass {

    void defaultMethod() {
    }

    private void privateMethod() {
    }

    protected void protectedMethod() {
    }

    public void publicMethod() {
    }

    public static void publicStaticMethod() {
    }

    public final void publicFinalMethod() {
    }

    public static final void publicStaticFinalMethod() {
    }

    public synchronized void publicSynchronizedMethod() {
    }

    public static synchronized void publicStaticSynchronizedMethod() {
    }

    public static final synchronized void publicStaticFinalSynchronizedMethod() {
    }

    public native void publicNativeMethod();

    public static native void publicStaticNativeMethod();

    public static final native void publicStaticFinalNativeMethod();

    public static final synchronized native void publicStaticFinalSynchronizedNativeMethod();

    public strictfp void publicMethod1() {
    }

    public strictfp static final void publicStrictfpStaticFinalMethod() {
    }

    public strictfp synchronized void publicStrictfpSynchronizedMethod() {
    }

    public strictfp static synchronized void publicStrictfpStaticSynchronizedMethod() {
    }

    public strictfp static final synchronized void publicStrictfpStaticFinalSynchronizedMethod() {
    }
}