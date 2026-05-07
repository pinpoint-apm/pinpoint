/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LastRowWithTiesResultsExtractorTest {

    private static final int TIE_OFFSET = 0;
    private static final int TIE_LENGTH = 4;
    private static final RowMapper<Integer> MAPPER = (result, rowNum) -> ByteArrayUtils.bytesToInt(result.getRow(), TIE_LENGTH);

    @Test
    void noTie_underLimit_returnsAll() throws Exception {
        ResultScanner scanner = scannerOf(
                row(1, 100),
                row(2, 200),
                row(3, 300));
        DefaultLastRowHandler<Integer> handler = new DefaultLastRowHandler<>();

        LastRowWithTiesResultsExtractor<Integer> extractor =
                new LastRowWithTiesResultsExtractor<>(MAPPER, 5, TIE_OFFSET, TIE_LENGTH, handler);

        List<Integer> result = extractor.extractData(scanner);

        assertThat(result).containsExactly(100, 200, 300);
        assertThat(handler.getLastRow()).isEqualTo(300);
    }

    @Test
    void noTie_limitReached_stopsAtLimit() throws Exception {
        ResultScanner scanner = scannerOf(
                row(1, 100),
                row(2, 200),
                row(3, 300),
                row(4, 400),
                row(5, 500));
        DefaultLastRowHandler<Integer> handler = new DefaultLastRowHandler<>();

        LastRowWithTiesResultsExtractor<Integer> extractor =
                new LastRowWithTiesResultsExtractor<>(MAPPER, 3, TIE_OFFSET, TIE_LENGTH, handler);

        List<Integer> result = extractor.extractData(scanner);

        assertThat(result).containsExactly(100, 200, 300);
        assertThat(handler.getLastRow()).isEqualTo(300);
    }

    @Test
    void tie_limitReached_drainsTrailingTies() throws Exception {
        // limit reached at tie=3; subsequent tie=3 rows are drained.
        // first tie=4 row breaks the drain and is NOT included.
        ResultScanner scanner = scannerOf(
                row(1, 100),
                row(2, 200),
                row(3, 300), row(3, 301), row(3, 302),
                row(4, 400));
        DefaultLastRowHandler<Integer> handler = new DefaultLastRowHandler<>();

        LastRowWithTiesResultsExtractor<Integer> extractor =
                new LastRowWithTiesResultsExtractor<>(MAPPER, 3, TIE_OFFSET, TIE_LENGTH, handler);

        List<Integer> result = extractor.extractData(scanner);

        assertThat(result).containsExactly(100, 200, 300, 301, 302);
        assertThat(handler.getLastRow()).isEqualTo(302);
    }

    private static byte[] row(int tieKey, int mapped) {
        byte[] row = new byte[TIE_LENGTH + 4];
        ByteArrayUtils.writeInt(tieKey, row, 0);
        ByteArrayUtils.writeInt(mapped, row, TIE_LENGTH);
        return row;
    }

    private static ResultScanner scannerOf(byte[]... rows) {
        List<Result> results = new ArrayList<>(rows.length);
        for (byte[] row : rows) {
            Result r = mock(Result.class);
            when(r.getRow()).thenReturn(row);
            results.add(r);
        }
        ResultScanner scanner = mock(ResultScanner.class);
        when(scanner.iterator()).thenReturn(results.iterator());
        return scanner;
    }
}