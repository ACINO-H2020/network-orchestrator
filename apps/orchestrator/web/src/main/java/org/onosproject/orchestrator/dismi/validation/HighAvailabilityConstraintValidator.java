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

import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-06-28.
 */
public class HighAvailabilityConstraintValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        String className = "HighAvailabilityConstraintValidator";
        //log.info("Validating and resolving HighAvailabilityConstraintValidator !");
        if (field == null) {
            log.error("Instance of Security constraint is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }

        HighAvailabilityConstraint highAvailabilityConstraint = null;
        if (field instanceof HighAvailabilityConstraint) {
            highAvailabilityConstraint = (HighAvailabilityConstraint) field;
        } else {
            log.error("Invalid instance of HighAvailabilityConstraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONSTRAINT,
                             "");
            tracker.setInvalid();
            return null;
        }
        //log.info("High availability constraint successfully validated and resolved !");
        return highAvailabilityConstraint;
    }
}
