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
package com.navercorp.pinpoint.bootstrap;

/**
 * @author Jongho Moon
 *
 */
@SuppressWarnings("unchecked")
public abstract class FieldAccessor {
    private static final FieldAccessor[] VALUES = {
        new FieldAccessor(ObjectSnooper0.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper0)object)._$PINPOINT$_getObjectField0();
            }
        },
        new FieldAccessor(ObjectSnooper1.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper1)object)._$PINPOINT$_getObjectField1();
            }
        },
        new FieldAccessor(ObjectSnooper2.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper2)object)._$PINPOINT$_getObjectField2();
            }
        },
        new FieldAccessor(ObjectSnooper3.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper3)object)._$PINPOINT$_getObjectField3();
            }
        },
        new FieldAccessor(ObjectSnooper4.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper4)object)._$PINPOINT$_getObjectField4();
            }
        },
        new FieldAccessor(ObjectSnooper5.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper5)object)._$PINPOINT$_getObjectField5();
            }
        },
        new FieldAccessor(ObjectSnooper6.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper6)object)._$PINPOINT$_getObjectField6();
            }
        },
        new FieldAccessor(ObjectSnooper7.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper7)object)._$PINPOINT$_getObjectField7();
            }
        },
        new FieldAccessor(ObjectSnooper8.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper8)object)._$PINPOINT$_getObjectField8();
            }
        },
        new FieldAccessor(ObjectSnooper9.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectSnooper9)object)._$PINPOINT$_getObjectField9();
            }
        }        
    };
    
    private final Class<?> type;
    
    private FieldAccessor(Class<?> type) {
        this.type = type;
    }
    
    public Class<?> getType() {
        return type;
    }

    public boolean isApplicable(Object object) {
        return type.isAssignableFrom(object.getClass());
    }
    
    public abstract <T> T get(Object object);

    
    public static FieldAccessor get(int index) {
        return VALUES[index];
    }
    
    public interface ObjectSnooper0 {
        public Object _$PINPOINT$_getObjectField0();
    }
    
    public interface ObjectSnooper1 {
        public Object _$PINPOINT$_getObjectField1();
    }

    public interface ObjectSnooper2 {
        public Object _$PINPOINT$_getObjectField2();
    }
    
    public interface ObjectSnooper3 {
        public Object _$PINPOINT$_getObjectField3();
    }
    
    public interface ObjectSnooper4 {
        public Object _$PINPOINT$_getObjectField4();
    }

    public interface ObjectSnooper5 {
        public Object _$PINPOINT$_getObjectField5();
    }

    public interface ObjectSnooper6 {
        public Object _$PINPOINT$_getObjectField6();
    }

    public interface ObjectSnooper7 {
        public Object _$PINPOINT$_getObjectField7();
    }

    public interface ObjectSnooper8 {
        public Object _$PINPOINT$_getObjectField8();
    }

    public interface ObjectSnooper9 {
        public Object _$PINPOINT$_getObjectField9();
    }

}
