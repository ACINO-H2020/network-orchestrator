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
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/4/16.
 */
public class ActionMulticastValidator extends FieldValidator {
    String className = "Multicast";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*
            private String action = "Multicast";
            private Subject source = null;
            private List<Subject> destination = new ArrayList<Subject>();

         */
        //log.info("Validating and resolving Action type Multicast !");
        Multicast mcast = null;
        Multicast resolvedMcast = null;

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedMcast;
        }

        //  Validate the object type
        if (!(field instanceof Multicast)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTAMULTICAST,
                             field.toString());
            return resolvedMcast;
        }
        mcast = (Multicast) field;
        resolvedMcast = new Multicast();

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(mcast.getSource(), mcast.getDestination())) {
            log.error("Source connection point cannot be in the detination list of Multicast topology !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionConnectionValidator::validateAndResolve Source connection point cannot be in the detination list of Multicast topology !");
            return resolvedMcast;
        }


        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> totalSubjectList = new ArrayList<>();

        Subject resolvedSubject;
        List<Subject> resolvedSubjectList = null;

        //  Source - This is a single Subject
        resolvedSubject = subjectValidator.validateAndResolve(mcast.getSource(), tracker);
        if (null == resolvedSubject) {
            log.error("Problems when resolving source subjest(s) for Action type Multicast !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             mcast.toString());
        } else {
            resolvedMcast.setSource(resolvedSubject);
            totalSubjectList.add(resolvedSubject);
            //log.info("Added resolved source subject(s) in Action type Multicast !");
        }

        //  Destination - This is a List of Subject
        resolvedSubjectList = subjectValidator.validateAndResolveList(mcast.getDestination(), tracker);
        if (null == resolvedSubjectList) {
            log.error("Problems when resolving destination subjest(s) for Action type Multicast !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             mcast.toString());
        } else {
            resolvedMcast.setDestination(resolvedSubjectList);
            totalSubjectList.addAll(resolvedSubjectList);
            //log.info("Added resolved destination subject(s) in Action type Multicast !");
        }

        /*
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (totalSubjectList.size() > 0) {
            //log.info("Resolving network edges for Action type Multicast !");
            subjectValidator.resolveNetworkEdges(totalSubjectList, tracker);
        }
        // log.info("Action type Multicast successfully resolved !");
        return resolvedMcast;
    }
}
