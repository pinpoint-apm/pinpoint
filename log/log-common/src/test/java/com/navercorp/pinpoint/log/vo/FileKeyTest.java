package com.navercorp.pinpoint.log.vo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

class FileKeyTest {

    @Test
    void parse() throws ParseException {
        FileKey fileKey = FileKey.parse("hostGroupName:hostName:fileName");

        Assertions.assertEquals("hostGroupName", fileKey.hostKey().hostGroupName());
        Assertions.assertEquals("hostName", fileKey.hostKey().hostName());
        Assertions.assertEquals("fileName", fileKey.fileName());

    }
}