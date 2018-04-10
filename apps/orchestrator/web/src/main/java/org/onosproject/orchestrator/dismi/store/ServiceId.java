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

package org.onosproject.orchestrator.dismi.store;

import org.onlab.util.Identifier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by stephane on 10/25/16.
 */
public class ServiceId extends Identifier<String> {

    // Public construction is prohibited
    private ServiceId(String id) {
        super(id);
        checkArgument(id != null && id.length() > 0, "Service ID cannot be null or empty");
    }

    // Default constructor for serialization
    protected ServiceId() {
        super("");
    }

    /**
     * Creates a service id using the supplied backing id.
     *
     * @param id service id
     * @return service identifier
     */
    public static ServiceId getId(String id) {
        return new ServiceId(id);
    }

}
