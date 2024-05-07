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
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.GroupedFieldNameEntity;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceValueView;
import com.navercorp.pinpoint.exceptiontrace.web.model.Grouped;
import com.navercorp.pinpoint.exceptiontrace.web.model.GroupedFieldName;
import com.navercorp.pinpoint.exceptiontrace.web.model.RawGroupedFieldName;
import com.navercorp.pinpoint.exceptiontrace.web.util.GroupByAttributes;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionMetaDataView;
import org.mapstruct.AfterMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;

import static com.navercorp.pinpoint.exceptiontrace.web.mapper.CLPMapper.makeReadableString;
import static com.navercorp.pinpoint.exceptiontrace.web.mapper.CLPMapper.replacePlaceHolders;

/**
 * @author intr3p1d
 */
@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        uses = {
                StackTraceMapper.class,
                MapStructUtils.class
        }
)
public interface ExceptionMetaDataEntityMapper {

    @Mappings({
            @Mapping(source = ".", target = "stackTrace", qualifiedBy = StackTraceMapper.StringsToStackTrace.class),
    })
    ExceptionMetaData toModel(ExceptionMetaDataEntity entity);

    @Mappings({
            @Mapping(source = ".", target = "stackTrace", qualifiedBy = StackTraceMapper.StringsToStackTrace.class),
    })
    ExceptionMetaDataView toView(ExceptionMetaDataEntity entity);

    @Mappings({
            @Mapping(source = "entity.values", target = "values", qualifiedBy = MapStructUtils.JsonStrToList.class),
            @Mapping(target = "tags", ignore = true),
            @Mapping(target = "groupedFieldName", ignore = true),
    })
    ExceptionTraceValueView toValueView(
            ExceptionTraceValueViewEntity entity,
            List<GroupByAttributes> attributesList
    );

    @Mappings({
            @Mapping(target = "groupedFieldName", ignore = true),
            @Mapping(source = "entity", target = "rawFieldName"),
    })
    ExceptionTraceSummary toSummary(
            ExceptionTraceSummaryEntity entity,
            List<GroupByAttributes> attributesList
    );

    @AfterMapping
    default void addRawGroupedFieldName(
            ExceptionTraceSummaryEntity entity,
            List<GroupByAttributes> attributesList,
            @MappingTarget ExceptionTraceSummary summary
    ) {
        RawGroupedFieldName groupedFieldName = new RawGroupedFieldName();
        for (GroupByAttributes attributes : attributesList) {
            switch (attributes) {
                case STACK_TRACE -> groupedFieldName.setStackTraceHash(checkIfNull(entity.getStackTraceHash()));
                case URI_TEMPLATE -> groupedFieldName.setUriTemplate(checkIfNull(entity.getUriTemplate()));
                case ERROR_CLASS_NAME -> groupedFieldName.setErrorClassName(checkIfNull(entity.getErrorClassName()));
                case ERROR_MESSAGE_LOG_TYPE ->
                        groupedFieldName.setErrorMessage_logtype(checkIfNull(selectErrorMessage(entity)));
            }
        }
        summary.setRawFieldName(groupedFieldName);
    }

    @AfterMapping
    default void addGroupedFieldName(
            GroupedFieldNameEntity entity,
            List<GroupByAttributes> attributesList,
            @MappingTarget Grouped grouped
    ) {
        GroupedFieldName groupedFieldName = new GroupedFieldName();
        for (GroupByAttributes attributes : attributesList) {
            switch (attributes) {
                case STACK_TRACE -> groupedFieldName.setStackTraceHash(checkIfNull(entity.getStackTraceHash()));
                case URI_TEMPLATE -> groupedFieldName.setUriTemplate(checkIfNull(entity.getUriTemplate()));
                case ERROR_CLASS_NAME -> groupedFieldName.setErrorClassName(checkIfNull(entity.getErrorClassName()));
                case ERROR_MESSAGE_LOG_TYPE ->
                        groupedFieldName.setErrorMessage(checkIfNull(selectErrorMessage(entity)));
            }
        }
        grouped.setGroupedFieldName(groupedFieldName);
    }

    @Named("selectErrorMessage")
    default String selectErrorMessage(GroupedFieldNameEntity entity) {
        if (entity.getErrorMessage_logtype() != null) {
            return replacePlaceHolders(
                    makeReadableString(entity.getErrorMessage_logtype())
            );
        }
        return entity.getErrorMessage();
    }

    default String checkIfNull(String s) {
        return StringUtils.defaultString(s, "null");
    }
}
