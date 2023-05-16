package com.navercorp.pinpoint.test.plugin.util;

import java.util.List;
import java.util.Objects;

public class ParentClass {
    static final boolean DELEGATE_PARENT = true;
    static final boolean ON_LOAD_CLASS = false;

    private final String[] libClass;

    public ParentClass(List<String> libClass) {
        Objects.requireNonNull(libClass, "libClass");
        this.libClass = libClass.toArray(new String[0]);
    }


    public boolean onDelegate(String clazzName) {
        for (String aClass : libClass) {
            if (clazzName.startsWith(aClass)) {
                return DELEGATE_PARENT;
            }
        }
        return ON_LOAD_CLASS;
    }
}
