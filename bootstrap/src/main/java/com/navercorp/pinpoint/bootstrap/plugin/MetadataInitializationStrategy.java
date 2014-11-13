package com.nhn.pinpoint.bootstrap.plugin;

public interface MetadataInitializationStrategy {
    public static final class ByConstructor implements MetadataInitializationStrategy {
        private final String className;
        
        public ByConstructor(String className) {
            this.className = className;
        }
        
        public String getClassName() {
            return className;
        }
    }
}
