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

import org.onosproject.orchestrator.dismi.primitives.DelayConstraint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.DelayConstraintExtended;
import org.onosproject.orchestrator.dismi.validation.InputAssertion.Type;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class DelayConstraintValidator extends FieldValidator {

    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        //log.info("Validating and resolving delay constraint !");
        DelayConstraint delayConstraint = null;
        DelayConstraintExtended delayConstraintExtended = null;

        String className = "DelayConstraintValidator";

        if (null == field) {
            log.error("DelayConstraint instance is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }

        //  Validate the object type
        if (field instanceof DelayConstraint) {
            delayConstraint = (DelayConstraint) field;

        } else {
            log.error("Invalid instance of DelayConstraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTADELAYCONSTRAINT,
                             "");
            return null;
        }
        delayConstraintExtended = new DelayConstraintExtended(delayConstraint);
        InputAssertion inputAssertion = null;
        String jitterStr = delayConstraint.getJitter();
        boolean isJitterExists = false;
        boolean isLatencyExists = false;
        if (null == jitterStr) {
            log.warn("Jitter value is DelayConstraint is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.JITTERISNULL,
                             "");
            isJitterExists = false;
        } else {
            log.info("Jitter value is being validated and resolved in DelayConstraint !");
            inputAssertion = new InputAssertion();
            jitterStr = inputAssertion.assertIntent(jitterStr);
            isJitterExists = true;
            jitterStr = jitterStr.trim();
            try {
                double numValue = inputAssertion.resolveValue(jitterStr, Type.TIME);
                if (numValue < 0) {
                    log.error("Invalid Jitter value in DelayConstraint !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.VALUECANNOTBENEGATIVE,
                                     jitterStr);
                    return null;
                }
                delayConstraintExtended.setJitterExt(new Double(numValue));
            } catch (Exception exp) {
                log.error("*Invalid Jitter value in DelayConstraint !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 jitterStr);

                return null;
            }
        }
        String latencyStr = delayConstraint.getLatency();

        if (null == latencyStr) {
            log.warn("Latency value is DelayConstraint is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.LATENCYISNULL,
                             "");
            isLatencyExists = false;
        } else {
            //log.info("Latency value is being validated and resolved in DelayConstraint !");
            inputAssertion = new InputAssertion();
            latencyStr = inputAssertion.assertIntent(latencyStr);
            isLatencyExists = true;
            latencyStr = latencyStr.trim();
            try {
                double numValue = inputAssertion.resolveValue(latencyStr, InputAssertion.Type.TIME);
                if (numValue < 0) {
                    log.error("Invalid Latency value in DelayConstraint !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.VALUECANNOTBENEGATIVE,
                                     latencyStr);
                    return null;
                }
                delayConstraintExtended.setLatencyExt(new Double(numValue));
            } catch (Exception exp) {
                log.error("*Invalid Latency value in DelayConstraint !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 latencyStr);
                return null;
            }
        }
        if (isJitterExists || isLatencyExists) {
            //log.info("DelayConstraint successfully validated and resolved !");
            return delayConstraintExtended;
        } else {
            log.error("Problems when validating and resolving DelayConstraint !");
            return null;
        }
    }
}
