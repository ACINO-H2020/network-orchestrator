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
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/4/16.
 */
public class ActionTreeValidator extends FieldValidator {
    String className = "Tree";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        Tree tree = null;
        Tree resolvedtree = null;

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedtree;
        }

        //  Validate the object type
        if (!(field instanceof Tree)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTATREE,
                             field.toString());
            return resolvedtree;
        }
        tree = (Tree) field;
        resolvedtree = new Tree();

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(tree.getSource(), tree.getDestination())) {
            log.error("Source connection point cannot be in the destiantion list of Tree topology !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionConnectionValidator::validateAndResolve Source connection point cannot be in the destiantion list of Tree topology !");
            return resolvedtree;
        }

        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> totalSubjectList = new ArrayList<>();

        Subject resolvedSubject;
        List<Subject> resolvedSubjectList = null;

        //  Source - This is a single Subject
        resolvedSubject = subjectValidator.validateAndResolve(tree.getSource(), tracker);
        if (null == resolvedSubject) {
            log.error("Problems when resolving source subjest(s) for Action type Tree !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             tree.toString());
        } else {
            resolvedtree.setSource(resolvedSubject);
            totalSubjectList.add(resolvedSubject);
            //log.info("Added resolved source subject(s) in Action type Tree !");
        }

        //  Destination - This is a List of Subject
        resolvedSubjectList = subjectValidator.validateAndResolveList(tree.getDestination(), tracker);
        if (null == resolvedSubjectList) {
            log.error("Problems when resolving destination subjest(s) for Action type Tree !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             tree.toString());
        } else {
            resolvedtree.setDestination(resolvedSubjectList);
            totalSubjectList.addAll(resolvedSubjectList);
            //log.info("Added resolved destination subject(s) in Action type Tree !");
        }

        /*
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (totalSubjectList.size() > 0) {
            //log.info("Resolving network edges for Action type Tree !");
            subjectValidator.resolveNetworkEdges(totalSubjectList, tracker);
        }
        //log.info("Action type Tree successfully resolved !");
        return resolvedtree;
    }
}
