/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.thrift.io.TCommandType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class SupportedCommandUtils {
    private SupportedCommandUtils() {
    }

    public static List<TCommandType> newSupportCommandList(List<Integer> supportCommandList) {
        if (CollectionUtils.isEmpty(supportCommandList)) {
            return Collections.emptyList();
        }

        final List<TCommandType> result = new ArrayList<>(supportCommandList.size());
        for (Integer supportCommandCode : supportCommandList) {
            TCommandType commandType = TCommandType.getType(supportCommandCode.shortValue());
            if (commandType != null) {
                result.add(commandType);
            }
        }
        return result;
    }

}
