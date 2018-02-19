package com.navercorp.pinpoint.plugin.openwhisk.aspect;

import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.interceptor.PinpointHeaderAccessor;
import scala.util.Try;
import whisk.core.connector.ActivationMessage;


@Aspect
public abstract class InvokeReactiveAspect {

    @PointCut
    public Try parse(String msg) {
        if (msg.charAt(0) == OpenwhiskConstants.PINPOINT_HEADER_DELIMITIER_ASCII) {
            Try t = __parse(msg.substring(msg.lastIndexOf(OpenwhiskConstants.PINPOINT_HEADER_POSTFIX)
                    + OpenwhiskConstants.PINPOINT_HEADER_POSTFIX_LENGTH, msg.length()));
            if (t.isSuccess()) {
                ActivationMessage m = (ActivationMessage) t.get();
                ((PinpointHeaderAccessor) m.transid())._$PINPOINT$_setPinpointHeader(msg.substring(OpenwhiskConstants.PINPOINT_HEADER_PREFIX_LENGTH,
                        msg.indexOf(OpenwhiskConstants.PINPOINT_HEADER_POSTFIX)));
            }
            return t;
        }
        return __parse(msg);
    }

    @JointPoint
    abstract Try __parse(String msg);

}
