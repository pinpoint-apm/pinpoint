/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.web.service;

import com.navercorp.pinpoint.uristat.web.model.UriStatSummary;
import com.navercorp.pinpoint.uristat.web.util.UriStatSummaryQueryParameter;

import java.util.List;

public interface UriStatSummaryService {
    @Deprecated
    List<UriStatSummary> getUriStatApplicationSummary(UriStatSummaryQueryParameter queryParameter);
    @Deprecated
    List<UriStatSummary> getUriStatAgentSummary(UriStatSummaryQueryParameter queryParameter);
    List<UriStatSummary> getUriStatApplicationPagedSummary(UriStatSummaryQueryParameter queryParameter);
    List<UriStatSummary> getUriStatAgentPagedSummary(UriStatSummaryQueryParameter queryParameter);

}
