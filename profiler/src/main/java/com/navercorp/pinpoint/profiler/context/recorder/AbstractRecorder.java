/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.context.recorder;

import com.navercorp.pinpoint.bootstrap.context.AttributeRecorder;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;

import java.util.Objects;

import com.navercorp.pinpoint.common.util.DataType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.profiler.context.Annotation;
import com.navercorp.pinpoint.profiler.context.annotation.Annotations;
import com.navercorp.pinpoint.profiler.context.errorhandler.IgnoreErrorHandler;
import com.navercorp.pinpoint.profiler.context.exception.ExceptionRecordingService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;

/**
 * @author jaehong.kim
 */
public abstract class AbstractRecorder implements AttributeRecorder {

    protected final StringMetaDataService stringMetaDataService;
    protected final SqlMetaDataService sqlMetaDataService;
    protected final IgnoreErrorHandler ignoreErrorHandler;
    protected final ExceptionRecordingService exceptionRecordingService;

    public AbstractRecorder(final StringMetaDataService stringMetaDataService,
                            SqlMetaDataService sqlMetaDataService,
                            IgnoreErrorHandler ignoreErrorHandler,
                            ExceptionRecordingService exceptionRecordingService) {
        this.stringMetaDataService = Objects.requireNonNull(stringMetaDataService, "stringMetaDataService");
        this.sqlMetaDataService = Objects.requireNonNull(sqlMetaDataService, "sqlMetaDataService");
        this.ignoreErrorHandler = Objects.requireNonNull(ignoreErrorHandler, "ignoreErrorHandler");
        this.exceptionRecordingService = Objects.requireNonNull(exceptionRecordingService, "exceptionRecordingService");
    }

    public void recordError() {
        maskErrorCode(1);
    }

    public void recordException(Throwable throwable) {
        recordException(true, throwable);
    }

    public void recordException(boolean markError, Throwable throwable) {
        recordDetailedException(throwable);
        if (throwable == null) {
            return;
        }
        final String drop = StringUtils.abbreviate(throwable.getMessage(), 256);
        // An exception that is an instance of a proxy class could make something wrong because the class name will vary.
        final int exceptionId = stringMetaDataService.cacheString(throwable.getClass().getName());
        setExceptionInfo(exceptionId, drop);
        if (markError) {
            if (!ignoreErrorHandler.handleError(throwable)) {
                recordError();
            }
        }
    }

    abstract void recordDetailedException(Throwable throwable);

    abstract void setExceptionInfo(int exceptionClassId, String exceptionMessage);

    abstract void maskErrorCode(final int errorCode);

    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == 0) {
            recordAttribute(AnnotationKey.API, methodDescriptor.getFullName());
        } else {
            setApiId0(methodDescriptor.getApiId());
        }
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        recordApi(methodDescriptor);
        recordArgs(args);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        recordApi(methodDescriptor);
        recordSingleArg(args, index);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        recordApi(methodDescriptor);
        recordArgs(args, start, end);
    }

    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        recordApi(methodDescriptor);
        recordSingleCachedString(args, index);
    }

    abstract void setApiId0(final int apiId);

    private void recordArgs(Object[] args, int start, int end) {
        if (args != null) {
            int max = Math.min(Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE), end);
            for (int i = start; i < max; i++) {
                recordAttribute(AnnotationKeyUtils.getArgs(i), args[i]);
            }
            // TODO How to handle if args length is greater than MAX_ARGS_SIZE?
        }
    }

    private void recordSingleArg(Object args, int index) {
        if (args != null) {
            recordAttribute(AnnotationKeyUtils.getArgs(index), args);
        }
    }

    private void recordSingleCachedString(String args, int index) {
        if (args != null) {
            int cacheId = stringMetaDataService.cacheString(args);
            recordAttribute(AnnotationKeyUtils.getCachedArgs(index), cacheId);
        }
    }

    private void recordArgs(Object[] args) {
        if (args != null) {
            int max = Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE);
            for (int i = 0; i < max; i++) {
                recordAttribute(AnnotationKeyUtils.getArgs(i), args[i]);
            }
            // TODO How to handle if args length is greater than MAX_ARGS_SIZE?
        }
    }

    @Override
    public void recordAttribute(AnnotationKey key, String value) {
        Annotation<String> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, int value) {
        Annotation<Integer> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Integer value) {
        Annotation<Integer> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, long value) {
        Annotation<Long> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Long value) {
        Annotation<Long> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, boolean value) {
        Annotation<Boolean> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, double value) {
        Annotation<Double> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, byte[] value) {
        Annotation<byte[]> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, DataType value) {
        Annotation<DataType> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    @Override
    public void recordAttribute(AnnotationKey key, Object value) {
        Annotation<?> annotation = Annotations.of(key.getCode(), value);
        addAnnotation(annotation);
    }

    abstract void addAnnotation(Annotation<?> annotation);
}
