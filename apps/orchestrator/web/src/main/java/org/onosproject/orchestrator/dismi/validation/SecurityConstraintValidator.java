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
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-10-06.
 */
public class SecurityConstraintValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        String className = "SecurityConstraintValidator";
        //log.info("Validating and resolving Security constraint list !");
        if (field == null) {
            log.error("Instance of Security constraint is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            tracker.setInvalid();
            return null;
        }

        SecurityConstraint securityConstraint = null;
        if (field instanceof SecurityConstraint) {
            securityConstraint = (SecurityConstraint) field;
        } else {
            log.error("Invalid instance of Security constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONSTRAINT,
                             "");
            tracker.setInvalid();
            return null;
        }
        //log.info("Security constraint successfully validated nad resolved !");
        return securityConstraint;
    }
}
