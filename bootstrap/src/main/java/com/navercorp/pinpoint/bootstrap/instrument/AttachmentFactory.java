package com.navercorp.pinpoint.bootstrap.instrument;

/**
 * @author emeroad
 */
public interface AttachmentFactory<T> {
    T createAttachment();
}
