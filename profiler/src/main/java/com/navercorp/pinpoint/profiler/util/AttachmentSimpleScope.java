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
