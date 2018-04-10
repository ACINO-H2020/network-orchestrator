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
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/4/16.
 */
public class ActionMeshValidator extends FieldValidator {
    String className = "Mesh";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*
            private List<Subject> source = new ArrayList<Subject>();
            private Subject destination = null;
         */
        //log.info("Validating and resolving Action type Mesh !");
        Mesh mesh = null;
        Mesh resolvedMesh = null;

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedMesh;
        }

        //  Validate the object type
        if (!(field instanceof Mesh)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTATREE,
                             field.toString());
            return resolvedMesh;
        }
        mesh = (Mesh) field;
        resolvedMesh = new Mesh();

        EdgesValidator edgesValidator = new EdgesValidator();
        if (edgesValidator.isSrcAndDstUnique(mesh.getSource())) {
            log.error("Duplicate connection point found in source addresses of Mesh topology !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDCONNECTIONPOINT,
                             "ActionConnectionValidator::validateAndResolve Duplicate connection point found in source addresses of Mesh topology !");
            return resolvedMesh;
        }

        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> totalSubjectList = new ArrayList<>();

        List<Subject> resolvedSubjectList = null;

        // The source is a list
        resolvedSubjectList = subjectValidator.validateAndResolveList(mesh.getSource(), tracker);
        if (null == resolvedSubjectList) {
            log.error("Problems when resolving source subjest(s) for Action type Mesh !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             mesh.toString());
        } else {
            resolvedMesh.setSource(resolvedSubjectList);
            totalSubjectList.addAll(resolvedSubjectList);
            //log.info("Added resolved source subject(s) in Action type Mesh !");
        }

        /*
            Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (totalSubjectList.size() > 0) {
            //log.info("Resolving network edges for Action type Mesh !");
            subjectValidator.resolveNetworkEdges(totalSubjectList, tracker);
        }
        //log.info("Action type Mesh successfully resolved !");
        return resolvedMesh;
    }
}
