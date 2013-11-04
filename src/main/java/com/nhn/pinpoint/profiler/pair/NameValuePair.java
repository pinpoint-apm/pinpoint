package com.nhn.pinpoint.profiler.pair;

/**
 * classLoading구조에서 interceptor가 parent에 위치하면서 멀티 value access 데이터 전달이 필요할 경우의 공통 자료구조로 사용한다.
 * @author emeroad
 */
public class NameValuePair<T, V> {
    private T name;
    private V value;

    public NameValuePair(T name, V value) {
        this.name = name;
        this.value = value;
    }

    public T getName() {
        return name;
    }

    public void setName(T name) {
        this.name = name;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
