package com.navercorp.pinpoint.web.uid.controller;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.service.BaseApplicationUidService;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.vo.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/uid/copy")
@ConditionalOnProperty(name = "pinpoint.modules.uid.enabled", havingValue = "true")
public class ApplicationUidCopyController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final CommonService commonService;
    private final BaseApplicationUidService baseApplicationUidService;

    public ApplicationUidCopyController(CommonService commonService, BaseApplicationUidService baseApplicationUidService) {
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.baseApplicationUidService = Objects.requireNonNull(baseApplicationUidService, "baseApplicationUidService");
    }

    @GetMapping(value = "")
    public ResponseEntity<String> copyApplicationUid() {
        StopWatch stopWatch = new StopWatch("syncApplicationUid");
        stopWatch.start("selectAllApplicationNames");
        List<Application> applications = commonService.selectAllApplicationNames();
        stopWatch.stop();

        List<ApplicationUidAttribute> beforeInsert = List.of();
        if (logger.isInfoEnabled()) {
            beforeInsert = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
        }

        stopWatch.start("getOrCreateApplicationUid");
        for (Application application : applications) {
            baseApplicationUidService.getOrCreateApplicationUid(ServiceUid.DEFAULT, application.getName(), application.getServiceTypeCode());
        }
        stopWatch.stop();

        if (logger.isInfoEnabled()) {
            stopWatch.start("!Debug baseApplicationUidService.getApplications");
            List<ApplicationUidAttribute> afterInsert = baseApplicationUidService.getApplications(ServiceUid.DEFAULT);
            stopWatch.stop();
            logger.info("syncApplicationUid total:{}, time taken: {} ms, before:{} after: {}", applications.size(), stopWatch.getTotalTimeMillis(), beforeInsert.size(), afterInsert.size());
        }
        logger.info(stopWatch.prettyPrint());
        return ResponseEntity.ok("OK");
    }
}
