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
import org.onosproject.orchestrator.dismi.primitives.LambdaSelector;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-10-17.
 */
public class LambdaSelectorValidator extends FieldValidator {

    /*
        LambdaSelector -- > Double lambdaCentre
        LambdaSelector -- > Double lambdaWitdh
    */
    String className = "LambdaSelectorValidator";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, org.onosproject.orchestrator.dismi.primitives.Tracker tracker) {
        LambdaSelector selector = null;
        log.info("Validating and resolving LambdaSelector !");
        if (null == field) {
            log.error("Instance of LambdaSelector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "field");
            return null;
        }
        if (!(field instanceof LambdaSelector)) {
            log.error("Invalid instance of LambdaSelector !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANETHSELECTOR,
                             "field");
            return null;
        }

        selector = (LambdaSelector) field;
        if (null == tracker) {
            log.error("Tracker of LambdaSelector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }
        tracker.addIssue(className,
                         Issue.SeverityEnum.WARNING,
                         Issue.ErrorTypeEnum.NOT_SUPPORTED,
                         "LambdaSelector is not supported yet !");
        log.warn("LambdaSelector is not supported yet !");
        return selector;
    }
}
