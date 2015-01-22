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
public class PluginUtils {
    @SuppressWarnings("unchecked")
    public static <T> T getMetadata(Object object) {
        return (T)((ObjectAccessor)object)._$PINPOINT$_getObject();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getMetadata2(Object object) {
        return (T)((ObjectAccessor2)object)._$PINPOINT$_getObject2();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getMetadata3(Object object) {
        return (T)((ObjectAccessor3)object)._$PINPOINT$_getObject3();
    }
    
    public static boolean getBooleanMetadata(Object object) {
        return ((BooleanAccessor)object)._$PINPOINT$_getBoolean();
    }

    public static int getIntMetadata(Object object) {
        return ((IntAccessor)object)._$PINPOINT$_getInt();
    }

    public static long getLongMetadata(Object object) {
        return ((LongAccessor)object)._$PINPOINT$_getLong();
    }

    public static double getDoubleMetadata(Object object) {
        return ((DoubleAccessor)object)._$PINPOINT$_getDouble();
    }

    public static void setMetadata(Object object, Object data) {
        ((ObjectAccessor)object)._$PINPOINT$_setObject(data);
    }

    public static void setMetadata2(Object object, Object data) {
        ((ObjectAccessor2)object)._$PINPOINT$_setObject2(data);
    }

    public static void setMetadata3(Object object, Object data) {
        ((ObjectAccessor3)object)._$PINPOINT$_setObject3(data);
    }

    
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object object) {
        return (T)((ObjectSnooper)object)._$PINPOINT$_getObject();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getField2(Object object) {
        return (T)((ObjectSnooper2)object)._$PINPOINT$_getObject2();
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getField3(Object object) {
        return (T)((ObjectSnooper3)object)._$PINPOINT$_getObject3();
    }
    public static boolean getBooleanField(Object object) {
        return ((BooleanSnooper)object)._$PINPOINT$_getBoolean();
    }

    public static int getIntField(Object object) {
        return ((IntSnooper)object)._$PINPOINT$_getInt();
    }

    public static long getLongField(Object object) {
        return ((LongSnooper)object)._$PINPOINT$_getLong();
    }

    public static double getDoubleField(Object object) {
        return ((DoubleSnooper)object)._$PINPOINT$_getDouble();
    }
}
