package com.navercorp.pinpoint.profiler.instrument.mock;


import com.navercorp.pinpoint.profiler.instrument.mock.BaseEnum;

/**
 * @author jaehong.kim
 */
public class ReturnClass {

    public void voidType() {
    }

    public byte returnByte() {
        return Byte.parseByte("0");
    }

    public Byte returnByteObject() {
        return Byte.valueOf("1");
    }

    public int returnInt() {
        return Integer.parseInt("1");
    }

    public Integer returnIntObject() {
        return Integer.valueOf("1");
    }

    public float returnFloat() {
        return Float.parseFloat("1.1");
    }

    public Float returnFloatObject() {
        return Float.valueOf("1.1");
    }

    public boolean returnBoolean() {
        return Boolean.parseBoolean("true");
    }

    public Boolean returnBooleanObject() {
        return Boolean.valueOf("true");
    }

    public char returnChar() {
        return Character.forDigit(1, 1);
    }

    public Character returnCharObject() {
        return Character.valueOf('1');
    }

    public String returnString() {
        return new String("s");
    }

    public Enum returnEnum() {
        return BaseEnum.AGENT;
    }

    public byte[] returnByteArray() {
        return new byte[0];
    }

    public Byte[] returnByteObjectArray() {
        return new Byte[0];
    }

    public int[] returnIntArray() {
        return new int[0];
    }

    public Integer[] returnIntObjectArray() {
        return new Integer[0];
    }

    public float[] returnFloatArray() {
        return new float[0];
    }

    public Float[] returnFloatObjectArray() {
        return new Float[0];
    }

    public boolean[] returnBooleanArray() {
        return new boolean[0];
    }

    public Boolean[] returnBooleanObjectArray() {
        return new Boolean[0];
    }

    public char[] returnCharArray() {
        return new char[0];
    }

    public Character[] returnCharObjectArray() {
        return new Character[0];
    }

    public String[] returnStringArray() {
        return new String[0];
    }

    public Enum[] returnEnumArray() {
        return new Enum[0];
    }

    public byte[][] returnByteArrays() {
        return new byte[0][];
    }

    public Byte[][] returnByteObjectArrays() {
        return new Byte[0][];
    }

    public int[][] returnIntArrays() {
        return new int[0][];
    }

    public Integer[][] returnIntObjectArrays() {
        return new Integer[0][];
    }

    public float[][] returnFloatArrays() {
        return new float[0][];
    }

    public Float[][] returnFloatObjectArrays() {
        return new Float[0][];
    }

    public boolean[][] returnBooleanArrays() {
        return new boolean[0][];
    }

    public Boolean[][] returnBooleanObjectArrays() {
        return new Boolean[0][];
    }

    public char[][] returnCharArrays() {
        return new char[0][];
    }

    public Character[][] returnCharObjectArrays() {
        return new Character[0][];
    }

    public String[][] returnStringArrays() {
        return new String[0][];
    }

    public Enum[][] returnEnumArrays() {
        return new Enum[0][];
    }

}
