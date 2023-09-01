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

package com.navercorp.pinpoint.exceptiontrace.collector.service;

import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionWrapperBo;
import com.navercorp.pinpoint.common.server.bo.exception.StackTraceElementWrapperBo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.exceptiontrace.collector.dao.ExceptionTraceDao;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
@Service
@ConditionalOnProperty(name = "pinpoint.modules.collector.exceptiontrace.enabled", havingValue = "true")
@Validated
public class PinotExceptionTraceService implements ExceptionMetaDataService {
    private final ExceptionTraceDao exceptionTraceDao;
    private final ServiceTypeRegistryService registry;

    public PinotExceptionTraceService(ExceptionTraceDao exceptionTraceDao, ServiceTypeRegistryService registry) {
        this.exceptionTraceDao = Objects.requireNonNull(exceptionTraceDao, "exceptionTraceDao");
        this.registry = Objects.requireNonNull(registry, "serviceTypeRegistryService");
    }

    @Override
    public void save(@Valid ExceptionMetaDataBo exceptionMetaDataBo) {
        List<ExceptionMetaData> exceptionMetaData = toExceptionMetaData(exceptionMetaDataBo);
        exceptionTraceDao.insert(exceptionMetaData);
    }

    private List<ExceptionMetaData> toExceptionMetaData(
            ExceptionMetaDataBo exceptionMetaDataBo
    ) {
        List<ExceptionMetaData> exceptionMetaData = new ArrayList<>();
        final ServiceType serviceType = registry.findServiceType(exceptionMetaDataBo.getServiceType());
        for (ExceptionWrapperBo e : exceptionMetaDataBo.getExceptionWrapperBos()) {
            final List<StackTraceElementWrapper> wrappers = traceElementWrappers(e.getStackTraceElements());
            exceptionMetaData.add(
                    ExceptionMetaData.valueOf(
                            e.getStartTime(),
                            transactionIdToString(exceptionMetaDataBo.getTransactionId()),
                            exceptionMetaDataBo.getSpanId(),
                            e.getExceptionId(),
                            serviceType.getName(),
                            exceptionMetaDataBo.getApplicationName(),
                            exceptionMetaDataBo.getAgentId(),
                            exceptionMetaDataBo.getUriTemplate(),
                            e.getExceptionClassName(),
                            e.getExceptionMessage(),
                            e.getExceptionDepth(),
                            wrappers
                    )
            );
        }
        return exceptionMetaData;
    }

    private static List<StackTraceElementWrapper> traceElementWrappers(List<StackTraceElementWrapperBo> wrapperBos) {
        return wrapperBos.stream().map(
                (StackTraceElementWrapperBo s) -> new StackTraceElementWrapper(s.getClassName(), s.getFileName(), s.getLineNumber(), s.getMethodName())
        ).collect(Collectors.toList());
    }

    private static String transactionIdToString(TransactionId transactionId) {
        return TransactionIdUtils.formatString(transactionId);
    }

}
