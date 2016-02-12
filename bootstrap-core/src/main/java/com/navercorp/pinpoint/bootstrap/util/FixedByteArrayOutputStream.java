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

package com.navercorp.pinpoint.bootstrap.util;

import java.io.ByteArrayOutputStream; 

/**
 * @author minwoo.jung
 */
public class FixedByteArrayOutputStream extends ByteArrayOutputStream { 
 
    
    public FixedByteArrayOutputStream(int size) { 
        super(size); 
    } 
 
    public void write(int b) { 
        if (count+1 > buf.length) { 
            return; 
        } 
        super.write(b); 
    } 
 
    public void write(byte b[], int off, int len) {
        if (count+len > buf.length) { 
            if (count  >= buf.length) {
                return;
            }
            super.write(b, off, buf.length - count);
        } else {
            super.write(b, off, len); 
        }
    } 
}
