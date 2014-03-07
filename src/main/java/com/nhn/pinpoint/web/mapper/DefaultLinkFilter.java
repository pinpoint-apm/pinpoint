package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.web.vo.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class DefaultLinkFilter implements LinkFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Application callerApplication;
    private final Application calleeApplication;

    public DefaultLinkFilter(Application callerApplication, Application calleeApplication) {
        if (callerApplication == null) {
            throw new NullPointerException("callerApplication must not be null");
        }
        if (calleeApplication == null) {
            throw new NullPointerException("calleeApplication must not be null");
        }
        this.callerApplication = callerApplication;
        this.calleeApplication = calleeApplication;
    }

    public boolean filter(String foundApplicationName, short foundServiceType) {
        if (this.calleeApplication.getServiceType().isWas() && this.callerApplication.getServiceType().isWas()) {
            logger.debug("check was to was.");
            // src가 같지 않으면 버림.
            if (!this.callerApplication.getName().equals(foundApplicationName) || this.callerApplication.getServiceTypeCode() != foundServiceType) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,1, DIFFERENT SRC. fetched={}/{} , params={}", foundApplicationName, ServiceType.findServiceType(foundServiceType), calleeApplication);
                }
                return true;
            }
        } else if (this.callerApplication.getServiceType().isUser()) {
            logger.debug("check client to was");
            // dest가 해당 was가 아니면 버림.
            if (!this.calleeApplication.getName().equals(foundApplicationName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("  DROP THE ROW,2, DIFFERENT DEST. fetched={}/{}, params={}", foundApplicationName, ServiceType.findServiceType(foundServiceType), this.calleeApplication);
                }
                return true;
            }
        } else {
            logger.debug("check any to any.");
            // dest가 같지 않으면 버림.
            if (this.calleeApplication.getServiceType().isUnknown()) {
                // dest가 unknown인 경우 application name만 비교.
                // TODO 다른 좋은 비교 방법 없을까??
                if (!this.calleeApplication.getName().equals(foundApplicationName)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,3, DIFFERENT DEST. fetched={}/{}, params={}", foundApplicationName, ServiceType.findServiceType(foundServiceType), calleeApplication);
                    }
                    return true;
                }
            } else {
                // dest가 unknown이 아니면 applicaiton name, type 둘 다 비교.
                if (!this.calleeApplication.getName().equals(foundApplicationName) || this.calleeApplication.getServiceType().getCode() != foundServiceType) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("  DROP THE ROW,4, DIFFERENT DEST. fetched={}/{}, params={}", foundApplicationName, ServiceType.findServiceType(foundServiceType), this.calleeApplication);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
