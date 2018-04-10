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

import org.onosproject.orchestrator.dismi.api.NotFoundException;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public interface ConnectionpointApiService {

    public abstract Response connectionpointGet()
            throws NotFoundException;

    public abstract Response connectionpointNameGet(String name)
            throws NotFoundException;

    public abstract Response connectionpointNamePut(String name, String newName)
            throws NotFoundException;

    public abstract Response connectionpointNamePut(String name, ConnectionPointExtended pointExtended)
            throws NotFoundException;

    public abstract Response connectionpointPost(ConnectionPointExtended pointExtended)
            throws NotFoundException;

    public abstract Response connectionpointNameDelete(String name)
            throws NotFoundException;


}
