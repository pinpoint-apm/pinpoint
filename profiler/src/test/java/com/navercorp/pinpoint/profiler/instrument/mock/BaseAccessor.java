/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.mock;

import java.util.Map;

/**
 * @author jaehong.kim
 */
public interface BaseAccessor {
    void _$PINPOINT$_setTraceInt(int value);
    int _$PINPOINT$_getTraceInt();

    void _$PINPOINT$_setTraceIntArray(int[] iArray);
    int[] _$PINPOINT$_getTraceIntArray();

    void _$PINPOINT$_setTraceIntArrays(int[][] iArrays);
    int[][] _$PINPOINT$_getTraceIntArrays();

    void _$PINPOINT$_setTraceObject(Object object);
    Object _$PINPOINT$_getTraceObject();

    void _$PINPOINT$_setTraceObjectArray(Object[] objectArray);
    Object[] _$PINPOINT$_getTraceObjectArray();

    void _$PINPOINT$_setTraceObjectArrays(Object[][] objectArrays);
    Object[][] _$PINPOINT$_getTraceObjectArrays();

    void _$PINPOINT$_setTraceEnum(Enum e);
    Enum _$PINPOINT$_getTraceEnum();

    void _$PINPOINT$_setTraceMap(Map map);
    Map _$PINPOINT$_getTraceMap();

    void _$PINPOINT$_setTraceStrMap(Map<String, String> map);
    Map<String, String> _$PINPOINT$_getTraceStrMap();

    void _$PINPOINT$_setTraceObjectMap(Map<Object, Object> map);
    Map<Object, Object> _$PINPOINT$_getTraceObjectMap();

    void _$PINPOINT$_setTraceWildcardMap(Map<?, ?> map);
    Map<?, ?> _$PINPOINT$_getTraceWildcardMap();

    void _$PINPOINT$_setTraceClass(Class clazz);
    Class _$PINPOINT$_getTraceClass();

    void _$PINPOINT$_setTraceDefaultStr(String defaultStr);
    String _$PINPOINT$_getTraceDefaultStr();

    void _$PINPOINT$_setTraceDefaultStaticStr(String defaultStaticStr);
    String _$PINPOINT$_getTraceDefaultStaticStr();

    void _$PINPOINT$_setTraceDefaultStaticFinalStr(String defaultStaticFinalStr);
    String _$PINPOINT$_getTraceDefaultStaticFinalStr();

    void _$PINPOINT$_setTracePrivateStr(String privateStr);
    String _$PINPOINT$_getTracePrivateStr();

    void _$PINPOINT$_setTracePrivateStaticStr(String privateStaticStr);
    String _$PINPOINT$_getTracePrivateStaticStr();

    void _$PINPOINT$_setTracePrivateStaticFinalStr(String privateStaticFinalStr);
    String _$PINPOINT$_getTracePrivateStaticFinalStr();

    void _$PINPOINT$_setTraceProtectedStr(String protectedStr);
    String _$PINPOINT$_getTraceProtectedStr();

    void _$PINPOINT$_setTraceProtectedStaticStr(String protectedStaticStr);
    String _$PINPOINT$_getTraceProtectedStaticStr();

    void _$PINPOINT$_setTraceProtectedStaticFinalStr(String protectedStaticFinalStr);
    String _$PINPOINT$_getTraceProtectedStaticFinalStr();

    void _$PINPOINT$_setTracePublicStr(String publicStr);
    String _$PINPOINT$_getTracePublicStr();

    void _$PINPOINT$_setTracePublicStaticStr(String publicStaticStr);
    String _$PINPOINT$_getTracePublicStaticStr();

    void _$PINPOINT$_setTracePublicStaticFinalStr(String publicStaticFinalStr);
    String _$PINPOINT$_getTracePublicStaticFinalStr();

    void _$PINPOINT$_setTraceVolatileInt(int volatileInt);
    int _$PINPOINT$_getTraceVolatileInt();

    void _$PINPOINT$_setTraceTransientInt(int transientInt);
    int _$PINPOINT$_getTraceTransientInt();
}