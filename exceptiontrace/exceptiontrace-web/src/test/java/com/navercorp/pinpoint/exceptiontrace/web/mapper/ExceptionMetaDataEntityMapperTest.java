package com.navercorp.pinpoint.exceptiontrace.web.mapper;


import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceSummaryEntity;
import com.navercorp.pinpoint.exceptiontrace.web.entity.ExceptionTraceValueViewEntity;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceSummary;
import com.navercorp.pinpoint.exceptiontrace.web.model.ExceptionTraceValueView;
import com.navercorp.pinpoint.exceptiontrace.web.view.ExceptionMetaDataView;
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

/**
 * @author intr3p1d
 */
@ContextConfiguration(classes = {
        ExceptionMetaDataEntityMapperImpl.class,
        StackTraceMapper.class,
        MapStructUtils.class,
        JacksonAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
class ExceptionMetaDataEntityMapperTest {
    private static final Logger logger = LogManager.getLogger(ExceptionMetaDataEntityMapper.class);
    private final Random random = new Random();


    @Autowired
    private ExceptionMetaDataEntityMapper mapper;

    @Autowired
    private MapStructUtils mapStructUtils;


    @Test
    public void testEntityToModel() {
        Throwable throwable = new RuntimeException();

        ExceptionMetaDataEntity expected = newExceptionMetaDataEntity(throwable);
        ExceptionMetaData actual = mapper.toModel(expected);

        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assertions.assertEquals(expected.getTransactionId(), actual.getTransactionId());
        Assertions.assertEquals(expected.getSpanId(), actual.getSpanId());
        Assertions.assertEquals(expected.getExceptionId(), actual.getExceptionId());

        Assertions.assertEquals(expected.getApplicationServiceType(), actual.getApplicationServiceType());
        Assertions.assertEquals(expected.getApplicationName(), actual.getApplicationName());
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId());
        Assertions.assertEquals(expected.getUriTemplate(), actual.getUriTemplate());

        Assertions.assertEquals(expected.getErrorClassName(), actual.getErrorClassName());
        Assertions.assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
        Assertions.assertEquals(expected.getExceptionDepth(), actual.getExceptionDepth());

        Assertions.assertEquals(expected.getStackTraceHash(), actual.getStackTraceHash());


        int size = throwable.getStackTrace().length;

        String classNames = expected.getStackTraceClassName();
        String fileNames = expected.getStackTraceFileName();
        String lineNumbers = expected.getStackTraceLineNumber();
        String methodNames = expected.getStackTraceMethodName();

        List<String> classNameIter = convertToList(classNames);
        List<String> fileNameIter = convertToList(fileNames);
        List<Integer> lineNumberIter = convertToList(lineNumbers);
        List<String> methodNameIter = convertToList(methodNames);

        List<StackTraceElementWrapper> actualStackTrace = actual.getStackTrace();

        for (int i = 0; i < size; i++) {
            Assertions.assertEquals(classNameIter.get(i), actualStackTrace.get(i).getClassName());
            Assertions.assertEquals(fileNameIter.get(i), actualStackTrace.get(i).getFileName());
            Assertions.assertEquals(lineNumberIter.get(i), actualStackTrace.get(i).getLineNumber());
            Assertions.assertEquals(methodNameIter.get(i), actualStackTrace.get(i).getMethodName());
        }
    }

    @Test
    public void testEntityToView() {
        Throwable throwable = new RuntimeException();

        ExceptionMetaDataEntity expected = newExceptionMetaDataEntity(throwable);
        ExceptionMetaDataView actual = mapper.toView(expected);

        Assertions.assertEquals(expected.getTimestamp(), actual.getTimestamp());
        Assertions.assertEquals(expected.getTransactionId(), actual.getTransactionId());
        Assertions.assertEquals(Long.toString(expected.getSpanId()), actual.getSpanId());
        Assertions.assertEquals(Long.toString(expected.getExceptionId()), actual.getExceptionId());

        Assertions.assertEquals(expected.getApplicationServiceType(), actual.getApplicationServiceType());
        Assertions.assertEquals(expected.getApplicationName(), actual.getApplicationName());
        Assertions.assertEquals(expected.getAgentId(), actual.getAgentId());
        Assertions.assertEquals(expected.getUriTemplate(), actual.getUriTemplate());

        Assertions.assertEquals(expected.getErrorClassName(), actual.getErrorClassName());
        Assertions.assertEquals(expected.getErrorMessage(), actual.getErrorMessage());
        Assertions.assertEquals(expected.getExceptionDepth(), actual.getExceptionDepth());

        Assertions.assertEquals(expected.getStackTraceHash(), actual.getStackTraceHash());


        int size = throwable.getStackTrace().length;

        String classNames = expected.getStackTraceClassName();
        String fileNames = expected.getStackTraceFileName();
        String lineNumbers = expected.getStackTraceLineNumber();
        String methodNames = expected.getStackTraceMethodName();

        List<String> classNameIter = convertToList(classNames);
        List<String> fileNameIter = convertToList(fileNames);
        List<Integer> lineNumberIter = convertToList(lineNumbers);
        List<String> methodNameIter = convertToList(methodNames);

        List<StackTraceElementWrapper> actualStackTrace = actual.getStackTrace();

        for (int i = 0; i < size; i++) {
            Assertions.assertEquals(classNameIter.get(i), actualStackTrace.get(i).getClassName());
            Assertions.assertEquals(fileNameIter.get(i), actualStackTrace.get(i).getFileName());
            Assertions.assertEquals(lineNumberIter.get(i), actualStackTrace.get(i).getLineNumber());
            Assertions.assertEquals(methodNameIter.get(i), actualStackTrace.get(i).getMethodName());
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

    public <T> List<T> convertToList(String json) {
        return mapStructUtils.jsonStrToList(json);
    }

    @Test
    public void testEntityToValueView() {
        ExceptionTraceValueViewEntity expected = newExceptionMetaDataEntity();

        ExceptionTraceValueView actual = mapper.entityToExceptionTraceValueView(expected);

        Assertions.assertEquals(expected.getUriTemplate(), actual.getGroupedFieldName().getUriTemplate());
        Assertions.assertEquals(expected.getErrorClassName(), actual.getGroupedFieldName().getErrorClassName());
        Assertions.assertEquals(expected.getErrorMessage(), actual.getGroupedFieldName().getErrorMessage());
        Assertions.assertEquals(expected.getStackTraceHash(), actual.getGroupedFieldName().getStackTraceHash());

        Assertions.assertNotNull(actual.getValues());
        Assertions.assertFalse(actual.getValues().isEmpty());
    }


    private ExceptionTraceValueViewEntity newExceptionMetaDataEntity() {
        ExceptionTraceValueViewEntity dataEntity = new ExceptionTraceValueViewEntity();

        dataEntity.setUriTemplate("uriTemplate");
        dataEntity.setErrorClassName("errorClassName");
        dataEntity.setErrorMessage("errorMessage");
        dataEntity.setStackTraceHash("stackTraceHash");

        dataEntity.setValues("[0,83,2,12]");
        return dataEntity;
    }

    @Test
    public void testEntityToSummary() {
        ExceptionTraceSummaryEntity expected = newExceptionTraceSummaryEntity();

        ExceptionTraceSummary actual = mapper.entityToExceptionTraceSummary(expected);

        Assertions.assertEquals(expected.getMostRecentErrorClass(), actual.getMostRecentErrorClass());
        Assertions.assertEquals(expected.getMostRecentErrorMessage(), actual.getMostRecentErrorMessage());
        Assertions.assertEquals(expected.getCount(), actual.getCount());
        Assertions.assertEquals(expected.getFirstOccurred(), actual.getFirstOccurred());
        Assertions.assertEquals(expected.getLastOccurred(), actual.getLastOccurred());

        Assertions.assertEquals(expected.getUriTemplate(), actual.getGroupedFieldName().getUriTemplate());
        Assertions.assertEquals(expected.getErrorClassName(), actual.getGroupedFieldName().getErrorClassName());
        Assertions.assertEquals(expected.getErrorMessage(), actual.getGroupedFieldName().getErrorMessage());
        Assertions.assertEquals(expected.getStackTraceHash(), actual.getGroupedFieldName().getStackTraceHash());
    }

    private ExceptionTraceSummaryEntity newExceptionTraceSummaryEntity() {
        ExceptionTraceSummaryEntity entity = new ExceptionTraceSummaryEntity();

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

}