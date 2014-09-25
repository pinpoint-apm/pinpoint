package com.nhn.pinpoint.profiler.javaassist;

import java.util.List;

/**
 * @author emeroad
 */
public class TestClass<T> {
    private int a;
    private List<T> b;
    private String[] stringArray;


    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int inlineClass(final int a) {
        Comparable c = new Comparable() {
            @Override
            public int compareTo(Object o) {
                return a;
            }
        };
        return c.compareTo(null);
    }

    private class InnerClass {
        private String str;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }
    }

    public void setB(List<T> b) {
        this.b = b;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }
}
