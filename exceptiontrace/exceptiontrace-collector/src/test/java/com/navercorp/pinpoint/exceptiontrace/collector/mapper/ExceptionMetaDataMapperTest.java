package com.navercorp.pinpoint.exceptiontrace.collector.mapper;


import com.navercorp.pinpoint.common.server.mapper.MapStructUtils;
import com.navercorp.pinpoint.exceptiontrace.collector.entity.ExceptionMetaDataEntity;
import com.navercorp.pinpoint.exceptiontrace.common.model.ExceptionMetaData;
import com.navercorp.pinpoint.exceptiontrace.common.model.StackTraceElementWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
@ContextConfiguration(classes = {
        ExceptionMetaDataMapperImpl.class,
        StackTraceMapper.class,
        ErrorMessageMapper.class,
        MapStructUtils.class,
        JacksonAutoConfiguration.class
})
@ExtendWith(SpringExtension.class)
class ExceptionMetaDataMapperTest {

    private final Logger logger = LogManager.getLogger(getClass());
    private final Random random = new Random();


    @Autowired
    ExceptionMetaDataMapper mapper;

    @Autowired
    MapStructUtils mapStructUtils;


    @Test
    public void testModelToEntity() {
        Throwable throwable = new RuntimeException();

        ExceptionMetaData expected = newRandomExceptionMetaData(throwable);
        ExceptionMetaDataEntity actual = mapper.toEntity(expected);

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

        StackTraceElement[] expectedStackTrace = throwable.getStackTrace();
        int size = throwable.getStackTrace().length;

        List<String> classNames = actual.getStackTraceClassName();
        List<String> fileNames = actual.getStackTraceFileName();
        List<Integer> lineNumbers = actual.getStackTraceLineNumber();
        List<String> methodNames = actual.getStackTraceMethodName();

        for (int i = 0; i < throwable.getStackTrace().length; i++) {
            StackTraceElement stackTraceElement = expectedStackTrace[i];
            Assertions.assertEquals(stackTraceElement.getClassName(), classNames.get(i));
            Assertions.assertEquals(stackTraceElement.getFileName(), fileNames.get(i));
            Assertions.assertEquals(stackTraceElement.getLineNumber(), lineNumbers.get(i));
            Assertions.assertEquals(stackTraceElement.getMethodName(), methodNames.get(i));
        }
    }


    private ExceptionMetaData newRandomExceptionMetaData(Throwable throwable) {
        List<StackTraceElementWrapper> wrapperList = wrapperList(throwable);

        return new ExceptionMetaData(
                "",
                random.nextLong(),
                "transactionId",
                random.nextLong(),
                random.nextLong(),
                "applicationServiceType",
                "applicationName",
                "agentId",
                "uriTemplate",
                "errorClassName",
                "errorMessage",
                random.nextInt(),
                wrapperList,
                "stackTraceHash"
        );
    }

    private List<StackTraceElementWrapper> wrapperList(Throwable throwable) {
        StackTraceElement[] stackTrace = throwable.getStackTrace();

        return Arrays.stream(stackTrace).map(
                (StackTraceElement s) -> new StackTraceElementWrapper(
                        s.getClassName(), s.getFileName(), s.getLineNumber(), s.getMethodName()
                )
        ).collect(Collectors.toList());
    }


    @Test
    public void testReplaceCharacter() {
        ErrorMessageMapper errorMessageMapper = new ErrorMessageMapper(true);
        String text = "가나다라마바사아자차카타파하";
        String replaced = errorMessageMapper.replaceCharacters(text);
        Assertions.assertEquals("", replaced);

        String text2 = "!\"#$%&'()*+,-./0123456789:;<=>?@" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`" +
                "abcdefghijklmnopqrstuvwxyz{|}~" +
                "This should be remain.";
        String replaced2 = errorMessageMapper.replaceCharacters(text2);
        Assertions.assertEquals(text2, replaced2);
    }

}