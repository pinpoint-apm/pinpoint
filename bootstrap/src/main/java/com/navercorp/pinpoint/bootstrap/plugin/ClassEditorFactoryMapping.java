/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.bootstrap.plugin;

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
