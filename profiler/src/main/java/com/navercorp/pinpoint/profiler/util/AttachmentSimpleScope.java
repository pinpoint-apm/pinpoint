package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.instrument.AttachmentScope;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * Lifecycle management DataStructure
 * @author emeroad
 */
public class AttachmentSimpleScope<T> extends SimpleScope implements AttachmentScope<T> {

    private T attachment;

    @Override
    public int pop() {
        final int pop = super.pop();
        if (pop == Scope.ZERO) {
            // clear reference
            this.attachment = null;
        }
        return pop;
    }

    public AttachmentSimpleScope(String name) {
        super(name);
    }


    @Override
    public T getAttachment() {
        return attachment;
    }

    @Override
    public void setAttachment(T object) {
        this.attachment = object;
    }

    @Override
    public T getOrCreate(AttachmentFactory<T> attachmentFactory) {
        if (attachmentFactory == null) {
            throw new NullPointerException("attachmentFactory must not be null");
        }
        if (this.attachment == null) {
            this.attachment = attachmentFactory.createAttachment();
        }
        return this.attachment;
    }
}
