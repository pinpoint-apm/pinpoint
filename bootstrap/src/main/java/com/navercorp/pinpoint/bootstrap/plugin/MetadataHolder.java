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
public enum MetadataHolder {
    OBJECT(ObjectAccessor.class),
    OBJECT2(ObjectAccessor2.class),
    OBJECT3(ObjectAccessor3.class),
    BOOLEAN(BooleanAccessor.class),
    INT(IntAccessor.class),
    LONG(LongAccessor.class),
    DOUBLE(DoubleAccessor.class);
    
    private final Class<? extends TraceValue> type;
    
    private MetadataHolder(Class<? extends TraceValue> type) {
        this.type = type;
    }
    
    public Class<? extends TraceValue> getType() {
        return type;
    }

    
    public static boolean isInjected(MetadataHolder accessor, Object object) {
        return accessor.type.isAssignableFrom(object.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object object) {
        return (T)((ObjectAccessor)object)._$PINPOINT$_getObject();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get2(Object object) {
        return (T)((ObjectAccessor2)object)._$PINPOINT$_getObject2();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get3(Object object) {
        return (T)((ObjectAccessor3)object)._$PINPOINT$_getObject3();
    }
    
    public static boolean getBoolean(Object object) {
        return ((BooleanAccessor)object)._$PINPOINT$_getBoolean();
    }

    public static int getInt(Object object) {
        return ((IntAccessor)object)._$PINPOINT$_getInt();
    }

    public static long getLong(Object object) {
        return ((LongAccessor)object)._$PINPOINT$_getLong();
    }

    public static double getDouble(Object object) {
        return ((DoubleAccessor)object)._$PINPOINT$_getDouble();
    }

    public static void set(Object object, Object value) {
        ((ObjectAccessor)object)._$PINPOINT$_setObject(value);
    }

    public static void set2(Object object, Object value) {
        ((ObjectAccessor2)object)._$PINPOINT$_setObject2(value);
    }

    public static void set3(Object object, Object value) {
        ((ObjectAccessor3)object)._$PINPOINT$_setObject3(value);
    }
    
    public static void setBoolean(Object object, boolean value) {
        ((BooleanAccessor)object)._$PINPOINT$_setBoolean(value);
    }
    
    public static void setInt(Object object, int value) {
        ((IntAccessor)object)._$PINPOINT$_setInt(value);
    }
    
    public static void setLong(Object object, long value) {
        ((LongAccessor)object)._$PINPOINT$_setLong(value);
    }
    
    public static void setDouble(Object object, double value) {
        ((DoubleAccessor)object)._$PINPOINT$_setDouble(value);
    }
    
    
    
    private interface BooleanAccessor extends TraceValue {
        public boolean _$PINPOINT$_getBoolean();
        public void _$PINPOINT$_setBoolean(boolean value);
    }

    private interface DoubleAccessor extends TraceValue {
        public double _$PINPOINT$_getDouble();
        public void _$PINPOINT$_setDouble(double value);
    }
    
    private interface IntAccessor extends TraceValue {
        public int _$PINPOINT$_getInt();
        public void _$PINPOINT$_setInt(int value);
    }
    
    private interface LongAccessor extends TraceValue {
        public long _$PINPOINT$_getLong();
        public void _$PINPOINT$_setLong(long value);
    }

    private interface ObjectAccessor extends TraceValue {
        public Object _$PINPOINT$_getObject();
        public void _$PINPOINT$_setObject(Object value);
    }
    
    private interface ObjectAccessor2 extends TraceValue {
        public Object _$PINPOINT$_getObject2();
        public void _$PINPOINT$_setObject2(Object value);
    }

    private interface ObjectAccessor3 extends TraceValue {
        public Object _$PINPOINT$_getObject3();
        public void _$PINPOINT$_setObject3(Object value);
    }
}
