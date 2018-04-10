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

package org.onosproject.orchestrator.dismi.negotiation;

import org.onlab.util.Identifier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by aghafoor on 2017-06-01.
 */
public class AlternativeSolsId extends Identifier<Long> {

    // Public construction is prohibited
    private AlternativeSolsId(Long id) {
        super(id);
        checkArgument(id != null && id > 0, "AlternativeSolsId cannot be null or value with 0 !");
    }

    // Default constructor for serialization
    protected AlternativeSolsId() {
        super(0l);
    }

    /*public static AlternativeSolsId getId(String id) {
        return new AlternativeSolsId(id);
    }*/

    /**
     * @param id
     * @return
     */
    public static AlternativeSolsId getId(Long id) {
        return new AlternativeSolsId(id);
    }
}
