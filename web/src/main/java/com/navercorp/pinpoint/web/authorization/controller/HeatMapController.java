package com.navercorp.pinpoint.web.authorization.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.scatter.DragArea;
import com.navercorp.pinpoint.web.scatter.DragAreaQuery;
import com.navercorp.pinpoint.web.scatter.Status;
import com.navercorp.pinpoint.web.scatter.heatmap.HeatMap;
import com.navercorp.pinpoint.web.scatter.heatmap.Point;
import com.navercorp.pinpoint.web.service.HeatMapService;
import com.navercorp.pinpoint.web.util.LimitUtils;
import com.navercorp.pinpoint.web.validation.NullOrNotBlank;
import com.navercorp.pinpoint.web.view.transactionlist.DotMetaDataView;
import com.navercorp.pinpoint.web.view.transactionlist.TransactionDotMetaDataViewModel;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.scatter.Dot;
import com.navercorp.pinpoint.web.vo.scatter.DotMetaData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController()
@RequestMapping("/api/heatmap")
@Validated
public class HeatMapController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final HeatMapService heatMap;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final boolean defaultTraceIndexReadV2;

    public HeatMapController(HeatMapService heatMap, ServiceTypeRegistryService serviceTypeRegistryService,
                             @Value("${pinpoint.web.trace.index.read.v2:false}") boolean defaultTraceIndexReadV2) {
        this.heatMap = Objects.requireNonNull(heatMap, "heatMap");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.defaultTraceIndexReadV2 = defaultTraceIndexReadV2;
    }

    @GetMapping(value = "/drag")
    public ResultView dragScatterArea(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("x1") long x1,
            @RequestParam("x2") long x2,
            @RequestParam("y1") long y1,
            @RequestParam("y2") long y2,
            @RequestParam(value = "agentId", required = false) @NullOrNotBlank String agentId,
            @RequestParam(value = "dotStatus", required = false) Boolean boolDotStatus,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limitParam,
            @RequestParam(value = "traceIndexReadV2", required = false) Optional<Boolean> traceIndexReadV2) {
        final int limit = LimitUtils.checkRange(limitParam);

        final DragArea dragArea = DragArea.normalize(x1, x2, y1, y2);

        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.unchecked(x1, x2);
        logger.debug("drag scatter data. RANGE={}, LIMIT={}", range, limit);
        final Dot.Status dotStatus = toDotStatus(boolDotStatus);
        final DragAreaQuery query = new DragAreaQuery(dragArea, agentId, dotStatus);

        final boolean useTraceIndexV2 = traceIndexReadV2.orElse(defaultTraceIndexReadV2);
        final LimitedScanResult<List<DotMetaData>> dotMetaData;
        if (!useTraceIndexV2) {
            dotMetaData = heatMap.dragScatterDataV2(applicationName, query, limit);
        } else {
            final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
            dotMetaData = heatMap.dragScatterDataV3(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceType.getCode(), query, limit);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("dragScatterArea applicationName:{} dots:{}", applicationName, dotMetaData.scanData().size());
        }

        final List<DotMetaData> scanData = dotMetaData.scanData();
        final TransactionDotMetaDataViewModel transaction = new TransactionDotMetaDataViewModel(scanData);
        final boolean complete = scanData.size() < limit;
        final PagingStatus scanStatus = new PagingStatus(complete, dotMetaData.limitedTime());
        return new ResultView(transaction.getMetadata(), scanStatus);

    }


    private Dot.Status toDotStatus(Boolean dotStatus) {
        if (dotStatus == null) {
            return null;
        }
        if ((Boolean.TRUE.equals(dotStatus))) {
            return Dot.Status.SUCCESS;
        }
        return Dot.Status.FAILED;
    }

    public record ResultView(List<DotMetaDataView> metadata, PagingStatus status) {
        @JsonUnwrapped
        public PagingStatus status() {
            return status;
        }
    }

    public record PagingStatus(boolean complete, long resultFrom) {
    }

    @GetMapping(value = "/get")
    public HeatMapController.HeatMapViewModel getHeatMapData(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "traceIndexReadV2", required = false) Optional<Boolean> traceIndexReadV2) {
        // TODO range check verification exception occurs. "from" is bigger than "to"
        final Range range = Range.unchecked(from, to);
        logger.debug("fetch getHeatMapData. RANGE={}, ", range);

        final boolean useTraceIndexV2 = traceIndexReadV2.orElse(defaultTraceIndexReadV2);

        final LimitedScanResult<HeatMap> scanResult;
        if (!useTraceIndexV2) {
            scanResult = this.heatMap.getHeatMap(applicationName, range, TimeUnit.SECONDS.toMillis(10), LimitUtils.MAX);
        } else {
            final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
            scanResult = this.heatMap.getHeatMapV2(ServiceUid.DEFAULT_SERVICE_UID_CODE, applicationName, serviceType.getCode(), range, TimeUnit.SECONDS.toMillis(10), LimitUtils.MAX);
        }

        final Status status = new Status(System.currentTimeMillis(), range);
        return new HeatMapController.HeatMapViewModel(scanResult.scanData(), status);
    }


    public static class HeatMapViewModel {
        private final HeatMap heatMap;
        private final Status status;

        public HeatMapViewModel(HeatMap heatMap, Status status) {
            this.heatMap = Objects.requireNonNull(heatMap, "heatMap");
            this.status = Objects.requireNonNull(status, "status");
        }

        @JsonProperty("data")
        public List<long[]> getData() {
            List<Point> pointList = heatMap.getData();
            final List<long[]> list = new ArrayList<>(pointList.size());
            for (Point point : pointList) {
                long[] longs = {point.x(), point.y(), point.success(), point.fail()};
                list.add(longs);
            }
            return list;
        }

        public long getSuccess() {
            return heatMap.getSuccess();
        }

        public long getFail() {
            return heatMap.getFail();
        }


        public long getResultFrom() {
            return heatMap.getOldestAcceptedTime();
        }

        public long getResultTo() {
            return heatMap.getLatestAcceptedTime();
        }

        public long[] getXIndex() {
            return heatMap.getXIndex();
        }

        public long getXTick() {
            return heatMap.getXTick();
        }

        public long[] getYIndex() {
            return heatMap.getYIndex();
        }

        public long getYTick() {
            return heatMap.getYTick();
        }


        @JsonUnwrapped
        public Status getStatus() {
            return status;
        }

    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            ServiceType serviceTypeFromCode = serviceTypeRegistryService.findServiceType(serviceTypeCode);
            if (serviceTypeFromCode != null && !ServiceType.UNDEFINED.equals(serviceTypeFromCode)) {
                return serviceTypeFromCode;
            }
        }
        if (serviceTypeName != null) {
            ServiceType serviceTypeFromName = serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
            if (serviceTypeFromName != null && !ServiceType.UNDEFINED.equals(serviceTypeFromName)) {
                return serviceTypeFromName;
            }
        }
        throw new IllegalArgumentException("application serviceType not found. code:" + serviceTypeCode + ", name:" + serviceTypeName);
    }
}
