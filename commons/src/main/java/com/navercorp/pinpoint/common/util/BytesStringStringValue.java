package com.navercorp.pinpoint.common.util;

import java.util.Arrays;
import java.util.Objects;

public class BytesStringStringValue implements DataType {
    private final byte[] bytesValue;
    private final String stringValue1;
    private final String stringValue2;

    public BytesStringStringValue(byte[] bytesValue, String stringValue1, String stringValue2) {
        this.bytesValue = bytesValue;
        this.stringValue1 = stringValue1;
        this.stringValue2 = stringValue2;
    }

    public byte[] getBytesValue() {
        return bytesValue;
    }

    public String getStringValue1() {
        return stringValue1;
    }

    public String getStringValue2() {
        return stringValue2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BytesStringStringValue that = (BytesStringStringValue) o;
        return Arrays.equals(bytesValue, that.bytesValue)
                && Objects.equals(stringValue1, that.stringValue1)
                && Objects.equals(stringValue2, that.stringValue2);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(stringValue1, stringValue2);
        result = 31 * result + Arrays.hashCode(bytesValue);
        return result;
    }

    @Override
    public String toString() {
        return "BytesStringStringValue{" +
                "bytesValue=" + Arrays.toString(bytesValue) +
                ", stringValue1='" + stringValue1 + '\'' +
                ", stringValue2='" + stringValue2 + '\'' +
                '}';
    }
}
