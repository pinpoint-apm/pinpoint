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

/**
 * @author Jongho Moon
 *
 */
public enum FieldSnooper {
    OBJECT(ObjectSnooper.class),
    OBJECT2(ObjectSnooper2.class),
    OBJECT3(ObjectSnooper3.class),
    INT(IntSnooper.class),
    LONG(LongSnooper.class),
    Double(DoubleSnooper.class),
    BOOLEAN(BooleanSnooper.class);
    
    private final Class<? extends Snooper> type;
    
    private FieldSnooper(Class<? extends Snooper> type) {
        this.type = type;
    }
    
    public Class<? extends Snooper> getType() {
        return type;
    }

    
    public static boolean isInjected(FieldSnooper snooper, Object object) {
        return snooper.type.isAssignableFrom(object.getClass()); 
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get(Object object) {
        return (T)((ObjectSnooper)object)._$PINPOINT$_getObjectField();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get2(Object object) {
        return (T)((ObjectSnooper2)object)._$PINPOINT$_getObjectField2();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T get3(Object object) {
        return (T)((ObjectSnooper3)object)._$PINPOINT$_getObjectField3();
    }
    
    public static boolean getBoolean(Object object) {
        return ((BooleanSnooper)object)._$PINPOINT$_getBooleanField();
    }

    public static int getInt(Object object) {
        return ((IntSnooper)object)._$PINPOINT$_getIntField();
    }

    public static long getLong(Object object) {
        return ((LongSnooper)object)._$PINPOINT$_getLongField();
    }

    public static double getDouble(Object object) {
        return ((DoubleSnooper)object)._$PINPOINT$_getDoubleField();
    }
    
    
    
    
    
    private interface BooleanSnooper extends Snooper {
        public boolean _$PINPOINT$_getBooleanField();
    }
    
    private interface DoubleSnooper extends Snooper {
        public double _$PINPOINT$_getDoubleField();
    }
    
    public interface LongSnooper extends Snooper {
        public long _$PINPOINT$_getLongField();
    }

    public interface IntSnooper extends Snooper {
        public int _$PINPOINT$_getIntField();
    }
    
    public interface ObjectSnooper extends Snooper {
        public Object _$PINPOINT$_getObjectField();
    }
    
    public interface ObjectSnooper2 extends Snooper {
        public Object _$PINPOINT$_getObjectField2();
    }
    
    public interface ObjectSnooper3 extends Snooper {
        public Object _$PINPOINT$_getObjectField3();
    }
}
