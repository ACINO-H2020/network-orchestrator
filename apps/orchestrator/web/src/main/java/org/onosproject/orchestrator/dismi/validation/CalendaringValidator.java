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

import org.onosproject.orchestrator.dismi.primitives.Calendaring;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.CalendaringExtended;
import org.onosproject.orchestrator.dismi.validation.InputAssertion.IntentTime;
import org.onosproject.orchestrator.dismi.validation.InputAssertion.Type;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class CalendaringValidator extends FieldValidator {
    String className = "CalendaringValidator";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        log.info("Validating and resolving Calendaring List !");
        if (null == field) {
            log.error("Calendaring List is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }

        if (!(field instanceof List)) {
            log.error("Invalid calendaring List !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_CALENDARING_LIST,
                             "");
            return null;
        }
        List<Calendaring> calendaringList = (List<Calendaring>) field;
        if (calendaringList.size() <= 0) {
            log.error("Calendaring List is empty !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_CALENDARING_LIST,
                             "Calendaring list is empty !");
            return null;
        }
        log.info("Successfully validated and resolved Calendaring List !");
        return validateAndResolveList(calendaringList, tracker);
    }

    private CalendaringExtended validateAndResolveACalendaring(Calendaring calendaring, Tracker tracker) {
        log.info("Validating and resolving a Calendaring entry !");
        CalendaringExtended calendaringExtended = null;
        InputAssertion inputAssertion = new InputAssertion();
        if (null == calendaring) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return null;
        }
        // This should come after checking null
        calendaringExtended = new CalendaringExtended(calendaring);
        Date startDate = calendaring.getStartTime();
        Date stopDate = calendaring.getStopTime();
        log.info("Comparing dates and time for calendaring entry !");
        InputAssertion.IntentTime intentTime = inputAssertion.compareDate(startDate, stopDate);
        switch (intentTime) {
            case FIXED:
                // Resolved it only for fixed duration
                calendaringExtended.setCalendaringType(IntentTime.FIXED);
                log.info("Calendaring entry is resolved as Fixed !");
                break;
            case OPEN_ENDED:
                // Resolved it only provides start time
                calendaringExtended.setCalendaringType(IntentTime.OPEN_ENDED);
                log.info("Calendaring entry is resolved as Open-Ended!");
                break;
            case TERMINATION_TIME:
                // Resolved it only provide termination time
                calendaringExtended.setCalendaringType(IntentTime.TERMINATION_TIME);
                log.info("Calendaring entry is resolved as fixed-termination-time !");
                break;
            case SAME_START_END:
                log.error("Problems when validating and resolving calendaring entry becasue start and end date/time " +
                                  "are same !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.DATEINVALID,
                                 "Start and end dates of service are same !");
                return null;
            case FAIL:
                log.error("Problems when validating and resolving calendaring entry !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.DATEINVALID,
                                 "Invalid start or end date !");
                return null;
            default:
                log.error("*Problems when validating and resolving calendaring entry !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.DATEINVALID,
                                 "Invalid start or/and end date !");
                return null;
        }
        log.info("Validating and resolving cost limit !");
        String costLimit = calendaring.getCostLimit();

        if (null == costLimit) {
            log.error("Cost limit value is not specified !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "Cost Limit =" + costLimit + "");
            return null;
        }

        costLimit = inputAssertion.assertIntent(costLimit);
        costLimit = costLimit.trim();
        try {
            double numValue = inputAssertion.resolveValue(costLimit, Type.TIME);
            if (numValue <= 0) {
                log.error("Invalid cost limit !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 calendaring.getCostLimit());
                return null;
            }
            calendaringExtended.setCostLimitExt(numValue);

        } catch (Exception exp) {
            log.error("*Invalid cost limit !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             calendaring.getCostLimit());
            return null;
        }
        log.info("Successfully resolved calendering entry !");
        return calendaringExtended;
    }

    private List<Calendaring> validateAndResolveList(List<Calendaring> calendaringList, Tracker tracker) {

        List<Calendaring> calendaringListExtended = new ArrayList<>();
        CalendaringExtended calendaringExtended = null;
        for (Calendaring calendaring : calendaringList) {

            calendaringExtended = validateAndResolveACalendaring(calendaring, tracker);
            if (null != calendaringExtended) {
                calendaringListExtended.add(calendaringExtended);
            } else {
                log.warn("Error in calendering entry !");
            }
        }
        return calendaringListExtended;
    }
}
