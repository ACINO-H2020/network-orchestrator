package org.onosproject.orchestrator.dismi.validation;

import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Validator for SDWAN action.
 */
public class ActionSDWANValidator extends FieldValidator {
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        /*
          private String action = null;
          private Subject source = null;
          private Subject destination = null;
         */
        log.info("Validating and resolving Action type SDWAN !");
        SDWAN sdwanAction = null;
        SDWAN resolvedSDWAN = null;
        String className = "SDWAN";

        if (null == field) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");
            return resolvedSDWAN;
        }

        //  Validate the object type
        if (!(field instanceof SDWAN)) {
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTAPATH,
                             field.toString());
            return resolvedSDWAN;
        }
        sdwanAction = (SDWAN) field;
        resolvedSDWAN = new SDWAN();

        // Validate and resolve the source and destination
        SubjectValidator subjectValidator = new SubjectValidator();
        List<Subject> resolvedSubjectList = new ArrayList<>();
        Object o;
        Subject resolvedSubject;
        //  Source
        o = subjectValidator.validateAndResolve(sdwanAction.getSource(), tracker);
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
            resolvedSDWAN.setSource(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            log.info("Added resolved source subject(s) in Action type Path !");
        }
        //  Destination
        o = subjectValidator.validateAndResolve(sdwanAction.getDestination(), tracker);
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
            resolvedSDWAN.setDestination(resolvedSubject);
            resolvedSubjectList.add(resolvedSubject);
            log.info("Added resolved destination subject(s) in Action type Path !");
        }

        /* Check feasibility:
            - EndPoint Type Compatibility
            - Uniqueness of the ConnectionPoints
         */
        if (resolvedSubjectList.size() > 0) {
            log.info("Resolving network edges for Action type Path !");
            subjectValidator.resolveNetworkEdges(resolvedSubjectList, tracker);
        }
        log.info("Action type Path successfully resolved !");
        return resolvedSDWAN;
    }
}
