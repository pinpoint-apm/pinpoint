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

import com.navercorp.pinpoint.exceptiontrace.web.dao.ExceptionTraceDao;
import com.navercorp.pinpoint.exceptiontrace.web.model.ErrorSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.util.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionDetailView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
    public List<ExceptionDetailView> getDetailExceptions(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getExceptions(queryParameter);
    }

    @Override
    public List<ExceptionDetailView> getSummarizedExceptions(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getSummarizedExceptions(queryParameter);
    }

    @Override
    public List<ExceptionGroupSummary> getGroupSummaries(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getGroupSummaries(queryParameter);
    }

    @Override
    public List<ExceptionChartValueView> getChartViews(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getChartValueViews(queryParameter);
    }

    @Override
    public List<ErrorSummary> getErrorSummaries(ExceptionTraceQueryParameter queryParameter) {
        return exceptionTraceDao.getErrorSummaries(queryParameter);
    }
}
