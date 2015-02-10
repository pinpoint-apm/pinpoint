/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

/**
 * @author Jongho Moon
 *
 */
public enum PrimitiveMetadata {
    BOOLEAN(BooleanAccessor.class),
    CHAR(CharAccessor.class),
    SHORT(ShortAccessor.class),
    INT(IntAccessor.class),
    LONG(LongAccessor.class),
    FLOAT(FloatAccessor.class),
    DOUBLE(DoubleAccessor.class);
    
    private final Class<? extends TraceValue> type;
    
    private PrimitiveMetadata(Class<? extends TraceValue> type) {
        this.type = type;
    }
    
    public Class<? extends TraceValue> getType() {
        return type;
    }
    
    public boolean isInstance(Object object) {
        return type.isAssignableFrom(object.getClass());
    }
    
    public static boolean getBoolean(Object object) {
        return ((BooleanAccessor)object)._$PINPOINT$_getBoolean();
    }
    
    public static char getChar(Object object) {
        return ((CharAccessor)object)._$PINPOINT$_getChar();
    }
    
    public static short getShort(Object object) {
        return ((ShortAccessor)object)._$PINPOINT$_getShort();
    }

    public static int getInt(Object object) {
        return ((IntAccessor)object)._$PINPOINT$_getInt();
    }

    public static long getLong(Object object) {
        return ((LongAccessor)object)._$PINPOINT$_getLong();
    }
    
    public static float getFloat(Object object) {
        return ((FloatAccessor)object)._$PINPOINT$_getFloat();
    }
    
    public static double getDouble(Object object) {
        return ((DoubleAccessor)object)._$PINPOINT$_getDouble();
    }
    
    public static void setBoolean(Object object, boolean value) {
        ((BooleanAccessor)object)._$PINPOINT$_setBoolean(value);
    }

    public static void setChar(Object object, char value) {
        ((CharAccessor)object)._$PINPOINT$_setChar(value);
    }

    public static void setShort(Object object, short value) {
        ((ShortAccessor)object)._$PINPOINT$_setShort(value);
    }
    
    public static void setInt(Object object, int value) {
        ((IntAccessor)object)._$PINPOINT$_setInt(value);
    }
    
    public static void setLong(Object object, long value) {
        ((LongAccessor)object)._$PINPOINT$_setLong(value);
    }
    
    public static void setFloat(Object object, float value) {
        ((FloatAccessor)object)._$PINPOINT$_setFloat(value);
    }
    
    public static void setDouble(Object object, double value) {
        ((DoubleAccessor)object)._$PINPOINT$_setDouble(value);
    }
    
    
    
    public interface BooleanAccessor extends TraceValue {
        public boolean _$PINPOINT$_getBoolean();
        public void _$PINPOINT$_setBoolean(boolean value);
    }

    public interface CharAccessor extends TraceValue {
        public char _$PINPOINT$_getChar();
        public void _$PINPOINT$_setChar(char value);
    }
    
    public interface ShortAccessor extends TraceValue {
        public short _$PINPOINT$_getShort();
        public void _$PINPOINT$_setShort(short value);
    }

    public interface IntAccessor extends TraceValue {
        public int _$PINPOINT$_getInt();
        public void _$PINPOINT$_setInt(int value);
    }
    
    public interface LongAccessor extends TraceValue {
        public long _$PINPOINT$_getLong();
        public void _$PINPOINT$_setLong(long value);
    }

    public interface FloatAccessor extends TraceValue {
        public float _$PINPOINT$_getFloat();
        public void _$PINPOINT$_setFloat(float value);
    }
    
    public interface DoubleAccessor extends TraceValue {
        public double _$PINPOINT$_getDouble();
        public void _$PINPOINT$_setDouble(double value);
    }
    
}
