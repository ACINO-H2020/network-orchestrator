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

package org.onosproject.orchestrator.dismi;

import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

/**
 * Created by aghafoor on 2016-11-16.
 */

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public interface ServiceApiService {

    public abstract Response serviceGet()
            throws NotFoundException;

    public abstract Response servicePost(Service service)
            throws NotFoundException;

    public abstract Response serviceServiceIdDelete(String serviceId)
            throws NotFoundException;

    public abstract Response serviceServiceIdGet(String serviceId)
            throws NotFoundException;

    //public abstract Response resourceServiceIdGet(String serviceId)
    //        throws NotFoundException;

    public abstract Response serviceServiceIdIntentIdDelete(String serviceId, String intentId)
            throws NotFoundException;

    public abstract Response serviceServiceIdIntentIdGet(String serviceId, String intentId)
            throws NotFoundException;

    public abstract Response serviceServiceIdIntentIdPut(String serviceId, String intentId, Intent intent)
            throws NotFoundException;

    public abstract Response serviceServiceIdPut(String serviceId, Service service)
            throws NotFoundException;

    public abstract Response getAlternativeSolitions(DismiIntentId intentId)
            throws NotFoundException;

}
