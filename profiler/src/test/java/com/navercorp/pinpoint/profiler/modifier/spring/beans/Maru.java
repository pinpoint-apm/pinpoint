package com.nhn.pinpoint.profiler.modifier.spring.beans;

public class Maru implements Comparable<Maru> {
    private static final String staticField;
    private final String field;
    
    static {
        staticField = System.getProperty("no-such-property", "default value");
    }
    
    public Maru(String field) {
        this.field = field;
    }
    
    public Maru() {
        this.field = null;
    }
    

    private void privateMethod() {
        
    }
    
    protected void protectedMethod() {
        
    }

    public void publicMethod() {
        
    }

    private static void staticPrivateMethod() {
        
    }
    
    public static void staticPublicMethod() {
        
    }

    // To test bridge method
    @Override
    public int compareTo(Maru o) {
        return 0;
    }

    // To test synthetic method
    public class ToTestSyntheticMethod {
        public void callOuterPrivate() {
            privateMethod();
        }
    }
}
