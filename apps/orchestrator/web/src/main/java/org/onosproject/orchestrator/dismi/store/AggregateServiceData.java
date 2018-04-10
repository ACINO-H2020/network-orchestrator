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

package org.onosproject.orchestrator.dismi.store;

import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.onosproject.orchestrator.dismi.primitives.DismiIntentState.*;
//import org.slf4j.Logger;

//import static org.slf4j.LoggerFactory.getLogger;

public class AggregateServiceData implements Serializable {

    private String serviceId;
    private String displayName;
    private List<Intent> originalIntentList;
    private List<IntentExtended> resolvedIntentList;
    private Tracker tracker = new Tracker();
    //private final Logger log = getLogger(getClass());
    private AggregateServiceData updateRequest;

//    private final Logger log = getLogger(getClass());

    public AggregateServiceData() {
        serviceId = "";
        displayName = "";
        originalIntentList = new ArrayList<>();
        resolvedIntentList = null;
        tracker = new Tracker();

        updateRequest = null;
    }

    /*
        This function creates a Service with the list of original Intents.
        If resolved services exist, their State is used as the state of the original Intents.
        If no resolved intents exists, the state of the intents is not changed
     */
    public Service getOriginalService() {
        Service service = new Service();

        service.setServiceId(serviceId);
        service.setDisplayName(displayName);

        if (originalIntentList.size() == 0) {
            return service;
        }

        // No Extended Intents yet?
        if ((null == resolvedIntentList) || (resolvedIntentList.size() == 0)) {
            if (originalIntentList.size() > 0) {
                service.setIntents(originalIntentList);
            }
        } else {
            for (Intent intent : originalIntentList) {
                intent = updateIntentState(intent, resolvedIntentList);
                service.addIntentsItem(intent);
            }
        }

        // It sets the status based on the status of Intents
        setServiceInfo(service);

        return service;
    }

    public Service getOriginalServiceUpdate() {
        if (null == updateRequest) {
            return new Service();
        }
        return updateRequest.getOriginalService();
    }

    public boolean setOriginalService(Service service) {
        if (isOriginalServiceValid()) {
            //  We already have an original Service
            return false;
        }

        if (null == service) {
            return false;
        }

        String id = service.getServiceId();
        List<Intent> intentList = service.getIntents();
        if ((null == id) || (id.length() == 0) || (null == intentList) || (intentList.size() == 0)) {
            return false;
        }

        serviceId = id;
        displayName = service.getDisplayName();
        originalIntentList = intentList;
        for (Intent intent : originalIntentList) {
            intent.setIntentStatus(PROCESSING);
        }
        return true;
    }

    public ServiceExtended getResolvedService() {
        ServiceExtended serviceExtended = new ServiceExtended();

        serviceExtended.setServiceId(serviceId);
        serviceExtended.setDisplayName(displayName);

        if (null != resolvedIntentList) {
            List<IntentExtended> listExtended = new ArrayList<>();
            for (IntentExtended intentExtended : resolvedIntentList) {
                listExtended.add(intentExtended);
            }
            serviceExtended.setIntentsExtended(listExtended);
        }
        return serviceExtended;
    }


    public boolean setResolvedService(ServiceExtended service, Tracker t) {
        if ((!isOriginalServiceValid()) ||
                (null == service) ||
                (null == t) ||
                (null == service.getIntents()) ||
                (service.getIntents().size() == 0)) {
            return false;
        }

        resolvedIntentList = service.getIntentsExtended();

        if ((null == resolvedIntentList) || (resolvedIntentList.size() == 0)) {
            resolvedIntentList = null;
            return false;
        }

        tracker = t;
        return true;
    }

    public boolean setServiceUpdate(Service service) {
        if ((null == service) ||
                (null == tracker) ||
                (null != updateRequest)) {
            return false;
        }

        if (!isOriginalServiceValid()) {
            return false;
        }

        updateRequest = new AggregateServiceData();

        updateRequest.setOriginalService(service);

        if ((!updateRequest.isOriginalServiceValid()) ||
                (serviceId.compareTo(updateRequest.serviceId) != 0) ||
                (displayName.compareTo(updateRequest.displayName) != 0)) {
            return false;
        }

        //  Check that all the Intent update requests have IDs that exist in the originals
        Intent intent;
        for (Intent it : updateRequest.originalIntentList) {
            intent = getOriginalIntent(it.getIntentId());
            if (null == intent) {
                traceMe("Intent \"" + it.getIntentId() + "\" does not belog to original Service \"" + serviceId + "\"");
                return false;
            }
        }

        return true;
    }

    public boolean setResolvedServiceUpdate(ServiceExtended service, Tracker tracker) {
        if (null == updateRequest) {
            return false;
        }
        if (!updateRequest.isOriginalServiceValid()) {
            return false;
        }
        if (null == resolvedIntentList) {
            return false;
        }
        if (null == service) {
            return false;
        }
        if (null == tracker) {
            return false;
        }
        if (null == service.getIntents()) {
            return false;
        }
        if (service.getIntents().size() == 0) {
            return false;
        }

        if (null != updateRequest.resolvedIntentList) {
            //  There is already a list of update requests!
            return false;
        }


        updateRequest.resolvedIntentList = new ArrayList<>();
        // Compare each resolvedIntentUpdate with the original resolvedUpdate
        List<IntentExtended> intentList = service.getIntentsExtended();
        IntentExtended iExtended;
        for (Intent it : intentList) {
            iExtended = getResolvedIntent(it.getIntentId());
            if (null != iExtended) {
                /*if (iExtended.getStateMachine().canChangeState(IntentFsmEvent.UpdateRequest)) */
                /*if (iExtended.getStateMachine().getState() == InternalIntentState.Validated)*/
                {
                    // Update of the Intent is permitted
                    updateRequest.resolvedIntentList.add(iExtended);
                }
            }
        }

        //  Did we get anything in?
        if (updateRequest.resolvedIntentList.size() == 0) {
            updateRequest.resolvedIntentList = null;
            return false;
        }

        updateRequest.tracker = tracker;
        return true;
    }

    private Intent updateIntentState(Intent intent, List<IntentExtended> list) {
        if ((null == intent) || (null == list)) {
            return intent;
        }

        for (IntentExtended ie : list) {
            if (ie.getIntentId().compareTo(intent.getIntentId()) == 0) {
                //intent.setIntentStatus(ie.getIntentStatus());
                intent.setIntentStatus(ie.getStateMachine().getState().getUserState());
                return intent;
            }
        }

        return intent;
    }

    public List<IntentExtended> getResolvedIntents() {
        if (null == resolvedIntentList) {
            return new ArrayList<IntentExtended>();
        }
        return resolvedIntentList;
    }

    public List<IntentExtended> getResolvedIntentUpdates() {
        return updateRequest.getResolvedIntents();
    }

    private void setServiceInfo(Service service) {
        if (null == service) {
            return;
        }

        List<Intent> list = service.getIntents();
        boolean hasProcessing = false;
        boolean hasProcessing_failed = false;
        boolean hasInstalling = false;
        boolean hasFailed = false;
        boolean hasInstalled = false;

        for (Intent intent : list) {
            switch (intent.getIntentStatus()) {
                case PROCESSING:
                    hasProcessing = true;
                    break;
                case PROCESSING_FAILED:
                    hasProcessing_failed = true;
                    break;
                case INSTALLING:
                    hasInstalling = true;
                    break;
                case INSTALLED:
                    hasInstalled = true;
                    break;
                case FAILED:
                    hasFailed = true;
                    break;
                case WITHDRAWING:   //  Ignore these cases as they are normal (requested by the client)
                case WITHDRAWN:
                default:
                    break;
            }
        }

        if (hasProcessing_failed || hasFailed) {
            service.setServiceStatus(FAILED);
        } else if (hasProcessing) {
            service.setServiceStatus(PROCESSING);
        } else if (hasInstalling) {
            service.setServiceStatus(INSTALLING);
        } else {
            service.setServiceStatus(INSTALLED);
        }
    }


    /*
    private String serviceId;
    private String displayName;
    private List<Intent> originalIntentList;
    private List<IntentExtended> extendedIntentList;
    private Tracker tracker;

    */
    public boolean isOriginalServiceValid() {
        if ((serviceId.length() == 0) ||
                (null == originalIntentList) ||
                (originalIntentList.size() == 0)) {
            return false;
        }
        return true;
    }

    public Intent getOriginalIntent(String intentId) {
        if (null == originalIntentList) {
            return null;
        }

        for (Intent intent : originalIntentList) {
            if (intentId.compareTo(intent.getIntentId()) == 0) {
                return intent;
            }
        }
        return null;
    }

    public IntentExtended getResolvedIntent(String intentId) {
        if (null == resolvedIntentList) {
            return null;
        }

        for (IntentExtended intent : resolvedIntentList) {
            if (intentId.compareTo(intent.getIntentId()) == 0) {
                return intent;
            }
        }
        return null;
    }

    public Tracker getTracker() {
        return tracker;
    }

    /*
        private String serviceId;
        private String displayName;
        private List<Intent> originalIntentList;
        private List<IntentExtended> extendedIntentList;
        private Tracker tracker;

        private AggregateServiceData updateRequest;

     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AggregateServiceData {\n");
        sb.append("    serviceId: ").append(toIndentedString(serviceId)).append("\n");
        sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
        sb.append("    originalIntentList: ").append(toIndentedString(originalIntentList)).append("\n");
        sb.append("    extendedIntentList: ").append(toIndentedString(resolvedIntentList)).append("\n");
        sb.append("    tracker: ").append(toIndentedString(tracker)).append("\n");
        sb.append("    updateRequest: ").append(toIndentedString(updateRequest)).append("\n");
        sb.append("}");
        return sb.toString();
    }


    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    private void traceMe(String msg) {
        System.out.println(msg);
    }
}
