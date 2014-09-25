package com.nhn.pinpoint.common.bo;

import com.nhn.pinpoint.common.buffer.Buffer;

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
