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

import org.onosproject.orchestrator.dismi.primitives.Connection;
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
public class ActionConnectionValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        //log.info("Validating and resolving Action type Connection !");
        Connection connection = null;
        Connection resolvedConnection = null;
        String className = "ActionConnectionValidator";

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedConnection;
        }

        //  Validate the object type
        if (!(field instanceof Connection)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTACONNECTION,
                             field.toString());
            return resolvedConnection;
        }
        connection = (Connection) field;
        resolvedConnection = new Connection();

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(connection.getSource(), connection.getDestination())) {
            log.error("Source and destination connection points should be different !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionConnectionValidator::validateAndResolve Invalid source and destiantion connection points !");
            return resolvedConnection;
        }

        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> resolvedSubjectList = new ArrayList<>();
        Object resolvedObj;
        Subject resolvedSubject;

        //  Source
        resolvedObj = subjectValidator.validateAndResolve(connection.getSource(), tracker);
        if (null == resolvedObj) {
            log.error("Problems when resolving source subjest(s) for Action type Connection !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the source!");
            return null;
        } else if (!(resolvedObj instanceof Subject)) { //FIXME Error here ?!!!
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the source!");
            log.error("Problems when resolving source subjest(s) for Action type Connection !");
            return null;
        } else {
            resolvedSubject = (Subject) resolvedObj;
            resolvedConnection.setSource(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            //log.info("Added resolved source subject(s) in Action type Connection !");
        }

        //  Destination
        resolvedObj = subjectValidator.validateAndResolve(connection.getDestination(), tracker);
        if (null == resolvedObj) {
            log.error("Problems when resolving destination subjest(s) for Action type Connection !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the destination !");
            return null;
        } else if (!(resolvedObj instanceof Subject)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "SubjectValidator::validateAndResolve did not return a subject for the destination!");
            return null;
        } else {
            resolvedSubject = (Subject) resolvedObj;
            resolvedConnection.setDestination(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            //log.info("Added resolved destination subject(s) in Action type Connection !");
        }

        /*
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (resolvedSubjectList.size() > 0) {
            //log.info("Resolving network edges for Action type Connection !");
            subjectValidator.resolveNetworkEdges(resolvedSubjectList, tracker);
        }
        //log.info("Action type Connection successfully resolved !");
        return resolvedConnection;
    }
    /* @Override
    public Tracker validate(Object field, Tracker tracker) {
        *//*
          private String action = null;
          private Subject source = null;
          private Subject destination = null;

         *//*


        Connection connection = null;
        String className = "Connection";

        if (null == field) {
            tracker.addIssue(className,
                    Issue.SeverityEnum.ERROR,
                    Issue.ErrorTypeEnum.NULLPOINTER,
                    "");
            return tracker;
        }

        //  Validate the object type
        if (field instanceof Connection) {
            connection = (Connection) field;
        } else {
            tracker.addIssue(className,
                    Issue.SeverityEnum.ERROR,
                    Issue.ErrorTypeEnum.OBJECTISNOTANACTION,
                    "");

            tracker.addIssue(className,
                    Tracker.ErrorType.ObjectIsNotAConnection,
                    Tracker.Severity.Error,
                    "");
            return tracker;
        }

        String actionStr = connection.getAction();

        if (null == actionStr) {
            tracker.addIssue(className,
                    Tracker.ErrorType.ActionNameIsNull,
                    Tracker.Severity.Warning,
                    "");
        }

        SubjectValidator subjectValidator = new SubjectValidator();
        tracker = subjectValidator.validate(connection.getSource(), tracker);
        tracker = subjectValidator.validate(connection.getDestination(), tracker);

        *//* TODO:
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         *//*

        return tracker;
    }*/
}
