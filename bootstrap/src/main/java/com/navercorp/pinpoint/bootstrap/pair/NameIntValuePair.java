package com.nhn.pinpoint.bootstrap.pair;

/**
 * classLoading구조에서 interceptor가 parent에 위치하면서 멀티 value access 데이터 전달이 필요할 경우의 공통 자료구조로 사용한다.
 * value가 int type일때 사용
 * @author emeroad
 */
public class NameIntValuePair<T> {
    private T name;
    private int value;

    public NameIntValuePair(T name, int value) {
        this.name = name;
        this.value = value;
    }

    public T getName() {
        return name;
    }

    public void setName(T name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
