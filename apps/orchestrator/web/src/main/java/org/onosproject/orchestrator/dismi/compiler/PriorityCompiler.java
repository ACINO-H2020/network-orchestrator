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

package org.onosproject.orchestrator.dismi.compiler;

import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-03-29.
 */
public class PriorityCompiler {
    private final Logger log = getLogger(getClass());

    public List<Priority> toAciPriority(List<Priority> priorities) {
        //ToDo: Currently ONOS does not support it, lets recheck with Michale
        log.info("Compiling priorities !");
        List<Priority> constraints = new ArrayList<Priority>();
        for (Priority priority : priorities) {
            Priority.PriorityEnum priorityEnum = priority.getPriority();
            switch (priorityEnum) {
                case AVAILABILITY:
                    break;
                case COST:
                    break;
                case LATENCY:
                    //constraints.add(new LatencySensitive());
                    break;
                case JITTER:
                    break;
                case BANDWIDTH:
                    break;
            }
        }
        return constraints;
    }
}
