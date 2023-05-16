package com.navercorp.pinpoint.web.query.controller;

import com.navercorp.pinpoint.web.query.model.BindSqlView;
import com.navercorp.pinpoint.web.query.service.BindType;
import com.navercorp.pinpoint.web.query.service.QueryService;
import com.navercorp.pinpoint.web.query.service.QueryServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class QueryController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final QueryServiceFactory queryServiceFactory;

    public QueryController(QueryServiceFactory queryServiceFactory) {
        this.queryServiceFactory = Objects.requireNonNull(queryServiceFactory, "queryServiceFactory");
    }

    @PostMapping(value = "/bind")
    public BindSqlView metaDataBind(@RequestParam("type") String type,
                                    @RequestParam("metaData") String metaData,
                                    @RequestParam("bind") String bind) {
        if (logger.isDebugEnabled()) {
            logger.debug("POST /bind params {metaData={}, bind={}}", metaData, bind);
        }

        final BindType bindType = BindType.of(type);
        if (bindType == null) {
            throw new IllegalArgumentException("Unknown Type:" + type);
        }

        if (metaData == null) {
            return new BindSqlView("");
        }

        final QueryService service = queryServiceFactory.getService(bindType);
        final String bindedQuery = service.bind(metaData, bind);
        if (logger.isDebugEnabled()) {
            logger.debug("bindedQuery={}", bindedQuery);
        }

        return new BindSqlView(bindedQuery);
    }

}
