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
package com.navercorp.pinpoint.exceptiontrace.collector.mapper;

import com.navercorp.pinpoint.exceptiontrace.collector.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author intr3p1d
 */
@Mapper(componentModel = "spring", uses = {StackTraceMapper.class, ErrorMessageMapper.class})
public interface ExceptionMetaDataMapper {

    @Mappings({
            @Mapping(source = "stackTrace", target = "stackTraceClassName", qualifiedBy = StackTraceMapper.StackTraceToClassNames.class),
            @Mapping(source = "stackTrace", target = "stackTraceFileName", qualifiedBy = StackTraceMapper.StackTraceToFileNames.class),
            @Mapping(source = "stackTrace", target = "stackTraceLineNumber", qualifiedBy = StackTraceMapper.StackTraceToLineNumbers.class),
            @Mapping(source = "stackTrace", target = "stackTraceMethodName", qualifiedBy = StackTraceMapper.StackTraceToMethodNames.class),
            @Mapping(source = "errorMessage", target = "errorMessage", qualifiedBy = ErrorMessageMapper.ReplaceCharacters.class)
    })
    ExceptionMetaDataEntity toEntity(ExceptionMetaData model);

}
