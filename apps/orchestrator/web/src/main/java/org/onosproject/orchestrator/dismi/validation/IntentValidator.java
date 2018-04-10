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
import org.onosproject.orchestrator.dismi.primitives.Calendaring;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.store.IntentFsmEvent;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 9/28/16.
 */
public class IntentValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {

        /*  Fields of an Intent primitive:
          private String intentId = null;
          private String displayName = null;
          private Action action = null;
          private List<Selector> selectors = new ArrayList<Selector>();
          private List<Constraint> constraints = new ArrayList<Constraint>();
          private List<Calendaring> calendaring = null;
          private List<Priority> priority = null;
         */
        //log.info("Validating and resolving Intent !");
        Intent intent = null;
        IntentExtended resolvedIntent = null;
        String className = "Intent";

        //  Field must be non null - MANDATORY
        if (null == field) {
            log.error("Intent instance is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }

        //  Object type must be Intent - MANDATORY
        if (!(field instanceof Intent)) {
            log.error("Invalid Intent instance !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANINTENT,
                             "");
            return null;
        }
        intent = (Intent) field;
        resolvedIntent = new IntentExtended();

        if (resolvedIntent.getStateMachine().getState().equals(IntentFsmEvent.Negotiation)) {
            //log.info("Intent state switched to "+IntentFsmEvent.UpdateRequest+" state !");
            resolvedIntent.getStateMachine().changeState(IntentFsmEvent.UpdateRequest);
        }


        // Change the state to "VALIDATING"
        if (!resolvedIntent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForValidation)) {
            log.error("Intent state cannot be switched to " + IntentFsmEvent.SubmitForValidation + " state !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.ERRORUNDEFINED,
                             "Internal error!");
            return null;
        } else {
            //log.info("Intent state switched to "+IntentFsmEvent.SubmitForValidation+" state !");
            resolvedIntent.getStateMachine().changeState(IntentFsmEvent.SubmitForValidation);
        }
        Tracker newTracker = new Tracker();

        //  Intent ID must be filled - MANDATORY
        if (intent.getIntentId() == null) {
            log.error("Intent id is missing. Invalid intent !");
            newTracker.addIssue(className,
                                Issue.SeverityEnum.ERROR,
                                Issue.ErrorTypeEnum.IDMISSING,
                                "The DISMI_Intent Id should already be assigned!");
        } else {
            resolvedIntent.setIntentId(intent.getIntentId());
        }

        //  Intent name should be filled - OPTIONAL but recommended
        String name = intent.getDisplayName();
        if (null == name) {
            log.error("Intent display name is missing. Invalid intent !");
            newTracker.addIssue(className,
                                Issue.SeverityEnum.WARNING,
                                Issue.ErrorTypeEnum.NAMEMISSING,
                                "Intent name is missing !");
        } else {
            resolvedIntent.setDisplayName(intent.getDisplayName());
        }

        String intentServiceProviderKey = intent.getIntentServiceProviderKey();
        if (intentServiceProviderKey != null) {
            resolvedIntent.setIntentServiceProviderKey(intentServiceProviderKey);
        }

        resolvedIntent.setIsNegotiatable(intent.getIsNegotiatable());
        //  Action validation - Success is MANDATORY
        ActionValidator actionValidator = new ActionValidator();
        Action action = intent.getAction();
        Action resolvedAction = null;
        Object o = null;
        o = actionValidator.validateAndResolve(action, newTracker);
        if (null == o) {
            log.error("Problems when validaing action !");
            newTracker.addIssue(className,
                                Issue.SeverityEnum.ERROR,
                                Issue.ErrorTypeEnum.NULLPOINTER,
                                "");
        } else if (!(o instanceof Action)) {
            log.error("Validated and Resolved action is not a valid instance of an Action !");
            newTracker.addIssue(className,
                                Issue.SeverityEnum.ERROR,
                                Issue.ErrorTypeEnum.OBJECTISNOTANACTION,
                                "");
        } else {
            resolvedAction = (Action) o;
            resolvedIntent.setAction(resolvedAction);
        }

        // Validating Constraints - OPTIONAL data
        FieldValidator constraintValidator = new ConstraintValidator();
        List<Constraint> constraintList = intent.getConstraints();
        if ((null != constraintList) && (constraintList.size() > 0)) {

            List<Constraint> constraintListResolved
                    = (List<Constraint>) constraintValidator.validateAndResolve(constraintList, newTracker);
            if (null == constraintListResolved) {
                log.error("Invalid validated and resolved constraint list !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.NULLPOINTER,
                                    "Constraints");
            } else if (constraintListResolved.size() == 0) {
                log.error("*Invalid validated and resolved constraint list !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.INFO,
                                    Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                    "Constraints");
            } else {
                resolvedIntent.setConstraints(constraintListResolved);
            }
        }

        // Validating Calendaring - OPTIONAL data
        CalendaringValidator calendaringValidator = new CalendaringValidator();
        List<Calendaring> calendaringList = intent.getCalendaring();
        if ((null != calendaringList) && (calendaringList.size() > 0)) {
            List<Calendaring> calendaringListExtended =
                    (List<Calendaring>) calendaringValidator.
                            validateAndResolve(calendaringList, newTracker);
            if (null == calendaringListExtended) {
                log.warn("Invalid validated and resolved calendaring list !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.NULLPOINTER,
                                    "Calendaring");
            } else if (calendaringListExtended.size() == 0) {
                log.warn("*validated and resolved calendaring list is empty !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.INFO,
                                    Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                    "Calendaring");
            } else {
                resolvedIntent.setCalendaring(calendaringListExtended);
            }
        }

        // Validating Selector - OPTIONAL data
        FieldValidator selectorValidator = new SelectorValidator();
        List<Selector> selectorList = intent.getSelectors();
        if ((null != selectorList) && (selectorList.size() > 0)) {
            List<Selector> selectorListResolved
                    = (List<Selector>) selectorValidator.validateAndResolve(selectorList, newTracker);
            if (null == selectorListResolved) {
                log.warn("Invalid validated and resolved selector list !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.NULLPOINTER,
                                    "Selector");
            } else if (selectorListResolved.size() == 0) {
                log.warn("*Validated and resolved selector list is empty !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.INFO,
                                    Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                    "Selector");
            } else {
                resolvedIntent.setSelectors(selectorListResolved);
            }
        }

        // Validating Priority - OPTIONAL data
        FieldValidator priorityValidator = new PriorityValidator();
        List<Priority> priorityList = intent.getPriorities();
        if ((null != priorityList) && (priorityList.size() > 0)) {
            List<Priority> priorityListExtended
                    = (List<Priority>) priorityValidator.validateAndResolve(priorityList, newTracker);
            if (null == priorityListExtended) {
                log.warn("Invalid validated and resolved priority list !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.NULLPOINTER,
                                    "Priority");
            } else if (priorityListExtended.size() == 0) {
                log.warn("*Validated and resolved priority list is empty !");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.INFO,
                                    Issue.ErrorTypeEnum.NOOPTIONALPARAMETER,
                                    "Priority");
            } else {
                resolvedIntent.setPriorities(priorityListExtended);
            }
        }

        /* Resolution Done - Now we:
            - Merge the new tracker with the original one
            - set the Intent State
         */

        if (newTracker.isValid()) {
            if (!resolvedIntent.getStateMachine().canChangeState(IntentFsmEvent.ValidationSuccess)) {
                log.error("Internal error: Could not set the State to SUCCESS after Intent resolution?!!!");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.ERRORUNDEFINED,
                                    "Internal error: Could not set the State to SUCCESS after Intent resolution?!!!");
                resolvedIntent = null;
            } else {
                //log.info("Intent state switched to "+IntentFsmEvent.ValidationSuccess+" !");
                resolvedIntent.getStateMachine().changeState(IntentFsmEvent.ValidationSuccess);
            }
        } else {
            if (!resolvedIntent.getStateMachine().canChangeState(IntentFsmEvent.ValidationFailure)) {
                log.error("Internal error: Could not set the State to FAILURE after Intent resolution?!!!");
                newTracker.addIssue(className,
                                    Issue.SeverityEnum.ERROR,
                                    Issue.ErrorTypeEnum.ERRORUNDEFINED,
                                    "Internal error: Could not set the State to FAILURE after Intent resolution?!!!");
                resolvedIntent = null;
            } else {
                //log.info("Intent state switched to "+IntentFsmEvent.ValidationFailure+" !");
                resolvedIntent.getStateMachine().changeState(IntentFsmEvent.ValidationFailure);
            }
        }
        //log.info("Successfully validated and resolved Intent "+resolvedIntent.getDisplayName() +" ["+resolvedIntent
        //      .getIntentId()+"]"+" !");
        tracker.addTracker(newTracker);
        return resolvedIntent;
    }
}
