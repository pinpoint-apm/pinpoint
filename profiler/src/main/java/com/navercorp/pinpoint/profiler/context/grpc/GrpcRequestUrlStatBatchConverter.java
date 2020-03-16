/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.grpc.trace.PRequestUrlStatBatch;
import com.navercorp.pinpoint.grpc.trace.PRequestUrlStatData;
import com.navercorp.pinpoint.grpc.trace.PRequestUrlStatMetadata;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.monitor.storage.RequestUrlStatInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class GrpcRequestUrlStatBatchConverter implements MessageConverter<PRequestUrlStatBatch> {

    @Override
    public PRequestUrlStatBatch toMessage(Object message) {
        if (message instanceof List) {
            Map<String, Integer> urlMetadata = new HashMap<String, Integer>();
            final List<PRequestUrlStatData> requestUrlStatDataList = new ArrayList<PRequestUrlStatData>();

            for (Object e : (List) message) {
                if (e instanceof RequestUrlStatInfo) {
                    RequestUrlStatInfo requestUrlStatInfo = (RequestUrlStatInfo) e;

                    String url = requestUrlStatInfo.getUrl();
                    Integer urlId = urlMetadata.get(url);

                    if (urlId == null) {
                        urlId = urlMetadata.size() + 1;
                        urlMetadata.put(url, urlId);
                    }

                    PRequestUrlStatData requestsStatData = convertToRequestUrlStatData(requestUrlStatInfo, urlId);
                    requestUrlStatDataList.add(requestsStatData);
                }
            }

            PRequestUrlStatBatch requestUrlStatBatch = convertToRequestUrlStatBatch(urlMetadata, requestUrlStatDataList);
            return requestUrlStatBatch;
        }
        return null;
    }

    private PRequestUrlStatData convertToRequestUrlStatData(RequestUrlStatInfo requestUrlStatInfo, Integer urlId) {
        PRequestUrlStatData.Builder builder = PRequestUrlStatData.newBuilder();
        builder.setUrlId(urlId);
        builder.setStatus(requestUrlStatInfo.getStatus());
        builder.setStartTime(requestUrlStatInfo.getStartTime());
        builder.setElapsedTime(requestUrlStatInfo.getElapsedTime());

        return builder.build();
    }


    private PRequestUrlStatBatch convertToRequestUrlStatBatch(Map<String, Integer> urlMetadata, List<PRequestUrlStatData> requestUrlStatDataList) {
        List<PRequestUrlStatMetadata> requestsStatUrlMetadataList = createUrlMetadataList(urlMetadata);

        PRequestUrlStatBatch.Builder builder = PRequestUrlStatBatch.newBuilder();
        builder.addAllRequestsUrlStatMetadata(requestsStatUrlMetadataList);
        builder.addAllRequestsStatData(requestUrlStatDataList);
        return builder.build();
    }

    private List<PRequestUrlStatMetadata> createUrlMetadataList(Map<String, Integer> urlMetadata) {
        List<PRequestUrlStatMetadata> result = new ArrayList<PRequestUrlStatMetadata>(urlMetadata.size());

        for (Map.Entry<String, Integer> urlMetaDataEntry : urlMetadata.entrySet()) {
            PRequestUrlStatMetadata.Builder urlMetadataBuilder = PRequestUrlStatMetadata.newBuilder();
            urlMetadataBuilder.setUrl(urlMetaDataEntry.getKey());
            urlMetadataBuilder.setId(urlMetaDataEntry.getValue());
            result.add(urlMetadataBuilder.build());
        }

        return result;
    }

}
