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

import com.navercorp.pinpoint.exceptiontrace.web.model.ErrorSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.query.ExceptionTraceQueryParameter;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionDetailView;
import com.navercorp.pinpoint.exceptiontrace.web.model.ClpConverted;
import com.navercorp.pinpoint.exceptiontrace.web.query.ClpQueryParameter;

import java.util.List;

/**
 * @author intr3p1d
 */
public interface ExceptionTraceService {

    List<ExceptionDetailView> getDetailExceptions(ExceptionTraceQueryParameter queryParameter);

    List<ExceptionDetailView> getSummarizedExceptions(ExceptionTraceQueryParameter queryParameter);

    List<ExceptionGroupSummary> getGroupSummaries(ExceptionTraceQueryParameter queryParameter);

    List<ExceptionChartValueView> getChartViews(ExceptionTraceQueryParameter queryParameter);
    List<ErrorSummary> getErrorSummaries(ExceptionTraceQueryParameter queryParameter);
    List<ClpConverted> getReplacedVariables(ClpQueryParameter queryParameter);
}
