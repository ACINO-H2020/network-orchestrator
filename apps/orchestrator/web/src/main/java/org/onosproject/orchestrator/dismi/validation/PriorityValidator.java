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

import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class PriorityValidator extends FieldValidator {

    private final Logger log = getLogger(getClass());
    String className = "PriorityValidator";

    public PriorityValidator() {

    }

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        List<Priority> priorityList;
        log.info("Validating and resolving priority list !");
        if (null == field) {
            log.error("Priority list  is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "priority");
            return null;
        }
        priorityList = (List<Priority>) field;

        if (null == tracker) {
            log.error("Invalid tracker of Priority list (null) !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }
        log.info("Successfully validated and resolved Priority list !");
        Object object = validateAndResolveList(priorityList, tracker);
        return object;
    }

    /*
        A priority can only be valid as it contains just one parameter that is an enum
     */
    private Priority validateAndResolvePriority(Priority priority, Tracker tracker) {
        return priority;
    }

    /*
        In a list of priorities, we want to check that each priority is present at most once
     */
    private List<Priority> validateAndResolveList(List<Priority> priorityList, Tracker tracker) {

        List<Priority> resolvedPriorities = new ArrayList<>();
        boolean isAvailable = false;
        boolean isCost = false;
        boolean isLatency = false;
        boolean isJitter = false;
        boolean isBw = false;
        Priority.PriorityEnum pval;

        for (Priority p : priorityList) {
            if (p.getPriority() == Priority.PriorityEnum.AVAILABILITY) {
                if (!isAvailable) {
                    isAvailable = true;
                    log.info("A entry in priority list is '" + p.getPriority() + "'!");
                    resolvedPriorities.add(copyPriority(p));
                }
            } else if (p.getPriority() == Priority.PriorityEnum.COST) {
                if (!isCost) {
                    isCost = true;
                    log.info("*A entry in priority list is '" + p.getPriority() + "'!");
                    resolvedPriorities.add(copyPriority(p));
                }
            } else if (p.getPriority() == Priority.PriorityEnum.LATENCY) {
                if (!isLatency) {
                    isLatency = true;
                    log.info("**A entry in priority list is '" + p.getPriority() + "'!");
                    resolvedPriorities.add(copyPriority(p));
                }
            } else if (p.getPriority() == Priority.PriorityEnum.JITTER) {
                if (!isJitter) {
                    isJitter = true;
                    log.info("***A entry in priority list is '" + p.getPriority() + "'!");
                    resolvedPriorities.add(copyPriority(p));
                }
            } else if (p.getPriority() == Priority.PriorityEnum.BANDWIDTH) {
                if (!isBw) {
                    isBw = true;
                    log.info("****A entry in priority list is '" + p.getPriority() + "'!");
                    resolvedPriorities.add(copyPriority(p));
                }
            }

            if (isAvailable && isCost && isLatency && isJitter && isBw) {
                break;
            }
        }
        return resolvedPriorities;
    }

    private Priority copyPriority(Priority p) {
        Priority pNew = new Priority();
        pNew.setPriority(p.getPriority());
        return pNew;
    }
}
