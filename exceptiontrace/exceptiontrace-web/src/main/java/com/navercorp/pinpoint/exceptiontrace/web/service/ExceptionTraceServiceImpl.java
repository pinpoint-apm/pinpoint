/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.exceptiontrace.web.service;

import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.web.dao.ExceptionTraceDao;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceValueView;
import com.navercorp.pinpoint.exceptiontrace.web.util.ExceptionTraceQueryParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author intr3p1d
 */
@Service
public class ExceptionTraceServiceImpl implements ExceptionTraceService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ExceptionTraceDao exceptionTraceDao;

    public ExceptionTraceServiceImpl(ExceptionTraceDao exceptionTraceDao) {
        this.exceptionTraceDao = Objects.requireNonNull(exceptionTraceDao, "exceptionTraceDao");
    }

    @Override
    public List<ExceptionMetaData> getTransactionExceptions(
            ExceptionTraceQueryParameter queryParameter
    ) {
        return applyQueryFunction(
                queryParameter,
                this::getExeptionMetaDataList
        );
    }

    @Override
    public List<ExceptionMetaData> getSummarizedExceptionsInRange(ExceptionTraceQueryParameter queryParameter) {
        return applyQueryFunction(
                queryParameter,
                this::getSummarizedExeptionMetaDataList
        );
    }

    @Override
    public List<ExceptionTraceSummary> getSummaries(ExceptionTraceQueryParameter queryParameter) {
        return applyQueryFunction(
                queryParameter,
                this::getExceptionTraceSummaries
        );
    }

    @Override
    public List<ExceptionTraceValueView> getValueViews(ExceptionTraceQueryParameter queryParameter) {
        return applyQueryFunction(
                queryParameter,
                this::getExceptionTraceValueViews
        );
    }

    private <T> List<T> applyQueryFunction(
            ExceptionTraceQueryParameter queryParameter,
            Function<ExceptionTraceQueryParameter, List<T>> queryFunction
    ) {
        return queryFunction.apply(queryParameter);
    }

    private List<ExceptionMetaData> getExeptionMetaDataList(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getExceptions(queryParameter);
    }

    private List<ExceptionMetaData> getSummarizedExeptionMetaDataList(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getSummarizedExceptions(queryParameter);
    }

    private List<ExceptionTraceSummary> getExceptionTraceSummaries(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getSummaries(queryParameter);
    }

    private List<ExceptionTraceValueView> getExceptionTraceValueViews(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getValueViews(queryParameter);
    }
}
