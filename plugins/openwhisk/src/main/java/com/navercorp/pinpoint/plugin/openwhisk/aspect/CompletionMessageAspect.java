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
