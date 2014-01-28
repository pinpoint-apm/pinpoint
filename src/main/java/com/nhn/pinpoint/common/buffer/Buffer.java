package com.nhn.pinpoint.common.buffer;

/**
 * @author emeroad
 */
public interface Buffer {

    public static final int BOOLEAN_FALSE = 0;
    public static final int BOOLEAN_TRUE = 1;

    public static final byte[] EMPTY = new byte[0];

    public static final String UTF8 = "UTF-8";

    void putPrefixedBytes(byte[] bytes);

    void putPrefixedString(String string);

    void put(byte v);

    void put(boolean v);

    void put(int v);

    /**
     * 가변인코딩을 사용하여 저장한다.
     * 상수값에 강한 인코딩을 한다.
     * 음수값이 들어갈 경우 사이즈가 fixint 인코딩 보다 더 커짐, 음수값의 분포가 많을 경우 매우 비효율적임.
     * 이 경우 putSVar를 사용한다. putSVar에 비해서 zigzag연산이 없어 cpu를 약간 덜사용하는 이점 뿐이 없음.
     * 음수가 조금이라도 들어갈 가능성이 있다면 putSVar를 사용하는 것이 이득이다..
     * 1~10 byte사용
     * max : 5, min 10
     * @param v
     */
    void putVar(int v);

    /**
     * 가변인코딩을 사용하여 저장한다.
     * 상수, 음수의 분포가 동일한 데이터 일 경우 사용한다.
     * 1~5 사용
     * max : 5, min :5
     * @param v
     */
    void putSVar(int v);

    void put(short v);

    void put(long v);

    /**
     * 가변인코딩을 사용하여 저장한다.
     * 상수값에 강한 인코딩을 한다.
     * 음수값이 들어갈 경우 사이즈가 fixint 인코딩 보다 더 커짐
     * 이경우 putSVar를 사용한다.
     * @param v
     */
    void putVar(long v);

    /**
     * 가변인코딩을 사용하여 저장한다.
     * 상수, 음수의 분포가 동일한 데이터 일 경우 사용한다.
     * @param v
     */
    void putSVar(long v);

    void put(byte[] v);

    byte readByte();

    int readUnsignedByte();

    boolean readBoolean();

    int readInt();

    int readVarInt();

    int readSVarInt();


    short readShort();

    long readLong();

    long readVarLong();

    long readSVarLong();

    byte[] readPrefixedBytes();

    String readPrefixedString();

    String read4PrefixedString();

    byte[] getBuffer();

    byte[] getInternalBuffer();

    void setOffset(int offset);

    int getOffset();

    int limit();
}
