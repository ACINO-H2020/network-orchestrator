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
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class SelectorValidator extends FieldValidator {
    String className = "Selector";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        log.info("Validating and resolving Selector list !");
        List<Selector> selectorList = null;
        if (null == field) {
            log.error("Instance of Selector list is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             className);
            return null;
        }
        if (!(field instanceof List)) {
            log.error("Invalid instance of Selector list !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_SELECTOR_LIST,
                             className);
            return null;
        }

        selectorList = (List<Selector>) field;
        if (selectorList.size() <= 0) {
            log.error("Selector list is empty !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             className);
            return null;
        }

        if (null == tracker) {
            log.error("Tracker of Selector list is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             className);
            return null;
        }
        //log.info("Selector list validation and resolution process completed !");
        return validateAndResolveList(selectorList, tracker);
    }

    private Selector validateAndResolveSelector(Selector selector, Tracker tracker) {
        Selector resolvedSelector;

        Vocabulary vocabulary = new Vocabulary();
        if (null == selector) {
            log.error("Instance of Selector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             className);
            return null;
        }

        FieldValidator fieldValidator
                = vocabulary.getValidator(Vocabulary.SELECTOR, selector.getClass());
        if (null == fieldValidator) {
            log.error(selector.getClass() + " selector is not registered !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASELECTOR,
                             className);
            return null;
        }

        Object resolvedObj = fieldValidator.validateAndResolve(selector, tracker);
        if (null == resolvedObj) {
            log.error("Invalid selector !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.RESOLUTIONFAILED,
                             selector.toString());
        } else if (!(resolvedObj instanceof Selector)) {
            log.error("*Invalid selector !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONSTRAINT,
                             selector.toString());
        }
        return (Selector) resolvedObj;
    }

    private List<Selector> validateAndResolveList(List<Selector> selectorList, Tracker tracker) {
        List<Selector> selectorListExtended = new ArrayList<Selector>();
        Selector resolvedSelector;

        if (null == selectorList) {
            log.error("Selector list is not valid !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "List<Selector>");
            return selectorListExtended;
        }

        for (Selector selector : selectorList) {
            resolvedSelector = validateAndResolveSelector(selector, tracker);
            if (null != resolvedSelector) {
                selectorListExtended.add(resolvedSelector);
            } else {
                log.error("Invalid validated and resolved Selector list !");
            }
        }
        return selectorListExtended;
    }
}
