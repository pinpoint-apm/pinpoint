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

package com.navercorp.pinpoint.common.bo;

import com.navercorp.pinpoint.common.buffer.Buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class AnnotationBoList {
    private List<AnnotationBo> annotationBoList;

    public AnnotationBoList() {
        this.annotationBoList = new ArrayList<AnnotationBo>();
    }


    public AnnotationBoList(int annotationBoListSize) {
        this.annotationBoList = new ArrayList<AnnotationBo>(annotationBoListSize);
    }

    public AnnotationBoList(List<AnnotationBo> annotationBoList) {
        if (annotationBoList == null) {
            this.annotationBoList = Collections.emptyList();
            return;
        }
        this.annotationBoList = annotationBoList;
    }

    public List<AnnotationBo> getAnnotationBoList() {
        return annotationBoList;
    }

    public void addAnnotationBo(AnnotationBo annotationBo) {
        this.annotationBoList.add(annotationBo);
    }

    public void writeValue(Buffer writer){

        int size = this.annotationBoList.size();
        writer.putVar(size);
        for (AnnotationBo annotationBo : this.annotationBoList) {
            annotationBo.writeValue(writer);
        }
    }

    public void readValue(Buffer reader) {
        int size = reader.readVarInt();
        if (size == 0) {
            return;
        }
        this.annotationBoList = new ArrayList<AnnotationBo>(size);
        for (int i = 0; i < size; i++) {
            AnnotationBo bo = new AnnotationBo();
            bo.readValue(reader);
            this.annotationBoList.add(bo);
        }
    }

    public int size() {
        return this.annotationBoList.size();
    }


    public void setSpanId(long spanId) {
        for (AnnotationBo annotationBo : this.annotationBoList) {
            annotationBo.setSpanId(spanId);
        }
    }
}
