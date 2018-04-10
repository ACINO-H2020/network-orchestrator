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

import org.onosproject.orchestrator.dismi.primitives.Aggregate;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/4/16.
 */
public class ActionAggregateValidator extends FieldValidator {
    String className = "Tree";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*
            private List<Subject> source = new ArrayList<Subject>();
            private Subject destination = null;

         */
        // log.info("Validating and resolving Action type Aggregate !");
        Aggregate aggregate = null;
        Aggregate resolvedAggregate = null;

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedAggregate;
        }

        //  Validate the object type
        if (!(field instanceof Aggregate)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTATREE,
                             field.toString());
            return resolvedAggregate;
        }
        aggregate = (Aggregate) field;
        resolvedAggregate = new Aggregate();

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(aggregate.getSource(), aggregate.getDestination())) {
            log.error("Destination connection point cannot be in the source list of Aggregate topology !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionConnectionValidator::validateAndResolve Destination connection point cannot be in the source list of Aggregate topology !");
            return resolvedAggregate;
        }


        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> totalSubjectList = new ArrayList<>();

        Subject resolvedSubject;
        List<Subject> resolvedSubjectList = null;

        // The source is a list
        resolvedSubjectList = subjectValidator.validateAndResolveList(aggregate.getSource(), tracker);
        if (null == resolvedSubjectList) {
            log.error("Problems when resolving source subjest(s) for Action type Aggregate !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             aggregate.toString());
        } else {
            resolvedAggregate.setSource(resolvedSubjectList);
            totalSubjectList.addAll(resolvedSubjectList);
            //log.info("Added resolved source subject(s) in Action type Aggregate !");
        }

        //  Destination - This is a single Subject
        resolvedSubject = subjectValidator.validateAndResolve(aggregate.getDestination(), tracker);
        if (null == resolvedSubject) {
            log.error("Action type Aggregate does not have destination subject !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             aggregate.toString());
        } else {
            resolvedAggregate.setDestination(resolvedSubject);
            totalSubjectList.add(resolvedSubject);
            // log.info("Added resolved destination subject(s) in Action type Aggregate !");
        }

        /*
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (totalSubjectList.size() > 0) {
            // log.info("Resolving network edges for Action type Aggregate !");
            subjectValidator.resolveNetworkEdges(totalSubjectList, tracker);
        }
        //log.info("Action type Aggregate successfully resolved !");
        return resolvedAggregate;
    }
}
