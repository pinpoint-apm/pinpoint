package com.nhn.pinpoint.web.service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;

import java.util.LinkedList;

/**
 *
 */
public class CallStack {
    private final LinkedList<Depth> stack = new LinkedList<Depth>();

    public static class Depth {
        private SpanAlign spanAlign;
        private int id;

        public Depth(SpanAlign spanAlign, int id) {
            this.spanAlign = spanAlign;
            this.id = id;
        }

        public SpanAlign getSpanAlign() {
            return spanAlign;
        }

        public void setSpanAlign(SpanAlign spanAlign) {
            this.spanAlign = spanAlign;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public void push(Depth depth) {
        stack.add(depth);
    }

    public Depth getLast() {
        return stack.getLast();
    }

    public Depth pop() {
        return stack.pollLast();
    }

    public Depth getParent(){
        int parent = stack.size() - 2;
        if (parent < 0) {
            return null;
        }
        return stack.get(stack.size()-2);
    }


}
