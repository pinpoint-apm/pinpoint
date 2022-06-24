package com.navercorp.pinpoint.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentLengthTest {

    @Test
    public void length() throws IOException {
        ContentLength.Builder builder = ContentLength.newBuilder();
        builder.addContentType(String.class);
        builder.addContentType(byte[].class);
        builder.addContentType(char[].class);
        builder.addContentType(InputStream.class);
        builder.addContentType(File.class);
        ContentLength contentLength = builder.build();

        String strContent = "abc";
        int strLength = contentLength.getLength(strContent);
        Assertions.assertEquals(strContent.length(), strLength);

        byte[] byteArray = new byte[1];
        int bytesLength = contentLength.getLength(byteArray);
        Assertions.assertEquals(byteArray.length, bytesLength);

        char[] charArray = new char[5];
        int charsLength = contentLength.getLength(charArray);
        Assertions.assertEquals(charArray.length, charsLength);

        InputStream inputStream = mock(InputStream.class);
        when(inputStream.available()).thenReturn(20);
        int streamLength = contentLength.getLength(inputStream);
        Assertions.assertEquals(inputStream.available(), streamLength);

        File file = mock(File.class);
        when(file.length()).thenReturn(30L);
        int fileLength = contentLength.getLength(file);
        Assertions.assertEquals(file.length(), fileLength);
    }

    @Test
    public void length_function() {
        ContentLength.Builder builder = ContentLength.newBuilder();
        builder.addFunction(new ContentLength.LengthFunction() {
            @Override
            public long getLength(Object context) {
                if (context instanceof long[]) {
                    return ((long[]) context).length;
                }
                return ContentLength.SKIP;
            }
        });

        ContentLength contentLength = builder.build();

        int length = contentLength.getLength(new long[10]);
        Assertions.assertEquals(10, length);
    }

    @Test
    public void length_unknownType() {
        ContentLength.Builder builder = ContentLength.newBuilder();
        ContentLength contentLength = builder.build();

        int length = contentLength.getLength("abc");
        Assertions.assertEquals(ContentLength.NOT_EXIST, length);

        int nullLength = contentLength.getLength(null);
        Assertions.assertEquals(ContentLength.NOT_EXIST, nullLength);
    }


    @Test
    public void overflow_file() {
        ContentLength.Builder builder = ContentLength.newBuilder();
        builder.addContentType(File.class);
        ContentLength content = builder.build();

        File file = mock(File.class);
        when(file.length()).thenReturn(Long.MAX_VALUE);

        int intLength = content.getLength(file);
        Assertions.assertEquals(Integer.MAX_VALUE, intLength);

        long longLength = content.getLongLength(file);
        Assertions.assertEquals(Long.MAX_VALUE, longLength);
    }
}