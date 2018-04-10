/*
 * Copyright (c) 2018 ACINO Consortium
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

package org.onosproject.orchestrator.dismi.validation;

import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;

/**
 * Created by stephane on 10/10/16.
 */
public interface DismiValidationServiceIface {
    Resource submitNewService(Service s);

    Resource submitServiceUpdate(String serviceId, Service service);

    Resource submitIntentUpdate(String serviceId, String intentId, Intent intent);

    boolean deleteService(String serviceId);

    boolean deleteIntent(String serviceId, String intentId);

}
