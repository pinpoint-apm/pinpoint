/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.aws.sdk.s3;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeProvider;

public class AwsSdkS3Constants {
    public static final ServiceType AWS_SDK_S3 = ServiceTypeProvider.getByName("AWS_SDK_S3");
    public static final ServiceType AWS_SDK_S3_INTERNAL = ServiceTypeProvider.getByName("AWS_SDK_S3_INTERNAL");
}
