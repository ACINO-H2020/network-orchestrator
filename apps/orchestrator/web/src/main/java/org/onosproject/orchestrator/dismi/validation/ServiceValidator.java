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

import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ServiceValidator extends FieldValidator {
    String className = "Service";
    private final Logger log = getLogger(getClass());

    /**
     * @param field   is the Service instance to resolve
     * @param tracker is the Tracker instance that logs the validation and resolution of the service
     * @return a partially or fully resolved Service instance (see the tracker) if possible.
     * Returns null if:
     * - The resolution gets a catastrophic failure (input parameters are null)
     * - The store can't be reached
     * - store.setResolvedService(...) fails
     */
    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        //log.info("ServiceValidator::Validating and resolving a service !");
        //  First validate and resolve the service
        ServiceExtended resolvedService = validateAndResolveService(field, tracker);
        DismiStoreIface store = get(DismiStoreIface.class);
        if (null == store) {
            log.error("ServiceValidator::Problems when getting instance of a store !");
            return null;
        }
        //log.info("ServiceValidator::Status of tracker after validating and resolving a service is '"+tracker
        //     .isValid()+"'!");
        if (!tracker.isValid()) {
            store.setResolvedService(resolvedService.getServiceId(), resolvedService, tracker);
        }

        //  Store the resolved service
        if (null != resolvedService) {
            //log.info("ServiceValidator::Storing validated and resolved service !");
            if (!store.setResolvedService(resolvedService.getServiceId(), resolvedService, tracker)) {
                log.error("ServiceValidator::Problems when storing validated and resolved service !");
                return null;
            }
        }
        //log.info("ServiceValidator::Service validation and resolution process completed !");
        return resolvedService;
    }

    /**
     * @param service is the Service instance to resolve
     * @param tracker is the Tracker instance that logs the validation and resolution of the service
     * @return a partially or fully resolved Service instance (see the tracker) if possible.
     * Returns null if:
     * - The resolution gets a catastrophic failure (input parameters are null)
     * - The store can't be reached
     * - store.setResolvedService(...) fails
     */
    public Service updateService(Service service, Tracker tracker) {

        // log.info("ServiceValidator::Handling validation and resolution process of an updated service '"+service
        //     .getDisplayName()+"'!");
        //  First validate and resolve the service
        ServiceExtended resolvedService = validateAndResolveService(service, tracker);

        DismiStoreIface store = get(DismiStoreIface.class);
        if (null == store) {
            log.error("*ServiceValidator::Problems when getting instance of a store !");
            return null;
        }
        if (!tracker.isValid()) {
            store.setResolvedService(resolvedService.getServiceId(), resolvedService, tracker);
        }
        //  Store the resolved service
        if (null != resolvedService) {
            //log.info("*ServiceValidator::Storing validated and resolved service !");
            if (!store.setResolvedServiceUpdate(resolvedService.getServiceId(), resolvedService, tracker)) {
                log.error("*ServiceValidator::Problems when storing validated and resolved service !");
                return null;
            }
        }
        //log.info("ServiceValidator::Updating service validation and resolution process completed !");
        return resolvedService;
    }

    private ServiceExtended validateAndResolveService(Object field, Tracker tracker) {
        /* Fields of a Service primitive

          private String serviceId = null;
          private String displayName = null;
          private List<Intent> intents = new ArrayList<Intent>();
       */

        Service service = null;
        ServiceExtended resolvedService = null;

        if (null == field) {
            log.error("ServiceValidator::Instance of a service is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "service");
            return null;
        }

        //  Validate the object type
        if (!(field instanceof Service)) {
            log.error("ServiceValidator::Invalid instance of a service !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTASERVICE,
                             "");
            return null;
        }
        service = (Service) field;
        // log.info("ServiceValidator::Service '"+service.getDisplayName()+"' having id '"+service.getServiceId()+"' " +
        //      "is being validated and resolved !");
        resolvedService = new ServiceExtended();

        //  Validate the object ID
        if (null == service.getServiceId()) {
            log.error("ServiceValidator::Service id is missing !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.IDMISSING,
                             "The Service Id should have already be assigned by the server");
        } else {
            resolvedService.setServiceId(service.getServiceId());
        }

        //  Validate the object name
        String name = service.getDisplayName();
        if (name == null) {
            log.error("ServiceValidator::Service name is missing !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.NAMEMISSING,
                             "");
        }
        resolvedService.setDisplayName(service.getDisplayName());

        //  Validate the object list of Intent
        List<Intent> list = service.getIntents();
        if (null == list) {
            log.error("ServiceValidator::Service intent list is missing !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "Service must have atleast one intent !");
            return null;
        }
        if (list.size() <= 0) {
            log.error("ServiceValidator::Service intent list is empty !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "Intent list is empty, service must have atleast one intent !");
            return null;
        }

        //Intent intent;
        IntentValidator validator = new IntentValidator();
        List<IntentExtended> resolvedList = new ArrayList<IntentExtended>();
        IntentExtended resolvedIntent;
        Object o;

        for (Intent intent : list) {
            o = validator.validateAndResolve(intent, tracker);
            if (null == o) {
                log.error("ServiceValidator::Intent after validation and resolution is null !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.NULLPOINTER,
                                 "IntentValidator::validateAndResolve returned a null pointer!");
            } else if (!(o instanceof IntentExtended)) {
                log.error("ServiceValidator::Invalid instance of Extended Intent !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.OBJECTISNOTANINTENT,
                                 "IntentValidator::validateAndResolve did not return an object of class Intent!");
            } else {
                resolvedIntent = (IntentExtended) o;
                resolvedList.add(resolvedIntent);
            }
        }
        // I think here we should check if resolvedList of intents is zero
        resolvedService.setIntentsExtended(resolvedList);
        //log.error("ServiceValidator::Service '"+resolvedService.getDisplayName()+"["+resolvedService.getServiceId()
        //  +"]' " +
        //   "validation and resolution process completed !");
        return resolvedService;
    }
}
