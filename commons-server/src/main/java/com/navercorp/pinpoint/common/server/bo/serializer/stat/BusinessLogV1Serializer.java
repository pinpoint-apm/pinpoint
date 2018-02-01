/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.bo.serializer.stat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.navercorp.pinpoint.common.server.bo.codec.stat.BusinessLogEncoder;
import com.navercorp.pinpoint.common.server.bo.stat.BusinessLogV1Bo;

/**
 * [XINGUANG]
 */
@Component
public class BusinessLogV1Serializer extends BusinessLogSerializer<BusinessLogV1Bo>{

	@Autowired
	protected BusinessLogV1Serializer(BusinessLogEncoder encoder) {
		super(encoder);
		// TODO Auto-generated constructor stub
	}

	

}
