/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.grpc.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrpcSenderCompressionTest {

    @Test
    void fromValue_isCaseInsensitive() {
        assertThat(GrpcSenderCompression.fromValue("gzip")).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue("GZIP")).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue(" Gzip ")).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue("none")).isEqualTo(GrpcSenderCompression.NONE);
        assertThat(GrpcSenderCompression.fromValue("NONE")).isEqualTo(GrpcSenderCompression.NONE);
    }

    @Test
    void defaultIsGzip_soAnOlderConfigFileWithoutTheKeyCompressesToo() {
        assertThat(GrpcSenderCompression.defaultCompression()).isEqualTo(GrpcSenderCompression.GZIP);
    }

    @Test
    void fromValue_missingOrBlank_isTheDefault() {
        assertThat(GrpcSenderCompression.fromValue(null)).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue("")).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue("   ")).isEqualTo(GrpcSenderCompression.GZIP);
    }

    @Test
    void fromValue_unknown_isTheDefault_insteadOfFailingTheAgent() {
        assertThat(GrpcSenderCompression.fromValue("zstd")).isEqualTo(GrpcSenderCompression.GZIP);
        assertThat(GrpcSenderCompression.fromValue("gzipp")).isEqualTo(GrpcSenderCompression.GZIP);
    }

    @Test
    void grpcName() {
        assertThat(GrpcSenderCompression.GZIP.getGrpcName()).isEqualTo("gzip");
        assertThat(GrpcSenderCompression.NONE.getGrpcName()).isNull();
    }
}
