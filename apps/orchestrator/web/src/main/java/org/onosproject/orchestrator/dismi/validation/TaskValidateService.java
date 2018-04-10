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

import org.onlab.rest.BaseResource;
import org.onosproject.orchestrator.dismi.compiler.IntentDecomposer;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.orchestrator.dismi.store.IntentFsmEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class TaskValidateService extends BaseResource implements Runnable {
    private DismiValidationServiceImpl.ValidationTypeEnum
            action = DismiValidationServiceImpl.ValidationTypeEnum.Create;
    private Service service = null;
    private Tracker tracker = null;
    private Service serviceResolved = null;
    private boolean isService = true;

    private Logger log = getLogger(getClass());
    private long deltaT = 20000;    // Sleeping time if can't reach another service

    private TaskValidateService() {
    }

    public TaskValidateService(Service s, DismiValidationServiceImpl.ValidationTypeEnum a) {
        action = a;
        service = s;
        tracker = new Tracker();
        isService = true;
    }

    public TaskValidateService(Intent intent, DismiValidationServiceImpl.ValidationTypeEnum a) {
        if (null == intent) {
            return;
        }
        action = a;
        tracker = new Tracker();
        service = new Service();
        // Create a service from the intent
        service.addIntentsItem(intent);
        Resource resource = new Resource();
        resource.setResource(intent.getIntentId());
        service.setServiceId((resource.getFullyQualifiedServiceId()));
        isService = false;
    }

    @Override
    public void run() {
        handleService();
    }

    public boolean handleService() {
        if (null == service) {
            log.error("handleService::Service value is null. It cannot be processed !");
            //traceMeError("TaskValidateService::handleService(): service is \"null\"");
            return false;
        }
        //log.info("handleService::Processing Service '"+service.getDisplayName()+"' !");
        ServiceValidator validator = new ServiceValidator();
        ServiceExtended serviceExtended;
        Object o;
        switch (action) {
            case Create:
                // Try to get the name of service
                //log.info("handleService::Validating and Resolving Service '"+service.getDisplayName()+"' !");
                o = validator.validateAndResolve(service, tracker);
                if ((null == o) || (!(o instanceof ServiceExtended))) {
                    log.error("TaskValidateService::handleService():ServiceValidator::validateAndResolve did not " +
                                      "return a Service !");
                    throw (new RuntimeException("TaskValidateService::handleService(): " +
                                                        "ServiceValidator::validateAndResolve did not return a Service!"));
                }
                serviceExtended = (ServiceExtended) o;

                if (tracker.isValid()) {
                    //log.info("handleService::Service '"+service.getDisplayName()+"' is valid and submitting for " +
                    //   "compilation !");
                    if (service2Aci(serviceExtended, tracker, DismiValidationServiceImpl.ValidationTypeEnum.Create)) {
                        if (null != serviceExtended) {
                            // log.info("handleService::Updating resolved service in store !");
                            DismiStoreIface dismiStore = get(DismiStoreIface.class);
                            if (!dismiStore.setResolvedService(serviceExtended.getServiceId(), serviceExtended, tracker)) {
                                log.error("handleService:: Problems when updating resolved service in store !");
                                return false;
                            }
                        }
                        //log.info("handleService::Service successfully submitted !");
                        return true;
                    } else {
                        //log.error("handleService::Problems when submitting a new service !");
                        return false;
                    }
                } else {
                    log.info("handleService::Validation and resolution process of service '" + service
                            .getDisplayName() + "' failed !");
                    DismiStoreIface dismiStore = get(DismiStoreIface.class);
                    if (!dismiStore.setResolvedService(serviceExtended.getServiceId(), serviceExtended, tracker)) {
                        return false;
                    }
                    return false;
                }
            case Update:
                o = validator.updateService(service, tracker);
                if ((null == o) || (!(o instanceof ServiceExtended))) {
                    log.error("TaskValidateService::handleService():ServiceValidator::updateService did not " +
                                      "return a Service !");
                    throw (new RuntimeException("TaskValidateService::handleService(): " +
                                                        "ServiceValidator::updateService did not return a Service!"));
                }
                serviceExtended = (ServiceExtended) o;
                if (tracker.isValid()) {
                    // log.info("handleService::Updated Service '"+service.getDisplayName()+"' is valid and submitting
                    // " +
                    //        "for compilation !");
                    if (service2Aci(serviceExtended, tracker, DismiValidationServiceImpl.ValidationTypeEnum.Update)) {
                        // Changing status of intents
                        List<IntentExtended> intentExtendedList = serviceExtended.getIntentsExtended();
                        List<IntentExtended> updatedIntentExtendedList = new ArrayList<IntentExtended>();
                        for (IntentExtended intent : intentExtendedList) {
                            if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation)) {
                                // Update store
                                //  log.info("handleService::Updated Service '"+service.getDisplayName()+"', changing " +
                                //        "intent status to '"+IntentFsmEvent.SubmitForInstallation+"'!");
                                intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation);
                            }
                            updatedIntentExtendedList.add(intent);
                        }
                        service.setDisplayName(serviceExtended.getDisplayName());
                        // Creating temp service to replace exisitng Original Services with Updated one
                        ServiceExtended serviceExtendedUpdated = new ServiceExtended();
                        serviceExtendedUpdated.setIntentsExtended(updatedIntentExtendedList);
                        serviceExtendedUpdated.setServiceStatus(service.getServiceStatus());
                        serviceExtendedUpdated.setServiceId(service.getServiceId());
                        serviceExtendedUpdated.setDisplayName(serviceExtended.getDisplayName());
                        serviceExtendedUpdated.setServerInfo(service.getServerInfo());
                        DismiStoreIface dismiStore = get(DismiStoreIface.class);
                        // Replacing Orginial with updated services
                        // log.info("handleService::Replacing original service with update service in store !");
                        dismiStore.placeOriginalWithUpdateService(serviceExtended.getServiceId(), service, serviceExtendedUpdated, tracker);
                        // log.info("handleService::Update service successfully submitted !");
                        return true;
                    } else {
                        log.error("handleService::Problems when submitting an updated service !");
                        return false;
                    }

                } else {
                    DismiStoreIface dismiStore = get(DismiStoreIface.class);
                    log.info("handleService::Validation and resolution process of updated-service '" + service
                            .getDisplayName() + "' failed !");
                    if (!dismiStore.setResolvedService(serviceExtended.getServiceId(), serviceExtended, tracker)) {
                        log.error("handleService:: Problems when updating resolved updated-service in store !");
                        return false;
                    }
                    return false;
                }
            case Delete:
                // log.info("handleService::Withdrawing an existing service '"+service.getServiceId()+"'!");
                DismiStoreIface store = get(DismiStoreIface.class);
                if (null == store) {
                    log.error("handleService::Problems when creating instance of a store !");
                    return false;
                }
                Service resolvedService = store.getResolvedService(service.getServiceId());
                //  Store the resolved service
                if (null == resolvedService) {
                    log.error("handleService::Colud not find service from store against service id !");
                    return false;
                }
                serviceExtended = (ServiceExtended) resolvedService;
                // log.info("handleService::Submitting request for service withdrawn !");
                service2Aci(serviceExtended, tracker, DismiValidationServiceImpl.ValidationTypeEnum.Delete);
                if (isService) {
                    store.deleteService(serviceExtended.getServiceId());
                } else {
                    store.deleteService(serviceExtended.getServiceId(), service, tracker);
                }
                log.info("handleService::Withdrawn request successfully submitted !");
                return true;
            default:
                log.error("TaskValidateService::run: Error - Action not recognized");
                return false;
        }
        // We don't register the resolved service to the Dismitore as this is already done by the ServiceValidator
    }

    private boolean service2Aci(ServiceExtended serviceExtended, Tracker tracker, DismiValidationServiceImpl
            .ValidationTypeEnum
            actionType) {
        //log.info("handleService::Converting DISMI Service into ACI Intents !");
        IntentDecomposer dismi2aci = get(IntentDecomposer.class);

        if (null == serviceExtended) {
            return false;
        }

        while (null == dismi2aci) {
            //log.error("Can't reach service Dismi2Aci - sleeping " + deltaT + "ms");
            //traceMe("Error: Can't reach service Dismi2Aci - sleeping " + deltaT + "ms");
            try {
                Thread.sleep(deltaT);
            } catch (InterruptedException e) {
                log.error("Dismi2Aci thread interruption error ");
                e.printStackTrace();
            }
            dismi2aci = get(IntentDecomposer.class);
        }

        dismi2aci.performAction(serviceExtended, tracker, actionType);

        return true;
    }

    private boolean deleteService() {
        return false;
    }
}
