/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.realtime;

import com.navercorp.pinpoint.pubsub.endpoint.PubSubFluxServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubMonoServiceDescriptor;
import com.navercorp.pinpoint.pubsub.endpoint.PubSubServiceDescriptor;
import com.navercorp.pinpoint.realtime.dto.ATCDemand;
import com.navercorp.pinpoint.realtime.dto.ATCSupply;
import com.navercorp.pinpoint.realtime.dto.ATDDemand;
import com.navercorp.pinpoint.realtime.dto.ATDSupply;
import com.navercorp.pinpoint.realtime.dto.Echo;

/**
 * @author youngjin.kim2
 */
public class RealtimePubSubServiceDescriptors {

    public static final PubSubFluxServiceDescriptor<ATCDemand, ATCSupply> ATC =
            PubSubServiceDescriptor.flux("atc", ATCDemand.class, ATCSupply.class);

    public static final PubSubMonoServiceDescriptor<ATDDemand, ATDSupply> ATD =
            PubSubServiceDescriptor.mono("atd", ATDDemand.class, ATDSupply.class);

    public static final PubSubMonoServiceDescriptor<Echo, Echo> ECHO =
            PubSubServiceDescriptor.mono("echo", Echo.class, Echo.class);

}
