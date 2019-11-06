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

package com.navercorp.pinpoint.profiler.context;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DepthCompressCallStack<T> extends DefaultCallStack<T> {

    private int latestStackIndex = 0;

    public DepthCompressCallStack(Factory<T> factory) {
        this(factory, -1);
    }
    
    public DepthCompressCallStack(Factory<T> factory, int maxDepth) {
        super(factory, maxDepth);
    }

    @Override
    protected void markDepth(T element, int depth) {
        // compact same depth
        if (latestStackIndex != index) {
            latestStackIndex = index;
            this.factory.markDepth(element, latestStackIndex);
        }
    }



}