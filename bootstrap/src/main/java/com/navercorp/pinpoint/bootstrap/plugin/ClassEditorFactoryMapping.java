package com.nhn.pinpoint.bootstrap.plugin;

public class ClassEditorFactoryMapping {
    private final String targetClassName;
    private final String editorFactoryClassName;
    
    public ClassEditorFactoryMapping(String targetClassName, String editorFactoryClassName) {
        this.targetClassName = targetClassName;
        this.editorFactoryClassName = editorFactoryClassName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }

    public String getEditorFactoryClassName() {
        return editorFactoryClassName;
    }
}
