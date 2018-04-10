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

import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ConstraintValidator extends FieldValidator {

    String className = "constraint";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        Constraint constraint;
        //log.info("Validating and resolving constraint !");
        if (null == field) {
            log.error("Connection instance is null ");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "field");
            return null;
        }

        if (!(field instanceof List)) {
            log.error("Invalid constraint List !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_CONSTRAINT_LIST,
                             "constraint");
            return null;
        }
        if (null == tracker) {
            log.error("Invalid tracker in constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }
        List<Constraint> constraintList = (List<Constraint>) field;
        //log.info("Constraint successfully validated and resolved !");
        return validateAndResolveList(constraintList, tracker);
    }

    private Constraint validateAndResolveConstraint(Constraint constraint, Tracker tracker) {

        Vocabulary vocabulary = new Vocabulary();
        if (null == constraint) {
            log.error("Invalid constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "constraint");
            return null;
        }

        FieldValidator fieldValidator
                = vocabulary.getValidator(Vocabulary.CONSTRAINT, constraint.getClass());
        if (null == fieldValidator) {
            log.error("Problems when finding constraint validator !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.CONSTRAINTNOTRECOGNISED,
                             "");
            return null;
        }

        Object resolvedObj = fieldValidator.validateAndResolve(constraint, tracker);
        if (null == resolvedObj) {
            log.error("Problems when validating and resolving constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.RESOLUTIONFAILED,
                             "");
        } else if (!(resolvedObj instanceof Constraint)) {
            log.error("*Problems when validating and resolving constraint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONSTRAINT,
                             "");
        }

        return (Constraint) resolvedObj;
    }

    private List<Constraint> validateAndResolveList(List<Constraint> constraintList, Tracker tracker) {
        List<Constraint> constraintListExtended = new ArrayList<Constraint>();
        Constraint constraint;
        Constraint constraintResolved;

        for (int i = 0; i < constraintList.size(); i++) {
            constraint = constraintList.get(i);
            constraintResolved = validateAndResolveConstraint(constraint, tracker);
            if (null != constraintResolved) {
                constraintListExtended.add(constraintResolved);
            }
        }

        if (constraintListExtended.size() > 0) { // We have resolved constraints to return
            return constraintListExtended;
        } else {
            log.error("Constraint validation failed !!");
            return null;
        }
    }
}
