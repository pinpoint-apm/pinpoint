package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultApiParserTest {
    @Test
    public void testParse() {
        ApiDescriptionParser apiDescriptionParser = new ApiDescriptionParser();
        DefaultApiParser dut = new DefaultApiParser(apiDescriptionParser);
        ApiMetaDataBo expected = new ApiMetaDataBo.Builder("express-node-sample-id", 1669280767548L, 12, 169, MethodTypeEnum.DEFAULT, "express.Function.proto.get(path, callback)")
                .setLocation("/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js")
                .build();

        Api actual = dut.parse(expected);

        assertThat(actual).extracting("method", "className", "description", "methodTypeEnum", "lineNumber", "location")
                .contains("get(path, callback)", "proto", "express.Function.proto.get(path, callback)", MethodTypeEnum.DEFAULT, 169, "/Users/workspace/pinpoint/@pinpoint-naver-apm/pinpoint-agent-node/samples/express/src/routes/index.js");
    }
}
