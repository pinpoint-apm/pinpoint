/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import org.apache.hadoop.hbase.client.ResultScanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author emeroad
 * @author minwoo.jung
 */

@ExtendWith(MockitoExtension.class)
public class HbaseTemplateExtractDataIT {

    @Mock
    ResultScanner scanner1;
    @Mock
    ResultScanner scanner2;
    @Mock
    ResultScanner scanner3;

    @Test
    public void extractData_close() {
        ResultsExtractor<Integer> action = new ResultsExtractor<>() {
            private int i;
            @Override
            public Integer extractData(ResultScanner results) throws Exception {
                return i++;
            }
        };
        ResultScanner[] scanners = {scanner1, scanner2, scanner3};

        HbaseTemplate.extractData(scanners, action);

        verify(scanner1).close();
        verify(scanner2).close();
        verify(scanner3).close();
    }

    @Test
    public void extractData_error() throws IOException {
        ResultsExtractor<Integer> action = new ResultsExtractor<>() {
            private int i;
            @Override
            public Integer extractData(ResultScanner results) throws Exception {
                if (i >= 1) {
                    throw new Exception("error:" + i);
                }
                results.next(); // touch

                return i++;
            }
        };
        ResultScanner[] scanners = {scanner1, scanner2, scanner3};

        HbaseSystemException error = Assertions.assertThrowsExactly(HbaseSystemException.class, () -> {
            HbaseTemplate.extractData(scanners, action);
        });

        verify(scanner1).close();
        verify(scanner2).close();
        verify(scanner3).close();

        verify(scanner1).next();
        verify(scanner2, never()).next();
        verify(scanner3, never()).next();

        Assertions.assertEquals("error:1", error.getCause().getMessage());
    }
}
