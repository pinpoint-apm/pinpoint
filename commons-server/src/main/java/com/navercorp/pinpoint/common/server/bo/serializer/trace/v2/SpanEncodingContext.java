package com.navercorp.pinpoint.common.server.bo.serializer.trace.v2;


/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanEncodingContext<T> {
    private T value;

//    private AnnotationBo prevAnnotationBo;

    public SpanEncodingContext(T value) {
        this.value = value;
    }


    public T getValue() {
        return value;
    }

//    public AnnotationBo getPrevFirstAnnotationBo() {
//        return prevAnnotationBo;
//    }
//
//    public void setPrevFirstAnnotationBo(AnnotationBo prevAnnotationBo) {
//        this.prevAnnotationBo = prevAnnotationBo;
//    }
}
