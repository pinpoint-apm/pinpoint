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
@SuppressWarnings("unchecked")
public abstract class FieldSnooper {
    private static final FieldSnooper[] VALUES = {
        new FieldSnooper(ObjectSnooper0.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper0)object)._$PINPOINT$_getObjectField0();
            }
        },
        new FieldSnooper(ObjectSnooper1.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper1)object)._$PINPOINT$_getObjectField1();
            }
        },
        new FieldSnooper(ObjectSnooper2.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper2)object)._$PINPOINT$_getObjectField2();
            }
        },
        new FieldSnooper(ObjectSnooper3.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper3)object)._$PINPOINT$_getObjectField3();
            }
        },
        new FieldSnooper(ObjectSnooper4.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper4)object)._$PINPOINT$_getObjectField4();
            }
        },
        new FieldSnooper(ObjectSnooper5.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper5)object)._$PINPOINT$_getObjectField5();
            }
        },
        new FieldSnooper(ObjectSnooper6.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper6)object)._$PINPOINT$_getObjectField6();
            }
        },
        new FieldSnooper(ObjectSnooper7.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper7)object)._$PINPOINT$_getObjectField7();
            }
        },
        new FieldSnooper(ObjectSnooper8.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper8)object)._$PINPOINT$_getObjectField8();
            }
        },
        new FieldSnooper(ObjectSnooper9.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper9)object)._$PINPOINT$_getObjectField9();
            }
        }        
    };
    
    private final Class<? extends Snooper> type;
    
    private FieldSnooper(Class<? extends Snooper> type) {
        this.type = type;
    }
    
    public Class<? extends Snooper> getType() {
        return type;
    }

    public boolean isApplicable(Object object) {
        return type.isAssignableFrom(object.getClass());
    }
    
    public abstract <T> T get(Object object);

    
    
    
    
    public static FieldSnooper get(int index) {
        return VALUES[index];
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
    
    
    public interface ObjectSnooper0 extends Snooper {
        public Object _$PINPOINT$_getObjectField0();
    }
    
    public interface ObjectSnooper1 extends Snooper {
        public Object _$PINPOINT$_getObjectField1();
    }

    public interface ObjectSnooper2 extends Snooper {
        public Object _$PINPOINT$_getObjectField2();
    }
    
    public interface ObjectSnooper3 extends Snooper {
        public Object _$PINPOINT$_getObjectField3();
    }
    
    public interface ObjectSnooper4 extends Snooper {
        public Object _$PINPOINT$_getObjectField4();
    }

    public interface ObjectSnooper5 extends Snooper {
        public Object _$PINPOINT$_getObjectField5();
    }

    public interface ObjectSnooper6 extends Snooper {
        public Object _$PINPOINT$_getObjectField6();
    }

    public interface ObjectSnooper7 extends Snooper {
        public Object _$PINPOINT$_getObjectField7();
    }

    public interface ObjectSnooper8 extends Snooper {
        public Object _$PINPOINT$_getObjectField8();
    }

    public interface ObjectSnooper9 extends Snooper {
        public Object _$PINPOINT$_getObjectField9();
    }

}
