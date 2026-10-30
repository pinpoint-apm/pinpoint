/*
 * Copyright 2026 NAVER Corp.
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
 *
 */
package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.Service;

/**
 * Removes the data a module keeps for a service.
 * <p>
 * Listeners run inside the delete transaction, before the service row goes. A listener
 * that throws fails the delete and keeps the service, so a retry can finish the cleanup.
 */
@FunctionalInterface
public interface ServiceDeletionListener {
    void onServiceDelete(Service service);
}
