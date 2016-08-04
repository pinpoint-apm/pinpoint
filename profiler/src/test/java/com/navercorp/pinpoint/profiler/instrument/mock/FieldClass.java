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
public class FieldClass {

    private byte b;
    private short s;
    private int i;
    private long l;
    private float f;
    private double d;
    private boolean y;
    private char c;

    private byte[] bArray;
    private short[] sArray;
    private int[] iArray;
    private long[] lArray;
    private float[] fArray;
    private double[] dArray;
    private boolean[] yArray;
    private char[] cArray;

    private byte[][] bArrays;
    private short[][] sArrays;
    private int[][] iArrays;
    private long[][] lArrays;
    private float[][] fArrays;
    private double[][] dArrays;
    private boolean[][] yArrays;
    private char[][] cArrays;

    private String str;
    private Object object;
    private Byte bObjct;
    private Short sObject;
    private Integer iObject;
    private Long lObject;
    private Float fObject;
    private Double dObject;
    private Boolean yObject;
    private Character cObject;

    private String[] strArray;
    private Object[] objectArray;
    private Byte[] bObjctArray;
    private Short[] sObjectArray;
    private Integer[] iObjectArray;
    private Long[] lObjectArray;
    private Float[] fObjectArray;
    private Double[] dObjectArray;
    private Boolean[] yObjectArray;
    private Character[] cObjectArray;

    private String[][] strArrays;
    private Object[][] objectArrays;
    private Byte[][] bObjctArrays;
    private Short[][] sObjectArrays;
    private Integer[][] iObjectArrays;
    private Long[][] lObjectArrays;
    private Float[][] fObjectArrays;
    private Double[][] dObjectArrays;
    private Boolean[][] yObjectArrays;
    private Character[][] cObjectArrays;

    private Enum e;
    private Enum[] eArray;
    private Enum[][] eArrays;

    private Map map;
    private Map<String, String> strMap;
    private Map<Object, Object> objectMap;
    private Map<?, ?> wildcardMap;

    private List list;
    private List<String> strList;
    private List<Object> objectList;
    private List<?> wildcardList;

    private Class clazz;
    private Method method;
    private Field field;

    String defaultStr;
    static String defaultStaticStr;
    final String defaultFinalStr = "foo";
    static final String defaultStaticFinalStr = "foo";

    private String privateStr;
    private static String privateStaticStr;
    private final String privateFinalStr = "foo";
    private static final String privateStaticFinalStr = "foo";

    protected String protectedStr;
    protected static String protectedStaticStr;
    protected final String protectedFinalStr = "foo";
    protected static final String protectedStaticFinalStr = "foo";

    public String publicStr;
    public static String publicStaticStr;
    public final String publicFinalStr = "foo";
    public static final String publicStaticFinalStr = "foo";

    private volatile int volatileInt;
    private transient int transientInt;

}