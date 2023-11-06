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
package com.navercorp.pinpoint.exceptiontrace.web.mapper;

import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionMetaDataView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author intr3p1d
 */
@Mapper(componentModel = "spring", uses = {StackTraceMapper.class, MapStructUtils.class})
public interface ExceptionMetaDataEntityMapper {

    @Mappings(
            @Mapping(source = ".", target = "stackTrace", qualifiedBy = StackTraceMapper.StringsToStackTrace.class)
    )
    ExceptionMetaData toModel(ExceptionMetaDataEntity entity);

    @Mappings(
            @Mapping(source = ".", target = "stackTrace", qualifiedBy = StackTraceMapper.StringsToStackTrace.class)
    )
    ExceptionMetaDataView toView(ExceptionMetaDataEntity entity);

    @Mappings({
            @Mapping(source = "values", target = "values", qualifiedBy = MapStructUtils.JsonStrToList.class),
            @Mapping(source = "uriTemplate", target = "groupedFieldName.uriTemplate"),
            @Mapping(source = "errorClassName", target = "groupedFieldName.errorClassName"),
            @Mapping(source = "errorMessage", target = "groupedFieldName.errorMessage"),
            @Mapping(source = "stackTraceHash", target = "groupedFieldName.stackTraceHash")
    })
    ExceptionTraceValueView entityToExceptionTraceValueView(ExceptionTraceValueViewEntity entity);

    @Mappings({
            @Mapping(source = "uriTemplate", target = "groupedFieldName.uriTemplate"),
            @Mapping(source = "errorClassName", target = "groupedFieldName.errorClassName"),
            @Mapping(source = "errorMessage", target = "groupedFieldName.errorMessage"),
            @Mapping(source = "stackTraceHash", target = "groupedFieldName.stackTraceHash")
    })
    ExceptionTraceSummary entityToExceptionTraceSummary(ExceptionTraceSummaryEntity entity);

}
