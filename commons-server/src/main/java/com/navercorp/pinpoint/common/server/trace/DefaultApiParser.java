package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.util.Assert;

import java.util.Objects;

public class DefaultApiParser implements ApiParser {
    private final ApiDescriptionParser parser;

    public DefaultApiParser(ApiDescriptionParser parser) {
        this.parser = Objects.requireNonNull(parser, "parser");
    }

    @Override
    public Api parse(ApiMetaDataBo apiMetadata) {
        Objects.requireNonNull(apiMetadata, "apiMetadata");

        MethodTypeEnum methodTypeEnum = apiMetadata.getMethodTypeEnum();
        Assert.isTrue(methodTypeEnum == MethodTypeEnum.DEFAULT,
                "Unexpected methodType:" + methodTypeEnum);

        try {
            ApiDescription apiDescription = parser.parse(apiMetadata);

            String method = apiDescription.getMethodDescription();
            String className = apiDescription.getSimpleClassName();
            String apiInfo = apiDescription.getApiDescription();

            return new Api(method, className, apiInfo, MethodTypeEnum.DEFAULT);
        } catch (Exception ignore) {
            // ignore
        }

        String description = apiMetadata.getDescription();
        return new Api(description, "", description, apiMetadata.getMethodTypeEnum());
    }
}
