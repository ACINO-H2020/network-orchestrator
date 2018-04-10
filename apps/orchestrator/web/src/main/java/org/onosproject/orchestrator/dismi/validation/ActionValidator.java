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

import org.onosproject.orchestrator.dismi.primitives.Action;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ActionValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        //Fields of an Action primitive:.
        //private String action = null;.
        //log.info("Validating and resolving Action !");
        Action action = null;
        Action resolvedAction = null;
        String className = "Action";

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedAction;
        }

        //  Validate the object type
        if (!(field instanceof Action)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANACTION,
                             "");

            return resolvedAction;
        }
        action = (Action) field;

        Vocabulary vocabulary = new Vocabulary();
        FieldValidator fieldValidator = vocabulary.getValidator(Vocabulary.ACTION, action.getClass());
        if (null == fieldValidator) {
            log.error("Problems when finding Action from vocabulary !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.ACTION_TYPE_NOT_FOUND,
                             "");
            return resolvedAction;
        }

        //log.info("Validating and resolving Action found from vocabulary !");
        //tracker = fieldValidator.validate(action, tracker);
        Object o;
        o = fieldValidator.validateAndResolve(action, tracker);
        if (!(o instanceof Action)) {
            log.error("Problems when validating and resolving Action !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANACTION,
                             "FieldValidator::validateAndResolve did not return an object of class Action!");

            return resolvedAction;
        } else if (null == o) {
            log.error("Problems when validating and resolving Action !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "FieldValidator::validateAndResolve returned a null pointer!");
            return resolvedAction;

        } else {
            resolvedAction = (Action) o;
        }
        //log.info("Successfully validated and resolved Action !");
        return resolvedAction;
    }
}
