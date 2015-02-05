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
@SuppressWarnings("unchecked")
public abstract class MetadataAccessor {
    private static final MetadataAccessor[] VALUES = {
        new MetadataAccessor(ObjectAccessor0.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor0)object)._$PINPOINT$_getObject0();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor0)object)._$PINPOINT$_setObject0(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor1.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor1)object)._$PINPOINT$_getObject1();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor1)object)._$PINPOINT$_setObject1(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor2.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor2)object)._$PINPOINT$_getObject2();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor2)object)._$PINPOINT$_setObject2(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor3.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor3)object)._$PINPOINT$_getObject3();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor3)object)._$PINPOINT$_setObject3(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor4.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor4)object)._$PINPOINT$_getObject4();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor4)object)._$PINPOINT$_setObject4(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor5.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor5)object)._$PINPOINT$_getObject5();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor5)object)._$PINPOINT$_setObject5(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor6.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor6)object)._$PINPOINT$_getObject6();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor6)object)._$PINPOINT$_setObject6(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor7.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor7)object)._$PINPOINT$_getObject7();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor7)object)._$PINPOINT$_setObject7(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor8.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor8)object)._$PINPOINT$_getObject8();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor8)object)._$PINPOINT$_setObject8(value);
            }
        },
        
        new MetadataAccessor(ObjectAccessor9.class) {
            @Override
            public <T> T get(Object object) {
                return (T)((ObjectAccessor9)object)._$PINPOINT$_getObject9();
            }
    
            @Override
            public <T> void set(Object object, T value) {
                ((ObjectAccessor9)object)._$PINPOINT$_setObject9(value);
            }
        }
    };
    
    
    private final Class<? extends TraceValue> type;
    
    private MetadataAccessor(Class<? extends TraceValue> type) {
        this.type = type;
    }
    
    public Class<? extends TraceValue> getType() {
        return type;
    }
    
    public boolean isApplicable(Object object) {
        return type.isAssignableFrom(object.getClass());
    }
    
    public static MetadataAccessor get(int index) {
        return VALUES[index];
    }
    
    public abstract <T> T get(Object object);
    public abstract <T> void set(Object object, T value);
   
    public interface ObjectAccessor extends TraceValue {
        public Object _$PINPOINT$_getObject();
        public void _$PINPOINT$_setObject(Object value);
    }
    
    public interface ObjectAccessor0 extends TraceValue {
        public Object _$PINPOINT$_getObject0();
        public void _$PINPOINT$_setObject0(Object value);
    }
    
    public interface ObjectAccessor1 extends TraceValue {
        public Object _$PINPOINT$_getObject1();
        public void _$PINPOINT$_setObject1(Object value);
    }
    
    public interface ObjectAccessor2 extends TraceValue {
        public Object _$PINPOINT$_getObject2();
        public void _$PINPOINT$_setObject2(Object value);
    }

    public interface ObjectAccessor3 extends TraceValue {
        public Object _$PINPOINT$_getObject3();
        public void _$PINPOINT$_setObject3(Object value);
    }
    
    public interface ObjectAccessor4 extends TraceValue {
        public Object _$PINPOINT$_getObject4();
        public void _$PINPOINT$_setObject4(Object value);
    }

    public interface ObjectAccessor5 extends TraceValue {
        public Object _$PINPOINT$_getObject5();
        public void _$PINPOINT$_setObject5(Object value);
    }

    public interface ObjectAccessor6 extends TraceValue {
        public Object _$PINPOINT$_getObject6();
        public void _$PINPOINT$_setObject6(Object value);
    }

    public interface ObjectAccessor7 extends TraceValue {
        public Object _$PINPOINT$_getObject7();
        public void _$PINPOINT$_setObject7(Object value);
    }

    public interface ObjectAccessor8 extends TraceValue {
        public Object _$PINPOINT$_getObject8();
        public void _$PINPOINT$_setObject8(Object value);
    }

    public interface ObjectAccessor9 extends TraceValue {
        public Object _$PINPOINT$_getObject9();
        public void _$PINPOINT$_setObject9(Object value);
    }
}
