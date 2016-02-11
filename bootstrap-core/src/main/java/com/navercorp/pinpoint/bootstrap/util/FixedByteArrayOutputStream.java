package com.navercorp.pinpoint.bootstrap.util;

import java.io.ByteArrayOutputStream; 

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
