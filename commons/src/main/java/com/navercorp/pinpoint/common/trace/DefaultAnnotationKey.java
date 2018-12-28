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

package com.navercorp.pinpoint.common.trace;

/**
 * 
 * @author netspider
 * @author emeroad
 * @author Jongho Moon
 */
public class DefaultAnnotationKey implements AnnotationKey {

    private final int code;
    private final String name;
    private final boolean viewInRecordSet;
    private final boolean errorApiMetadata;

    DefaultAnnotationKey(AnnotationKeyBuilder builder) {
        this.code = builder.code();
        this.name = builder.name();
        this.viewInRecordSet = builder.viewInRecordSet();
        this.errorApiMetadata = builder.errorApiMetadata();
    }

    DefaultAnnotationKey(int code, String name, AnnotationKeyProperty... properties) {
        this.code = code;
        this.name = name;
        
        boolean viewInRecordSet = false;
        boolean errorApiMetadata = false;
        
        for (AnnotationKeyProperty property : properties) {
            switch (property) {
            case VIEW_IN_RECORD_SET:
                viewInRecordSet = true;
                break;
            case ERROR_API_METADATA:
                errorApiMetadata = true;
                break;
            }
        }
        
        this.viewInRecordSet = viewInRecordSet;
        this.errorApiMetadata = errorApiMetadata;
    }
    

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }
    
    public boolean isErrorApiMetadata() {
        return errorApiMetadata;
    }

    public boolean isViewInRecordSet() {
        return viewInRecordSet;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnnotationKey{");
        sb.append("code=").append(code);
        sb.append(", name='").append(name);
        sb.append('}');
        return sb.toString();
    }
}