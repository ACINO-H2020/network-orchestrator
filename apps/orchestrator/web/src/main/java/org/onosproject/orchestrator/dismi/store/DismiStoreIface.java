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

import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;

import java.util.List;
import java.util.Set;


public interface DismiStoreIface {

    /* ConnectionPoints */
    Set<ConnectionPoint> getConnectionPoints();

    ConnectionPointList getConnectionPointsAsList();

    boolean connectionPointExists(ConnectionPoint connectionPoint);

    boolean updateConnectionPoint(ConnectionPoint currentCP, ConnectionPoint newCP, Set<EndPoint> endPointSet);

    boolean updateConnectionPoint(ConnectionPoint cp1, ConnectionPoint cp2);

    boolean deleteConnectionPoint(ConnectionPoint connectionPoint);

    Set<EndPoint> getEndPoints(ConnectionPoint point);

    EndPointList getEndPointsAsList(ConnectionPoint point);

    void listEndPoints();

    void addEndPoint(ConnectionPoint connectionPoint, EndPoint e);

    /*  Services    */
    List<Service> getServicesAsList();

    void removeAllServices();

    Resource addNewService(Service service);

    Resource addServiceUpdate(Service service);
    //Resource getResource(String id);

    Service getOriginalService(String id);

    Service getOriginalServiceUpdate(String id);

    Service getResolvedService(String id);

    boolean setResolvedService(String id, ServiceExtended service, Tracker tracker);

    boolean setResolvedServiceUpdate(String id, ServiceExtended service, Tracker tracker);

    //boolean setResolvedIntentUpdate(String intentId, Intent intent, Tracker tracker);
    //Service removeService(String id);
    void listServices();

    void placeOriginalWithUpdateService(String id, Service service, ServiceExtended resolvedService, Tracker tracker);

    /* Intents */
    Intent getOriginalIntent(String serviceId, String intentId);
    //public boolean setResolvedIntentStatus(String serviceId, String intentId, IntentFsmEvent status);

    /*  Updating a Service  */
    //boolean setServiceUpdateRequest(String serviceID, Service service, Service resolvedService, Tracker tracker);

    boolean deleteService(String id);

    boolean deleteService(String id, Service service, Tracker tracker);
}
