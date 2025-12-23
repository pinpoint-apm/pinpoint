/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.scatter.ScatterView;
import com.navercorp.pinpoint.web.scatter.Status;
import com.navercorp.pinpoint.web.service.ScatterChartService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.view.transactionlist.TransactionMetaDataViewModel;
import com.navercorp.pinpoint.web.vo.GetTraceInfo;
import com.navercorp.pinpoint.web.vo.GetTraceInfoParser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
@RestController
@RequestMapping("/api")
@Validated
public class ScatterChartController implements AccessDeniedExceptionHandler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ScatterChartService scatterChartService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final boolean defaultTraceIndexReadV2;

    private final GetTraceInfoParser getTraceInfoParser = new GetTraceInfoParser();

    public ScatterChartController(
            ScatterChartService scatterChartService,
            ServiceTypeRegistryService serviceTypeRegistryService,
            @Value("${pinpoint.web.trace.index.read.v2:false}") boolean defaultTraceIndexReadV2) {
        this.scatterChartService = Objects.requireNonNull(scatterChartService, "scatterChartService");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.defaultTraceIndexReadV2 = defaultTraceIndexReadV2;
    }


    /**
     * selected points from scatter chart data query
     *
     * @param requestParam requestParam
     * @return TransactionMetaDataViewModel
     */
    @PostMapping(value = "/transactionmetadata")
    public TransactionMetaDataViewModel postTransactionMetadata(@RequestParam Map<String, String> requestParam) {
        final List<GetTraceInfo> selectTraceInfoList = this.getTraceInfoParser.parse(requestParam);

        if (CollectionUtils.isEmpty(selectTraceInfoList)) {
            return new TransactionMetaDataViewModel();
        }

        final List<SpanBo> metadata = scatterChartService.selectTransactionMetadata(selectTraceInfoList);
        return new TransactionMetaDataViewModel(metadata);
    }

    /**
     * @param applicationName applicationName
     * @param from            from
     * @param to              to
     * @param limitParam      max number of data return. if the requested data exceed this limit, we need
     *                        additional calls to fetch the rest of the data
     * @return ScatterView.ResultView
     */
    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/getScatterData")
    public ScatterView.ResultView getScatterData(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam("xGroupUnit") @Positive int xGroupUnit,
            @RequestParam("yGroupUnit") @Positive int yGroupUnit,
            @RequestParam("limit") int limitParam,
            @RequestParam(value = "backwardDirection", required = false, defaultValue = "true")
                    boolean backwardDirection,
            @RequestParam(value = "traceIndexReadV2", required = false) Optional<Boolean> traceIndexReadV2) {
        final int limit = LimitUtils.checkRange(limitParam);

        // TODO: range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.unchecked(from, to);
        logger.debug(
                "fetch scatter data. RANGE: {}, X-Group-Unit: {}, Y-Group-Unit: {}, LIMIT: {}, " +
                        "BACKWARD_DIRECTION: {}",
                range, xGroupUnit, yGroupUnit, limit, backwardDirection
        );
        final boolean useTraceIndexV2 = traceIndexReadV2.orElse(defaultTraceIndexReadV2);

        final ScatterData scatterData;
        if (!useTraceIndexV2) {
            scatterData = scatterChartService.selectScatterData(applicationName, range, xGroupUnit, yGroupUnit, limit, backwardDirection);
        } else {
            final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
            scatterData = scatterChartService.selectScatterDataV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceType.getCode(), range, xGroupUnit, yGroupUnit, limit, backwardDirection);
        }
        final boolean requestComplete = scatterData.getDotSize() < limit;
        ScatterView.DotView dotView = new ScatterView.DotView(scatterData, requestComplete);
        return wrapScatterResultView(range, dotView);
    }

    private static ScatterView.ResultView wrapScatterResultView(Range range, ScatterView.DotView dotView) {
        final Status status = new Status(System.currentTimeMillis(), range);
        return ScatterView.wrapResult(dotView, status);
    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            ServiceType serviceTypeFromCode = serviceTypeRegistryService.findServiceType(serviceTypeCode);
            if (!ServiceType.UNDEFINED.equals(serviceTypeFromCode) && serviceTypeFromCode != null) {
                return serviceTypeFromCode;
            }
        }
        if (serviceTypeName != null) {
            ServiceType serviceTypeFromName = serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
            if (!ServiceType.UNDEFINED.equals(serviceTypeFromName) && serviceTypeFromName != null) {
                return serviceTypeFromName;
            }
        }
        throw new IllegalArgumentException("application serviceType not found. code:" + serviceTypeCode + ", name:" + serviceTypeName);
    }
}
