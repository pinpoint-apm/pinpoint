package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public interface Attachment {

    Object getAttachment();

    void setAttachment(Object object);
}
