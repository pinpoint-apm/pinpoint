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
public class DelegatorSuperClass {

    public void publicArgByteReturnVoid(byte b) {
    }

    public void publicArgByteArrayReturnVoid(byte[] bArray) {
    }

    public void publicArgByteArraysReturnVoid(byte[][] bArrays) {
    }

    public void publicArgComplexReturnVoid(int i, float f, double d, boolean y) {
    }

    public void publicArgsReturnVoid(Object... args) {
    }

    public void publicArgInterfaceReturnVoid(Map map, Map<String, String> strMap, Map<Object, Object> objectMap) {
    }

    public void publicArgEnumReturnVoid(Enum e) {
    }

    public String publicArgStringReturnString(String a) {
        return "publicArgStringReturnString";
    }

    public String[] publicArgStringReturnStringArray(String a, String b) {
        String[] result = new String[2];
        result[0] = a;
        result[1] = b;

        return result;
    }

    public String[][] publicArgStringReturnStringArrays(String a, String b, String c) {
        String[][] result = new String[1][3];
        result[0][0] = a;
        result[0][1] = b;
        result[0][2] = c;

        return result;
    }
}