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

package com.navercorp.pinpoint.thrift.io;

import java.io.ByteArrayOutputStream;

/**
 * @author Taejin Koo
 */
public class ResettableByteArrayOutputStream extends ByteArrayOutputStream {

    public ResettableByteArrayOutputStream(int size) {
        super(size);
    }
    
    public void reset(int resetIndex) {
        int bufferLength = buf.length;
        if (bufferLength < resetIndex) {
            throw new IllegalArgumentException("PushbackByteArrayOutputStream reset fail. current buffer length:" + bufferLength + ", resetIndex:" + resetIndex);
        }
        
        this.count = resetIndex;
    }
    
}
