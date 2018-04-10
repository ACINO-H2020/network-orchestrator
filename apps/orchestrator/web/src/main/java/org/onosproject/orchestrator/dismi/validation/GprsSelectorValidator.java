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

import org.onosproject.orchestrator.dismi.primitives.GprsSelector;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-10-17.
 */
public class GprsSelectorValidator extends FieldValidator {

    /*
     * GPRSSelector-> private Integer gtptEid ;
     * Reference: https://en.wikipedia.org/wiki/GPRS_Tunnelling_Protocol
     *
     */
    private final Logger log = getLogger(getClass());
    String className = "GPRSSelectorValidator";

    @Override
    public Object validateAndResolve(Object field, org.onosproject.orchestrator.dismi.primitives.Tracker tracker) {
        GprsSelector selector = null;
        log.info("Validating and resolving Gprs Selector !");
        if (null == field) {
            log.error("Gprs Selector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "field");
            return null;
        }
        if (!(field instanceof GprsSelector)) {
            log.error("Invalid Gprs Selector instance !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANETHSELECTOR,
                             "field");
            return null;
        }

        selector = (GprsSelector) field;
        if (null == tracker) {
            log.error("Tracker of Gprs Selector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }

        tracker.addIssue(className,
                         Issue.SeverityEnum.WARNING,
                         Issue.ErrorTypeEnum.NOT_SUPPORTED,
                         "GPRSSelector is not supported yet !");
        log.info("GPRSSelector is not supported yet !");
        return selector;
    }
}
