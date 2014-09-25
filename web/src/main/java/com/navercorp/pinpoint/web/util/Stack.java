package com.nhn.pinpoint.web.util;

import java.util.LinkedList;

/**
 * @author emeroad
 */
public class Stack<T> {

    private final LinkedList<T> stack = new LinkedList<T>();

    public void push(T obj) {
        if (obj == null) {
            throw new NullPointerException("obj must not be null");
        }

        stack.add(obj);
    }

    public T getLast() {
        return stack.getLast();
    }

    public T pop() {
        return stack.pollLast();
    }

    public T getParent() {
        final int parent = stack.size() - 2;
        if (parent < 0) {
            return null;
        }
        return stack.get(parent);
    }

    @Override
    public String toString() {
        return "Stack{" +
                "stack=" + stack +
                '}';
    }
}
