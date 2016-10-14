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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ConstructorClass {

    public ConstructorClass() {
    }

    public ConstructorClass(byte b) {
    }

    public ConstructorClass(short s) {
    }

    public ConstructorClass(int i) {
    }

    public ConstructorClass(float f) {
    }

    public ConstructorClass(double d) {
    }

    public ConstructorClass(boolean y) {
    }

    public ConstructorClass(char c) {
    }

    public ConstructorClass(byte[] bArray) {
    }

    public ConstructorClass(short[] sArray) {
    }

    public ConstructorClass(int[] iArray) {
    }

    public ConstructorClass(float[] fArray) {
    }

    public ConstructorClass(double[] dArray) {
    }

    public ConstructorClass(boolean[] yArray) {
    }

    public ConstructorClass(char[] cArray) {
    }

    public ConstructorClass(byte[][] bArrays) {
    }

    public ConstructorClass(short[][] sArrays) {
    }

    public ConstructorClass(int[][] iArrays) {
    }

    public ConstructorClass(float[][] fArrays) {
    }

    public ConstructorClass(double[][] dArrays) {
    }

    public ConstructorClass(boolean[][] yArrays) {
    }

    public ConstructorClass(char[][] cArrays) {
    }

    public ConstructorClass(byte b, short s, int i, float f, double d, boolean y, char c, byte[] bArray, short[] sArray, int[] iArray, float[] fArray, double[] dArray, boolean[] yArray, char[] cArray, byte[][] bArrays, short[][] sArrays, int[][] iArrays, float[][] fArrays, double[][] dArrays, boolean[][] yArrays, char[][] cArrays) {
    }

    public ConstructorClass(String str) {
    }

    public ConstructorClass(Object object) {
    }

    public ConstructorClass(Byte bObject) {
    }

    public ConstructorClass(Short sObject) {
    }

    public ConstructorClass(Integer iObject) {
    }

    public ConstructorClass(Long lObject) {
    }

    public ConstructorClass(Float fObject) {
    }

    public ConstructorClass(Double dObject) {
    }

    public ConstructorClass(Boolean yObject) {
    }

    public ConstructorClass(Character cObject) {
    }

    public ConstructorClass(String[] strArray) {
    }

    public ConstructorClass(Object[] objectArray) {
    }

    public ConstructorClass(Byte[] bObjectArray) {
    }

    public ConstructorClass(Short[] sObjectArray) {
    }

    public ConstructorClass(Integer[] iObjectArray) {
    }

    public ConstructorClass(Long[] lObjectArray) {
    }

    public ConstructorClass(Float[] fObjectArray) {
    }

    public ConstructorClass(Double[] dObjectArray) {
    }

    public ConstructorClass(Boolean[] yObjectArray) {
    }

    public ConstructorClass(Character[] cObjectArray) {
    }

    public ConstructorClass(String[][] strArrays) {
    }

    public ConstructorClass(Object[][] objectArrays) {
    }

    public ConstructorClass(Byte[][] bObjectArrays) {
    }

    public ConstructorClass(Short[][] sObjectArrays) {
    }

    public ConstructorClass(Integer[][] iObjectArrays) {
    }

    public ConstructorClass(Long[][] lObjectArrays) {
    }

    public ConstructorClass(Float[][] fObjectArrays) {
    }

    public ConstructorClass(Double[][] dObjectArrays) {
    }

    public ConstructorClass(Boolean[][] yObjectArrays) {
    }

    public ConstructorClass(Character[][] cObjectArrays) {
    }

    public ConstructorClass(String str, Object object, Byte bObject, Short sObject, Integer iObject, Long lObject, Float fObject, Double dObject, Boolean yObject, Character cObject, String[] strArray, Object[] objectArray, Byte[] bObjectArray, Short[] sObjectArray, Integer[] iObjectArray, Long[] lObjectArray, Float[] fObjectArray, Double[] dObjectArray, Boolean[] yObjectArray, Character[] cObjectArray, String[][] strArrays, Object[][] objectArrays, Byte[][] bObjectArrays, Short[][] sObjectArrays, Integer[][] iObjectArrays, Long[][] lObjectArrays, Float[][] fObjectArrays, Double[][] dObjectArrays, Boolean[][] yObjectArrays, Character[][] cObjectArrays) {
    }

    public ConstructorClass(Enum e) {
    }

    public ConstructorClass(Enum[] eArray) {
    }

    public ConstructorClass(Enum[][] eArrays) {
    }

    public ConstructorClass(Map map, Map<String, String> strMap, Map<Object, Object> objectMap, Map<?, ?> wildcardMap) {
    }

    public ConstructorClass(List list, List<String> strList, List<Object> objectList, List<?> wildcardList) {
    }

    public ConstructorClass(Class clazz, Method method, Field field) {
    }

    private ConstructorClass(String s, int i) {
    }

    protected ConstructorClass(String s, int i, byte b) {
    }

    ConstructorClass(String s, int i, byte b, Object o) {
    }

    @Deprecated
    ConstructorClass(String s, int i, byte b, Object o, Enum e) {
    }


    ConstructorClass(String s, int i, byte b, Object o, Enum e, char c) {
        this(s, i, b, o, e, c, 1.1f);
    }

    ConstructorClass(String s, int i, byte b, Object o, Enum e, char c, float f) {
    }

    public ConstructorClass(String s, int i, byte b, Object o, Enum e, char c, float f, long l) {
        super();
    }
}