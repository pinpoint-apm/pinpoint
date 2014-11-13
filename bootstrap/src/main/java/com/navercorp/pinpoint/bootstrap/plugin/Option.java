package com.nhn.pinpoint.bootstrap.plugin;

public abstract class Option<T> {
    public abstract T getValue();
    public abstract boolean hasValue();
    
    public static <U> Option<U> withValue(U value) {
        return new WithValue<U>(value);
    }
    
    @SuppressWarnings("unchecked")
    public static <U> Option<U> empty() {
        return (Option<U>)EMPTY;
    }
    
    private static final class WithValue<T> extends Option<T> {
        private final T value;
        
        private WithValue(T value) {
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean hasValue() {
            return true;
        }
        
    }
 
    private static final Option<Object> EMPTY = new Option<Object>() {

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public boolean hasValue() {
            return false;
        }
    };
    
}
