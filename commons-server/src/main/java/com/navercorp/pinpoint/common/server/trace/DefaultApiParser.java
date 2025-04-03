package com.navercorp.pinpoint.common.server.trace;

import com.google.common.base.Preconditions;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;

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
        Preconditions.checkArgument(methodTypeEnum == MethodTypeEnum.DEFAULT, "Unexpected methodType:%s", methodTypeEnum);

        try {
            ApiDescription apiDescription = parser.parse(apiMetadata);

            String method = apiDescription.getMethodDescription();
            String className = apiDescription.getSimpleClassName();
            String apiInfo = apiDescription.getApiDescription();

            return new Api.Builder(method, className, apiInfo, MethodTypeEnum.DEFAULT)
                    .setLineNumber(apiMetadata.getLineNumber())
                    .setLocation(apiMetadata.getLocation())
                    .build();
        } catch (Exception ignored) {
            // ignore
        }

        String description = apiMetadata.getDescription();
        return new Api.Builder(description, "", description, apiMetadata.getMethodTypeEnum()).build();
    }
}
