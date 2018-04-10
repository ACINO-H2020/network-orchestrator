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
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/4/16.
 */
public class ActionPathValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*
          private String action = null;
          private Subject source = null;
          private Subject destination = null;
         */
        //log.info("Validating and resolving Action type Path !");
        Path path = null;
        Path resolvedPath = null;
        String className = "Path";

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedPath;
        }

        //  Validate the object type
        if (!(field instanceof Path)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTAPATH,
                             field.toString());
            return resolvedPath;
        }
        path = (Path) field;

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(path.getSource(), path.getDestination())) {
            log.error("Source and destination connection points should be different !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionPathValidator::validateAndResolve Invalid source and destiantion connection points !");
            return resolvedPath;
        }

        resolvedPath = new Path();

        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> resolvedSubjectList = new ArrayList<>();
        Object o;
        Subject resolvedSubject;
        //  Source
        o = subjectValidator.validateAndResolve(path.getSource(), tracker);
        if (!(o instanceof Subject)) {
            log.error("Problems when resolving source subjest(s) for Action type Path !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the source!");

        } else if (null == o) {
            log.error("Problems when resolving source subjest(s) for Action type Path !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "SubjectValidator::validateAndResolve returned a null pointer for the source!");
        } else {
            resolvedSubject = (Subject) o;
            resolvedPath.setSource(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            // log.info("Added resolved source subject(s) in Action type Path !");
        }
        //  Destination
        o = subjectValidator.validateAndResolve(path.getDestination(), tracker);
        if (!(o instanceof Subject)) {
            log.error("Problems when resolving destination subjest(s) for Action type Path !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the destination!");

        } else if (null == o) {
            log.error("Problems when resolving destination subjest(s) for Action type Path !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "SubjectValidator::validateAndResolve returned a null pointer for the destination!");
        } else {
            resolvedSubject = (Subject) o;
            resolvedPath.setDestination(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            //log.info("Added resolved destination subject(s) in Action type Path !");
        }

        /* Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (resolvedSubjectList.size() > 0) {
            //log.info("Resolving network edges for Action type Path !");
            subjectValidator.resolveNetworkEdges(resolvedSubjectList, tracker);
        }
        // log.info("Action type Path successfully resolved !");
        return resolvedPath;
    }
}
