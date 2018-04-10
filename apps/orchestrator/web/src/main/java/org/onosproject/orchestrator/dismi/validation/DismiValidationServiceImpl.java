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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by stephane on 10/10/16.
 */


@Component(immediate = true)
@org.apache.felix.scr.annotations.Service
public class DismiValidationServiceImpl implements DismiValidationServiceIface {
    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DismiStoreIface dismiStore;

    @Activate
    public void activate() {
        log.info("DismiValidationServiceImpl started.");
    }

    @Deactivate
    public void deactivate() {
        log.info("DismiValidationServiceImpl stopped.");
    }

    @Override
    public Resource submitNewService(Service service) {
        //log.info("Submitting a new service '"+service.getDisplayName()+"' !");
        Resource resource = dismiStore.addNewService(service);

        if (resource.isValid()) {
            //log.info("Processing newly submitted service !");
            Service s = dismiStore.getOriginalService(service.getServiceId());
            processService(s, ValidationTypeEnum.Create);
        }

        return resource;
    }

    @Override
    public Resource submitServiceUpdate(String serviceId, Service service) {
        // log.info("Updating an existing service '"+service.getDisplayName()+"' !");
        service.setServiceId(serviceId);

        Resource resource = dismiStore.addServiceUpdate(service);

        if (resource.isValid()) {
            log.info("Processing started to update a service !");
            Service s = dismiStore.getOriginalServiceUpdate(service.getServiceId());
            processService(service, ValidationTypeEnum.Update);
        }

        return resource;
    }

    @Override
    public Resource submitIntentUpdate(String serviceId, String intentId, Intent intent) {
        //log.info("Updating an intent of existing service. intent Name:'"+intent.getDisplayName()+"' !");
        Resource rfail = new Resource();
        rfail.setInvalid();

        if ((null == intent) ||
                (null == dismiStore.getOriginalIntent(serviceId, intentId)) ||
                (!intentId.startsWith(serviceId))) {
            log.error("Invalid intent '" + intent.getDisplayName() + "' !");
            return rfail;
        }

        Resource resource = new Resource();
        resource.setResource(intentId);
        intent.setIntentId(intentId);

        if (resource.isValid()) {   // This should always be true)
            log.info("Processing started to update an intent of a service '" + serviceId + "' !");
            processIntent(intent, ValidationTypeEnum.Update);
        }

        return resource;
    }

    @Override
    public boolean deleteService(String serviceId) {
        // log.info("Deleting an existing service '"+serviceId+"' !");
        if (null == dismiStore.getOriginalService(serviceId)) {
            return false;
        }

        Service service = new Service();
        service.setServiceId(serviceId);
        log.info("Processing started to delete a service !");
        processService(service, ValidationTypeEnum.Delete);
        return true;
    }

    @Override
    public boolean deleteIntent(String serviceId, String intentId) {
        //log.info("Deleting an intent "+intentId+" of existing service '"+serviceId+"' !");
        if (null == dismiStore.getOriginalIntent(serviceId, intentId)) {
            log.error("Invalid intent " + intentId + " of a service '" + serviceId + "' !");
            return false;
        }

        if (!intentId.startsWith(serviceId)) {
            //  The intentId and serviceId do not belong together!
            log.error("*Invalid intent " + intentId + " of a service '" + serviceId + "' !");
            return false;
        }

        Intent intent = new Intent();
        intent.setIntentId(intentId);
        log.info("Processing started to delete an intent of a service '" + serviceId + "' !");
        processIntent(intent, ValidationTypeEnum.Delete);
        return true;
    }

    private void processService(Service s, ValidationTypeEnum action) {

        TaskValidateService task = new TaskValidateService(s, action);
        Thread thread = new Thread(task);
        thread.start();

    }

    private void processIntent(Intent intent, ValidationTypeEnum action) {

        TaskValidateService task = new TaskValidateService(intent, action);
        Thread thread = new Thread(task);
        thread.start();

    }

    public enum ValidationTypeEnum {
        Create("Create Service"),

        Update("Update Service or Intent"),

        Delete("Delete Service or intent");

        private String value;

        ValidationTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
