package com.nhn.pinpoint.javaassist;

public class TestClass {
    private int a;

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
}
