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

import org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.BandwidthConstraintExtended;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class BandwidthConstraintValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        BandwidthConstraint bandwidthConstraint = null;
        BandwidthConstraintExtended bandwidthConstraintExtended = null;
        double bitrate = 0;
        String className = "BandwidthConstraintValidator";
        //log.info("Validating and resolving bandwidth constraint !");
        if (null == field) {
            log.error("Bandwidth constraint instance is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }

        //  Validate the object type
        if (field instanceof BandwidthConstraint) {
            bandwidthConstraint = (BandwidthConstraint) field;
        } else {
            log.error("Invalid Bandwidth constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTABANDWIDTHCONSTRAINT,
                             "");
            return null;
        }
        String bitrateStr = bandwidthConstraint.getBitrate();
        if (null == bitrateStr) {
            log.error("Bandwidth value is not specified in Bandwidth constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.BITRATEISNULL,
                             "");
            return null;
        }
        InputAssertion inputAssertion = new InputAssertion();
        bitrateStr = inputAssertion.assertIntent(bitrateStr);
        bitrateStr = bitrateStr.trim();
        try {
            //log.info("Resolving and validating bandwidth value and its unit !");
            bitrate = inputAssertion.resolveValue(bitrateStr, InputAssertion.Type.BANDWIDTH);
            if (bitrate < 0) {
                log.error("Invalid 'Bandwidth' value in Bandwidth constraint !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.VALUECANNOTBENEGATIVE,
                                 "bitrate=" + bandwidthConstraint.getBitrate());
                return null;
            }
        } catch (Exception exp) {
            log.error("*Invalid 'Bandwidth' value in Bandwidth constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "bitrate=" + bandwidthConstraint.getBitrate());
            return null;
        }
        bandwidthConstraintExtended = new BandwidthConstraintExtended(bandwidthConstraint);
        bandwidthConstraintExtended.setBitrateExt(bitrate);
        //log.info("Bandwidth constraint successfully resolved !");
        return bandwidthConstraintExtended;
    }
}
