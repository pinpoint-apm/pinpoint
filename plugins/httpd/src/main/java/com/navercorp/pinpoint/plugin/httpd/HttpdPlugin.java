package com.navercorp.pinpoint.plugin.httpd;

import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * Created by chenguoxi on 7/18/16.
 */
public class HttpdPlugin implements ProfilerPlugin, TransformTemplateAware {

//    private TransformTemplate transformTemplate;

    /*
     * (non-Javadoc)
     *
     * @see com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin#setUp(com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext)
     */
    @Override
    public void setup(ProfilerPluginSetupContext context) {
        return;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
//        this.transformTemplate = transformTemplate;
        return;
    }
}

