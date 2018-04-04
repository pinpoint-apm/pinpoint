/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.openwhisk.aspect;

import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import scala.util.Try;


@Aspect
public abstract class CompletionMessageAspect {

    @PointCut
    public Try parse(String msg) {
        if (msg.charAt(0) == OpenwhiskConstants.PINPOINT_HEADER_DELIMITIER_ASCII) {
            return __parse(msg.substring(msg.lastIndexOf(OpenwhiskConstants.PINPOINT_HEADER_POSTFIX)
                    + OpenwhiskConstants.PINPOINT_HEADER_POSTFIX_LENGTH, msg.length()));
        }
        return __parse(msg);
    }

    @JointPoint
    abstract Try __parse(String msg);

}
