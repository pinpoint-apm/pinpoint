package com.nhn.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public class Method {
    private final String name;
    private final String[] paramTypes;
    private final int accessFlags;

    public Method(String name, String[] paramTypes, int accessFlags) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }

        if (paramTypes == null) {
            throw new NullPointerException("paramTypes must not be null");
        }
        
        this.name = name;
        this.paramTypes = paramTypes;
        this.accessFlags = accessFlags;
    }

    public String getName() {
        return name;
    }

    public String[] getParameterTypes() {
        return paramTypes;
    }

    public int getModifiers() {
        return accessFlags;
    }
    
    public boolean isConstructor() {
        return name.equals("<init>");
    }
}
