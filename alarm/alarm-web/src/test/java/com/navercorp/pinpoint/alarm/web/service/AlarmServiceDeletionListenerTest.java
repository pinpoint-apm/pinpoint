/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.web.service;

import com.navercorp.pinpoint.alarm.dao.AlarmNotificationChannelDao;
import com.navercorp.pinpoint.alarm.dao.AlarmRuleV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmTemplateDao;
import com.navercorp.pinpoint.alarm.service.AlarmRuleDeleter;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import com.navercorp.pinpoint.common.server.uid.Service;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlarmServiceDeletionListenerTest {

    private final AlarmTemplateDao templateDao = mock(AlarmTemplateDao.class);
    private final AlarmRuleV2Dao ruleDao = mock(AlarmRuleV2Dao.class);
    private final AlarmRuleDeleter ruleDeleter = mock(AlarmRuleDeleter.class);
    private final AlarmNotificationChannelDao channelDao = mock(AlarmNotificationChannelDao.class);
    private final AlarmTemplateService templateService = mock(AlarmTemplateService.class);
    private final AlarmChannelService channelService = mock(AlarmChannelService.class);

    private final AlarmServiceDeletionListener listener = new AlarmServiceDeletionListener(
            templateDao, ruleDao, ruleDeleter, channelDao, templateService, channelService);

    @Test
    void locksHeadersThenRulesThenChannelsBeforeDeleting() {
        when(templateDao.selectByService("svc")).thenReturn(List.of(template(9L), template(3L)));
        when(templateDao.selectByIdForUpdate(3L)).thenReturn(template(3L));
        when(templateDao.selectByIdForUpdate(9L)).thenReturn(template(9L));
        when(ruleDao.selectRuleIdsByServiceForUpdate("svc")).thenReturn(List.of(11L, 12L));
        when(channelDao.selectIdsByServiceNameForUpdate("svc")).thenReturn(List.of(4L));

        listener.onServiceDelete(new Service("svc", 1));

        InOrder order = inOrder(templateDao, ruleDao, ruleDeleter, channelDao, templateService, channelService);
        order.verify(templateDao).selectByIdForUpdate(3L);
        order.verify(templateDao).selectByIdForUpdate(9L);
        order.verify(ruleDao).selectRuleIdsByServiceForUpdate("svc");
        order.verify(channelDao).selectIdsByServiceNameForUpdate("svc");
        order.verify(ruleDeleter).deleteRules(List.of(11L, 12L));
        order.verify(templateService).markTemplateDeleted(3L);
        order.verify(templateService).markTemplateDeleted(9L);
        order.verify(channelService).removeChannel(4L);
    }

    @Test
    void skipsTemplateDeletedSinceTheUnlockedRead() {
        when(templateDao.selectByService("svc")).thenReturn(List.of(template(3L)));
        when(templateDao.selectByIdForUpdate(3L)).thenReturn(null);
        when(ruleDao.selectRuleIdsByServiceForUpdate("svc")).thenReturn(List.of());
        when(channelDao.selectIdsByServiceNameForUpdate("svc")).thenReturn(List.of());

        listener.onServiceDelete(new Service("svc", 1));

        verify(templateService, never()).markTemplateDeleted(3L);
    }

    private static AlarmTemplate template(Long id) {
        AlarmTemplate template = new AlarmTemplate();
        template.setId(id);
        template.setServiceName("svc");
        return template;
    }
}
