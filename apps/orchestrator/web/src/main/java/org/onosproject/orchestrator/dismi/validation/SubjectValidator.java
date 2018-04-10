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


import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthEndPoint;
import org.onosproject.orchestrator.dismi.primitives.FiberEndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.LambdaEndPoint;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;


public class SubjectValidator extends FieldValidator {
    String className = "Subject";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*private ConnectionPoint connectionPoint = null;
        private List<Selector> selectors = new ArrayList<Selector>();
        private List<Constraint> constraints = new ArrayList<Constraint>();
        */
        log.info("Validating and resolving a subject !");
        Subject subject = null;
        Subject resolvedSubject = null;

        if (null == field) {
            log.error("Invalid subject (null), Please specify value of subject !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "Invalid subject, Please specify value of subject !");
            return resolvedSubject;
        }

        //  Validate the object type
        if (!(field instanceof Subject)) {
            log.error("Invalid subject !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASUBJECT,
                             "The specified value is not a valid subject !");
            return resolvedSubject;
        }

        subject = (Subject) field;
        resolvedSubject = validateAndResolve(subject, tracker);
        return resolvedSubject;
    }

    /*
    private ConnectionPoint connectionPoint = null;
    private List<Selector> selectors = new ArrayList<Selector>();
    private List<Constraint> constraints = new ArrayList<Constraint>();
    */
    public Subject validateAndResolve(Subject field, Tracker tracker) {
        Subject subject = null;
        Subject resolvedSubject = null;

        if (null == field) {
            log.error("*Invalid subject, Please specify value of subject !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "Invalid subject, Please specify value of subject !");
            return resolvedSubject;
        }
        subject = (Subject) field;
        resolvedSubject = new Subject();


        // Validating ConnectionPoint - Mandatory
        ConnectionPointValidator connectionPointValidator = new ConnectionPointValidator();
        ConnectionPoint point = subject.getConnectionPoint();
        ConnectionPointExtended resolvedPoint = connectionPointValidator.validateAndResolve(point, tracker);
        //Errors have already been reported in the ConnectionPointValidator
        if ((null != resolvedPoint)
                && (null != resolvedPoint.getEndpoints()
                && (resolvedPoint.getEndpoints().size() > 0))) {
            resolvedSubject.setConnectionPoint(resolvedPoint);
        } else {
            log.error("Problems when validating and resolving connection points of a subject !");
            return null;
        }

        // Validating Selector - OPTIONAL data
        SelectorValidator selectorValidator = new SelectorValidator();
        //log.info("Validating and resolving selectors associated with a subject !");
        List<Selector> selectorList = subject.getSelectors();
        if ((null != selectorList) && (selectorList.size() > 0)) {
            List<Selector> selectorListResolved
                    = (List<Selector>) selectorValidator.validateAndResolve(selectorList, tracker);
            if (null == selectorListResolved) {
                log.error("Resolved and validated selector list of a subject is null !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.NULLPOINTER,
                                 "Specified selector(s) are invalid !");
            } else if (selectorListResolved.size() == 0) {
                log.error("Resolved and validated selector list of a subject is empty !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.INFO,
                                 Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                 "Selector");
            } else {
                log.info("Validation and resolution process of selectors associated with a subject completed !");
                resolvedSubject.setSelectors(selectorListResolved);
            }
        } else {
            log.info("Selector list with subject is empty !");
        }


        // Validating Constraints - OPTIONAL data
        ConstraintValidator constraintValidator = new ConstraintValidator();
        //log.error("Validating and resolving constraints associated with a subject !");
        List<Constraint> constraintList = subject.getConstraints();
        if ((null != constraintList) && (constraintList.size() > 0)) {
            List<Constraint> constraintListResolved
                    = (List<Constraint>) constraintValidator.validateAndResolve(constraintList, tracker);
            if (null == constraintListResolved) {
                log.error("Resolved and validated constraint list of a subject is null !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.NULLPOINTER,
                                 "Specified constraints(s) are invalid !");
            } else if (constraintListResolved.size() == 0) {
                log.error("Resolved and validated constraint list of a subject is empty !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.INFO,
                                 Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                 "Constraints");
            } else {
                log.info("Validation and resolution process of constraints associated with a subject completed !");
                resolvedSubject.setConstraints(constraintListResolved);
            }
        }

        return resolvedSubject;
    }

    public List<Subject> validateAndResolveList(List<Subject> list, Tracker tracker) {
        Subject resolvedSubject = null;
        List<Subject> newList = null;

        if (null == list) {
            log.error("Invalid subject list, its value should not be null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "Invalid subject, its value should not be null !");
            return newList;
        }

        newList = new ArrayList<>();

        for (Subject subject : list) {
            resolvedSubject = validateAndResolve(subject, tracker);
            if (null != resolvedSubject) {
                newList.add(resolvedSubject);
            } else {
                log.error("Validation and resolution process of subject '" + subject.getConnectionPoint() + "' failed !");
            }
        }

        if (newList.size() > 0) {
            return newList;
        } else {
            log.error("Validation and resolution process generated empty list !");
            return null;
        }
    }


    /*
      Build a list of all Subject and check that
        - The ConnectionPoints are unique
        - There is a common type of EndPoints (IP, Eth, Lambda, ...)
          Remove types of EndPoints that are not present in ALL ConnectionPoints
     */
    public void resolveNetworkEdges(List<Subject> list, Tracker tracker) {
        String className = "List<Subject>";
        //log.info("Resolving network edges !");
        if (null == list) {
            log.error("*Subject list if null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
        }

        checkNetworkEdgeUniqueness(list, tracker);
        pruneEndPoints(list, tracker);

    }

    private void checkNetworkEdgeUniqueness(List<Subject> list, Tracker tracker) {
        String className = "List<Subject>";

        int i, j;
        ConnectionPoint c, d;

        for (i = 0; i < list.size(); i++) {
            c = list.get(i).getConnectionPoint();
            for (j = i + 1; j > list.size(); j++) {
                d = list.get(j).getConnectionPoint();
                if (c.getName().compareTo(d.getName()) == 0) {
                    log.error("Connection point associated with subject is not unique !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.CONNECTIONPOINTISNOTUNIQUE,
                                     c.getName());
                }
            }
        }
    }

    private void pruneEndPoints(List<Subject> subjectList, Tracker tracker) {
        String className = "List<Subject>";

        int i, j;
        ConnectionPoint point;
        ConnectionPointExtended extendedPoint;
        Set<EndPoint> endPointSet;
        Set<EndPoint> newEndpointSet;

        boolean haveIP = true;
        boolean haveEth = true;
        boolean haveLambda = true;
        boolean haveFiber = true;

        //Check that the ConnectionPoint of each Subject is a ConnectionPointExtended
        //that actually contains EndPoints
        List<Subject> tempList = subjectList;
        subjectList = new ArrayList<Subject>();
        for (i = 0; i < tempList.size(); i++) {
            point = tempList.get(i).getConnectionPoint();
            if (!(point instanceof ConnectionPointExtended)) {
                log.error("Invalid instance of Connection point associated with a subject !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.OBJECTISNOTACONNECTIONPOINTEXTENDED,
                                 point.toString());

            } else {
                extendedPoint = (ConnectionPointExtended) point;
                endPointSet = extendedPoint.getEndpoints();
                if ((null == endPointSet) || (endPointSet.size() == 0)) {
                    log.error("Connection point does not have any endpoint !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.CONNECTIONPOINTHASNOENDPOINTS,
                                     point.toString());
                } else {
                    subjectList.add(tempList.get(i));
                }
            }
        }

        //Scan all EndPoints, find which types of EndPoints are not common to all ConnectionPoints
        for (i = 0; i < subjectList.size(); i++) {
            boolean hasIP = false;
            boolean hasEth = false;
            boolean hasLambda = false;
            boolean hasFiber = false;

            point = subjectList.get(i).getConnectionPoint();

            extendedPoint = (ConnectionPointExtended) point;
            endPointSet = extendedPoint.getEndpoints();
            for (EndPoint endPoint : endPointSet) {
                if (endPoint instanceof IPEndPoint) {
                    hasIP = true;
                } else if (endPoint instanceof EthEndPoint) {
                    hasEth = true;
                } else if (endPoint instanceof LambdaEndPoint) {
                    hasLambda = true;
                } else if (endPoint instanceof FiberEndPoint) {
                    hasFiber = true;
                } else {
                    // FIXME: we shold use a debug output instead
                    // of a Tracker issue here!
                    log.error("*Invalid endpoint !");
                    tracker.addIssue(className,
                                     Issue.SeverityEnum.ERROR,
                                     Issue.ErrorTypeEnum.ENDPOINTBELONGSTONOSUBCLASS,
                                     point.getName() + ": " + endPoint);
                }
            }

            if (!hasIP) {
                haveIP = false;
            }
            if (!hasEth) {
                haveEth = false;
            }
            if (!hasLambda) {
                haveLambda = false;
            }
            if (!hasFiber) {
                haveFiber = false;
            }
        }


        //Remove EndPoints when their type is not present in all ConnectionPoints:
        //We add the EndPoints that we keep to a new list that replaces the
        //older one in the ConnectionPointExtended.

        for (i = 0; i < subjectList.size(); i++) {

            newEndpointSet = new HashSet<>();
            point = subjectList.get(i).getConnectionPoint();
            extendedPoint = (ConnectionPointExtended) point;
            endPointSet = extendedPoint.getEndpoints();
            for (EndPoint endPoint : endPointSet) {
                if (endPoint instanceof IPEndPoint) {
                    if (haveIP) {
                        newEndpointSet.add(endPoint);
                    }
                } else if (endPoint instanceof EthEndPoint) {
                    if (haveEth) {
                        newEndpointSet.add(endPoint);
                    }
                } else if (endPoint instanceof LambdaEndPoint) {
                    if (haveLambda) {
                        newEndpointSet.add(endPoint);
                    }
                } else if (endPoint instanceof FiberEndPoint) {
                    if (haveFiber) {
                        newEndpointSet.add(endPoint);
                    }
                }
            }
            extendedPoint.setEndpoints(newEndpointSet);
        }
    }
}
