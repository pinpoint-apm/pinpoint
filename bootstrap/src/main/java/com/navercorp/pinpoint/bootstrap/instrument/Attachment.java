package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public interface Attachment<T> {

    T getAttachment();

    void setAttachment(T object);

    T getOrCreate(AttachmentFactory<T> attachmentFactory);
}
