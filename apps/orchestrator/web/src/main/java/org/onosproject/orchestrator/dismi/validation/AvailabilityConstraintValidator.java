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

import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.AvailabilityConstraintExtended;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class AvailabilityConstraintValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());
    /*
        private String mttr = null;
        private String mtbf = null;
        private String availability = null;
     */

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        String className = "AvailabilityConstraint";
        String toValidate = null;
        log.info("Validating and resolving availability constraint !");
        AvailabilityConstraintExtended availabilityConstraintExtended = null;
        InputAssertion inputAssertion = new InputAssertion();
        if (field == null) {
            log.error("Availability constraint instance is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            tracker.setInvalid();
            return null;
        }
        AvailabilityConstraint availabilityConstraint = null;
        if (field instanceof AvailabilityConstraint) {
            availabilityConstraint = (AvailabilityConstraint) field;
        } else {
            log.error("Invalid availability constraint instance !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONSTRAINT,
                             "");
            tracker.setInvalid();
            return null;
        }
        availabilityConstraintExtended = new AvailabilityConstraintExtended(availabilityConstraint);
        toValidate = availabilityConstraint.getAvailability();
        if (null == toValidate) {
            log.error("Availability constraint value is not specified !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "Availability = " + toValidate);

            return null;
        }


        toValidate = inputAssertion.assertIntent(toValidate);
        toValidate = toValidate.trim();
        Double percent = inputAssertion.validatePercentage(toValidate);
        if (percent < 0) {
            log.error("Availability constraint value is below limit (should be between 0-100)!");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "");
            tracker.setInvalid();
            return null;
        }
        log.info("'Availability' in availability constraint resolved !");
        availabilityConstraintExtended.setAvailabilityExt(percent);

        toValidate = availabilityConstraint.getMtbf();
        if (null == toValidate) {
            log.warn("Could not find 'MTBF' value in availability constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "MTBF = " + toValidate);

        }
        if (null != toValidate) {
            toValidate = inputAssertion.assertIntent(toValidate);
            toValidate = toValidate.trim();

            try {
                Double numValue = inputAssertion.resolveValue(toValidate, InputAssertion.Type.TIME);
                if (numValue < 0) {
                    log.error("Availability constraint value of MTBF is not correct !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.VALUECANNOTBENEGATIVE,
                                     "");

                    return null;
                }
                log.info("'MTBF' in availability constraint resolved !");
                availabilityConstraintExtended.setMtbfExt(numValue);
            } catch (Exception exp) {
                log.error("Availability constraint value of MTBF is not correct !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "");

                return null;
            }
        }

        toValidate = availabilityConstraint.getMttr();
        if (null == toValidate) {
            log.warn("1- Availability constraint value of MTTR is not correct !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "MTTR = " + toValidate);
            //return null;
        }
        if (null != toValidate) {
            toValidate = inputAssertion.assertIntent(toValidate);
            toValidate = toValidate.trim();

            try {
                Double numValue = inputAssertion.resolveValue(toValidate, InputAssertion.Type.TIME);
                if (numValue < 0) {
                    log.error("2- Availability constraint value of MTTR is not correct !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.VALUECANNOTBENEGATIVE,
                                     "");
                    return null;
                }
                log.info("'MTTR' in availability constraint resolved !");
                availabilityConstraintExtended.setMttrExt(numValue);
            } catch (Exception exp) {
                log.error("3- Availability constraint value of MTTR is not correct !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "");
                return null;
            }
        }
        log.info("'Availability constraint successfully resolved !");
        return availabilityConstraintExtended;
    }
}
