package com.navercorp.pinpoint.exceptiontrace.web.mapper;


import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionChartValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionGroupSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.GroupedFieldNameEntity;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionGroupSummary;
import com.navercorp.pinpoint.exceptiontrace.web.util.GroupByAttributes;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionChartValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionDetailView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author intr3p1d
 */
@ContextConfiguration(classes = {
        ExceptionEntityMapperImpl.class,
        StackTraceMapper.class,
        MapStructUtils.class,
        JacksonAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
class ExceptionEntityMapperTest {
    private static final Logger logger = LogManager.getLogger(ExceptionEntityMapper.class);
    private final Random random = new Random();


    @Autowired
    private ExceptionEntityMapper mapper;

    @Autowired
    private MapStructUtils mapStructUtils;


    @Test
    public void testEntityToModel() {
        Throwable throwable = new RuntimeException();

        ExceptionMetaDataEntity expected = newExceptionMetaDataEntity(throwable);
        ExceptionMetaData actual = mapper.toModel(expected);

        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getTransactionId(), actual.getTransactionId());
        assertEquals(expected.getSpanId(), actual.getSpanId());
        assertEquals(expected.getExceptionId(), actual.getExceptionId());

        assertEquals(expected.getApplicationServiceType(), actual.getApplicationServiceType());
        assertEquals(expected.getApplicationName(), actual.getApplicationName());
        assertEquals(expected.getAgentId(), actual.getAgentId());
        assertEquals(expected.getUriTemplate(), actual.getUriTemplate());

        assertEquals(expected.getErrorClassName(), actual.getErrorClassName());
        assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
        assertEquals(expected.getExceptionDepth(), actual.getExceptionDepth());

        assertEquals(expected.getStackTraceHash(), actual.getStackTraceHash());


        int size = throwable.getStackTrace().length;

        String classNames = expected.getStackTraceClassName();
        String fileNames = expected.getStackTraceFileName();
        String lineNumbers = expected.getStackTraceLineNumber();
        String methodNames = expected.getStackTraceMethodName();

        List<String> classNameIter = mapStructUtils.jsonToStringList(classNames);
        List<String> fileNameIter = mapStructUtils.jsonToStringList(fileNames);
        List<Integer> lineNumberIter = mapStructUtils.jsonToIntegerList(lineNumbers);
        List<String> methodNameIter = mapStructUtils.jsonToStringList(methodNames);

        List<StackTraceElementWrapper> actualStackTrace = actual.getStackTrace();

        for (int i = 0; i < size; i++) {
            assertEquals(classNameIter.get(i), actualStackTrace.get(i).getClassName());
            assertEquals(fileNameIter.get(i), actualStackTrace.get(i).getFileName());
            assertEquals(lineNumberIter.get(i), actualStackTrace.get(i).getLineNumber());
            assertEquals(methodNameIter.get(i), actualStackTrace.get(i).getMethodName());
        }
    }

    @Test
    public void testEntityToView() {
        Throwable throwable = new RuntimeException();

        ExceptionMetaDataEntity expected = newExceptionMetaDataEntity(throwable);
        ExceptionDetailView actual = mapper.toDetailView(expected);

        assertEquals(expected.getTimestamp(), actual.getTimestamp());
        assertEquals(expected.getTransactionId(), actual.getTransactionId());
        assertEquals(Long.toString(expected.getSpanId()), actual.getSpanId());
        assertEquals(Long.toString(expected.getExceptionId()), actual.getExceptionId());

        assertEquals(expected.getApplicationServiceType(), actual.getApplicationServiceType());
        assertEquals(expected.getApplicationName(), actual.getApplicationName());
        assertEquals(expected.getAgentId(), actual.getAgentId());
        assertEquals(expected.getUriTemplate(), actual.getUriTemplate());

        assertEquals(expected.getErrorClassName(), actual.getErrorClassName());
        assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
        assertEquals(expected.getExceptionDepth(), actual.getExceptionDepth());

        assertEquals(expected.getStackTraceHash(), actual.getStackTraceHash());


        int size = throwable.getStackTrace().length;

        String classNames = expected.getStackTraceClassName();
        String fileNames = expected.getStackTraceFileName();
        String lineNumbers = expected.getStackTraceLineNumber();
        String methodNames = expected.getStackTraceMethodName();

        List<String> classNameIter = mapStructUtils.jsonToStringList(classNames);
        List<String> fileNameIter = mapStructUtils.jsonToStringList(fileNames);
        List<Integer> lineNumberIter = mapStructUtils.jsonToIntegerList(lineNumbers);
        List<String> methodNameIter = mapStructUtils.jsonToStringList(methodNames);

        List<StackTraceElementWrapper> actualStackTrace = actual.getStackTrace();

        for (int i = 0; i < size; i++) {
            assertEquals(classNameIter.get(i), actualStackTrace.get(i).getClassName());
            assertEquals(fileNameIter.get(i), actualStackTrace.get(i).getFileName());
            assertEquals(lineNumberIter.get(i), actualStackTrace.get(i).getLineNumber());
            assertEquals(methodNameIter.get(i), actualStackTrace.get(i).getMethodName());
        }
    }

    private ExceptionMetaDataEntity newExceptionMetaDataEntity(Throwable throwable) {
        ExceptionMetaDataEntity dataEntity = new ExceptionMetaDataEntity();

        dataEntity.setTimestamp(random.nextLong());
        dataEntity.setTransactionId("transactionId");
        dataEntity.setSpanId(random.nextLong());
        dataEntity.setExceptionId(random.nextLong());
        dataEntity.setApplicationServiceType("applicationServiceType");
        dataEntity.setApplicationName("applicationName");
        dataEntity.setAgentId("agentId");
        dataEntity.setUriTemplate("uriTemplate");
        dataEntity.setErrorClassName("errorClassName");
        dataEntity.setErrorMessage("errorMessage");
        dataEntity.setExceptionDepth(random.nextInt());
        dataEntity.setStackTraceHash("stackTraceHash");

        List<StackTraceElement> elements = List.of(throwable.getStackTrace());

        dataEntity.setStackTraceClassName(toFlattenedString(elements, StackTraceElement::getClassName));
        dataEntity.setStackTraceFileName(toFlattenedString(elements, StackTraceElement::getFileName));
        dataEntity.setStackTraceLineNumber(toFlattenedString(elements, StackTraceElement::getLineNumber));
        dataEntity.setStackTraceMethodName(toFlattenedString(elements, StackTraceElement::getMethodName));
        return dataEntity;
    }

    private <T> String toFlattenedString(List<StackTraceElement> elements, Function<StackTraceElement, T> getter) {
        List<T> collect = elements.stream().map(getter).collect(Collectors.toList());
        return mapStructUtils.listToJsonStr(collect);
    }

    @Test
    public void testEntityToValueView() {
        ExceptionChartValueViewEntity expected = newExceptionMetaDataEntity();

        ExceptionChartValueView actual = mapper.toChartView(
                expected, List.of(GroupByAttributes.values())
        );

        assertEquals(expected.getUriTemplate(), actual.getGroupedFieldName().getUriTemplate());
        assertEquals(expected.getErrorClassName(), actual.getGroupedFieldName().getErrorClassName());
        assertEquals(expected.getErrorMessage(), actual.getGroupedFieldName().getErrorMessage());
        assertEquals(expected.getStackTraceHash(), actual.getGroupedFieldName().getStackTraceHash());

        Assertions.assertNotNull(actual.getValues());
        Assertions.assertFalse(actual.getValues().isEmpty());
    }


    private ExceptionChartValueViewEntity newExceptionMetaDataEntity() {
        ExceptionChartValueViewEntity dataEntity = new ExceptionChartValueViewEntity();

        dataEntity.setUriTemplate("uriTemplate");
        dataEntity.setErrorClassName("errorClassName");
        dataEntity.setErrorMessage("errorMessage");
        dataEntity.setStackTraceHash("stackTraceHash");

        dataEntity.setValues("[0,83,2,12]");
        return dataEntity;
    }

    @Test
    public void testEntityToSummary() {
        ExceptionGroupSummaryEntity expected = newExceptionTraceSummaryEntity();

        ExceptionGroupSummary actual = mapper.toSummary(
                expected, List.of(GroupByAttributes.values())
        );

        assertEquals(expected.getMostRecentErrorClass(), actual.getMostRecentErrorClass());
        assertEquals(expected.getMostRecentErrorMessage(), actual.getMostRecentErrorMessage());
        assertEquals(expected.getCount(), actual.getCount());
        assertEquals(expected.getFirstOccurred(), actual.getFirstOccurred());
        assertEquals(expected.getLastOccurred(), actual.getLastOccurred());

        assertEquals(expected.getUriTemplate(), actual.getGroupedFieldName().getUriTemplate());
        assertEquals(expected.getErrorClassName(), actual.getGroupedFieldName().getErrorClassName());
        assertEquals(expected.getErrorMessage(), actual.getGroupedFieldName().getErrorMessage());
        assertEquals(expected.getStackTraceHash(), actual.getGroupedFieldName().getStackTraceHash());
    }

    private ExceptionGroupSummaryEntity newExceptionTraceSummaryEntity() {
        ExceptionGroupSummaryEntity entity = new ExceptionGroupSummaryEntity();

        entity.setMostRecentErrorClass("MostRecentErrorClass");
        entity.setMostRecentErrorMessage("MostRecentErrorMessage");
        entity.setCount(random.nextLong());
        entity.setFirstOccurred(random.nextLong());
        entity.setLastOccurred(random.nextLong());

        entity.setUriTemplate("uriTemplate");
        entity.setErrorClassName("errorClassName");
        entity.setErrorMessage("errorMessage");
        entity.setStackTraceHash("stackTraceHash");
        return entity;
    }

    @Test
    public void testSelectErrorMessage() {
        GroupedFieldNameEntity entity = new GroupedFieldNameEntity();
        entity.setErrorMessage_logtype("getAgentsList.from: \u0011\u0001 ì\u009D´ì\u0083\u0081ì\u009D´ì\u0096´ì\u0095¼ í\u0095©ë\u008B\u0088ë\u008B¤");

        String result = mapper.selectErrorMessage(entity);
        assertEquals(
                "getAgentsList.from: " + CLPMapper.DICTIONARY_REPLACEMENT + " 이상이어야 합니다"
                , result
        );
    }
}