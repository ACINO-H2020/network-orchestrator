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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.ModelType;
import org.onosproject.orchestrator.dismi.compiler.IntentDecomposerManager;
import org.onosproject.orchestrator.dismi.primitives.Action;
import org.onosproject.orchestrator.dismi.primitives.Aggregate;
import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint;
import org.onosproject.orchestrator.dismi.primitives.Calendaring;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.DelayConstraint;
import org.onosproject.orchestrator.dismi.primitives.DismiIntentState;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthEndPoint;
import org.onosproject.orchestrator.dismi.primitives.EthSelector;
import org.onosproject.orchestrator.dismi.primitives.FiberEndPoint;
import org.onosproject.orchestrator.dismi.primitives.GprsSelector;
import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.LambdaEndPoint;
import org.onosproject.orchestrator.dismi.primitives.LambdaSelector;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.ServerInfo;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.onosproject.orchestrator.dismi.primitives.VPN;
import org.onosproject.orchestrator.dismi.primitives.extended.BandwidthConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.CalendaringExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.DelayConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.onosproject.orchestrator.dismi.validation.InputAssertion;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;


@Component(immediate = true)
//@Service
@org.apache.felix.scr.annotations.Service
public class DismiStoreImpl implements DismiStoreIface {

    private static final Serializer SERIALIZER = Serializer
            .using(new KryoNamespace
                    .Builder()
                           .register(KryoNamespaces.API)
                           .register(CpId.class)
                           .register(EndPoint.class)
                           .register(IPEndPoint.class)
                           .register(EthEndPoint.class)
                           .register(LambdaEndPoint.class)
                           .register(FiberEndPoint.class)
                           //TODO: addAditionalClass
                           .register(ServiceId.class)
                           .register(AggregateServiceData.class)
                           //.register(AggregateServiceData.Status.class)
                           .register(Action.class)
                           .register(Aggregate.class)
                           .register(AvailabilityConstraint.class)
                           .register(BandwidthConstraint.class)
                           .register(Calendaring.class)
                           .register(Calendaring.RecurrenceEnum.class)
                           .register(Connection.class)
                           .register(ConnectionPoint.class)
                           .register(Constraint.class)
                           .register(Date.class)
                           .register(DismiIntentState.class)
                           .register(DelayConstraint.class)
                           .register(EthSelector.class)
                           .register(GprsSelector.class)
                           .register(Intent.class)
                           .register(IntentExtended.class)
                           .register(InternalIntentState.class)
                           .register(IPSelector.class)
                           .register(IPSelector.IpProtocolEnum.class)
                           .register(Issue.class)
                           .register(Issue.SeverityEnum.class)
                           .register(Issue.ErrorTypeEnum.class)
                           .register(LambdaSelector.class)
                           .register(Mesh.class)
                           .register(Multicast.class)
                           .register(SDWAN.class)
                           .register(VPN.class)
                           .register(Path.class)
                           .register(Priority.class)
                           .register(Priority.PriorityEnum.class)
                           .register(Service.class)
                           .register(ServiceExtended.class)
                           .register(SecurityConstraint.class)
                           .register(SecurityConstraint.EncStrengthEnum.class)
                           .register(SecurityConstraint.IntStrengthEnum.class)
                           .register(Selector.class)
                           .register(Subject.class)
                           .register(Tracker.class)
                           .register(Tree.class)
                           .register(ServerInfo.class)
                           .register(ConnectionPointExtended.class)
                           .register(CalendaringExtended.class)
                           .register(InputAssertion.IntentTime.class)
                           .register(BandwidthConstraintExtended.class)
                           .register(DelayConstraintExtended.class)
                           .register(HighAvailabilityConstraint.class)
                           .register(IntentFiniteStateMachine.class)
                           .register(IntentDecomposerManager.class)

                           //
                           //.nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
                           .build("StoreTest"));

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AciStoreIface aciStoreIface;

    //private ConsistentMap<CpId, Set<BaseClass>> consistentMap;
    //private Map<CpId, Set<BaseClass>> map;
    private ConsistentMap<CpId, Set<EndPoint>> connectionPointConsistentMap;
    private Map<CpId, Set<EndPoint>> connectionPointMap;
    private ConsistentMap<ServiceId, AggregateServiceData> serviceConsistentMap;
    private Map<ServiceId, AggregateServiceData> serviceMap;
    private int serviceCounter;

    private final Logger log = getLogger(getClass());

    private DismiStateHandler dismiStateHandler;
    private DismiInternalIntentListener internalIntentListener = new DismiInternalIntentListener();

    @Activate
    public void activate() {

        connectionPointConsistentMap = storageService.<CpId, Set<EndPoint>>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("consistent-map-connectionpoint")
                .withRelaxedReadConsistency()
                .build();
        connectionPointMap = connectionPointConsistentMap.asJavaMap();

        serviceConsistentMap = storageService.<ServiceId, AggregateServiceData>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("consistent-map-servicetest")
                .withRelaxedReadConsistency()
                .build();
        serviceMap = serviceConsistentMap.asJavaMap();

        serviceCounter = 1;

        dismiStateHandler = new DismiStateHandler(this, aciStoreIface, intentService, hostService);
        intentService.addListener(internalIntentListener);

        log.debug("Service StoreTest started");
        //log.debug();

        //initDb();
    }

    @Deactivate
    public void deactivate() {
        intentService.removeListener(internalIntentListener);
        internalIntentListener = null;
        dismiStateHandler = null;
        connectionPointConsistentMap.destroy();
        serviceConsistentMap.destroy();
        log.debug("Stopped");
    }


    /****************************************************
     * ConnectionPoint.
     ****************************************************/

    private Set<CpId> getConnectionPointIds() {
        Set<CpId> set = ImmutableSet.copyOf(connectionPointMap.keySet());

        if (null == set) {  // That should not be possible !
            set = new HashSet<>();
        }

        return set;
    }

    private Set<EndPoint> getEndPoints(CpId id) {
        log.debug("DismiStoreImpl::getEndPoints - id " + id);

        Set<EndPoint> set = connectionPointMap.get(id);
        if (null == set) {
            set = new HashSet<>();
        }
        return set;
    }

    private void updateCpId(CpId id, Set<EndPoint> set) {
        if ((null != id) || (null != set)) {
            connectionPointMap.put(id, set);
        }
    }

    @Override
    public boolean deleteConnectionPoint(ConnectionPoint connectionPoint) {
        CpId id = CpId.getId(connectionPoint.getName());

        if (connectionPointMap.containsKey(id)) {
            connectionPointMap.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean updateConnectionPoint(ConnectionPoint cp1, ConnectionPoint cp2) {

        //  Sanitizing
        if ((null == cp1) || (null == cp2)) {
            return false;
        }
        String name1 = cp1.getName();
        String name2 = cp2.getName();
        if ((null == name1) || (name1.length() == 0) || (null == name2) || (name2.length() == 0)) {
            return false;
        }

        //  Same name?
        if (name1.compareTo(name2) == 0) {
            return false;
        }

        CpId id = CpId.getId(name1);
        Set<EndPoint> set;
        if (connectionPointMap.containsKey(id)) {
            set = connectionPointMap.remove(id);
            for (EndPoint e : set) {
                addEndPoint(cp2, e);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Set<ConnectionPoint> getConnectionPoints() {
        Set<ConnectionPoint> cpSet = new HashSet<>();

        //log.debug("DismiStoreImpl::getConnectionPoints - Entering the function");

        Set<CpId> cpIdSet = getConnectionPointIds();
        ConnectionPoint cp;

        for (CpId id : cpIdSet) {
            //log.debug("DismiStoreImpl::getConnectionPoints - Found cpId: " + id);
            cp = new ConnectionPoint();
            cp.setName(id.toString());
            cpSet.add(cp);
        }

        return cpSet;
    }

    @Override
    public ConnectionPointList getConnectionPointsAsList() {
        Set<ConnectionPoint> set = getConnectionPoints();
        ConnectionPointList list = new ConnectionPointList();

        for (ConnectionPoint cp : set) {
            list.add(cp);
        }
        return list;
    }

    @Override
    public Set<EndPoint> getEndPoints(ConnectionPoint connectionPoint) {
        log.debug("DismiStoreImpl::getEndPoints - point " + connectionPoint);

        Set<CpId> idSet = getConnectionPointIds();
        String name = connectionPoint.getName();

        if (null != name) {
            for (CpId id : idSet) {
                if (id.toString().compareTo(name) == 0) {
                    return getEndPoints(id);
                }
            }
        }
        log.debug("Found no id for connectionPoint with name \"" + name + "\"");
        return new HashSet<EndPoint>();
    }

    @Override
    public EndPointList getEndPointsAsList(ConnectionPoint connectionPoint) {
        log.debug("DismiStoreImpl::getEndPointsAsList - point " + connectionPoint);

        Set<EndPoint> set = getEndPoints(connectionPoint);
        EndPointList list = new EndPointList();

        if (null != set) {
            for (EndPoint e : set) {
                list.add(e);
            }
        }
        return list;
    }

    @Override
    public void addEndPoint(ConnectionPoint connectionPoint, EndPoint e) {
        //log.debug("DismiStoreImpl::addEndPoint:");
        //log.debug("  Adding EndPoint to ConnectionPoint with name \"" + connectionPoint.getName() + "\"");

        CpId id = CpId.getId(connectionPoint.getName());
        Set<EndPoint> set = getEndPoints(id);
        //Set<EndPoint> set = getEndPoints(connectionPoint);

        set.add(e);
        updateCpId(id, set);
        //log.debug("  Done :-)");
    }

    @Override
    public boolean connectionPointExists(ConnectionPoint connectionPoint) {
        Set<ConnectionPoint> set = getConnectionPoints();

        for (ConnectionPoint cp : set) {
            if (connectionPoint.equals(cp)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateConnectionPoint(ConnectionPoint currentCP, ConnectionPoint newCP, Set<EndPoint> endPointSet) {

        //  Sanitizing
        if ((null == endPointSet) || (null == currentCP) || (null == newCP)) {
            return false;
        }
        if ((null == currentCP.getName()) || (null == newCP.getName())) {
            return false;
        }
        if ((currentCP.getName().length() == 0) || (newCP.getName().length() == 0)) {
            return false;
        }

        // Does the ConnectionPoint exist?
        if (!deleteConnectionPoint(currentCP)) {
            return false;
        }

        // Let's go!
        for (EndPoint e : endPointSet) {
            addEndPoint(newCP, e);
        }
        return true;
    }


    /****************************************************
     * DISMI services.
     * <p>
     * SERVICEROOTNAME = "DismiService_";
     * INTENTROOTNAME = "DismiIntent_";
     * SERVICE_INTENT_SEP = "-";
     ****************************************************/
    @Override
    public List<Service> getServicesAsList() {
        log.debug("DismiStoreImpl::getServicesAsList:");

        Set<ServiceId> idSet = serviceMap.keySet();

        if (idSet.isEmpty()) {
            return Lists.newArrayList();
        }

        List<Service> serviceList = Lists.newArrayList();
        //  idSet can have a size of zero, but it is always a valid object
        for (ServiceId id : idSet) {
            serviceList.add(serviceMap.get(id).getOriginalService());
        }
        return serviceList;
    }

    private AggregateServiceData getAggregateServiceData(String id) {
        ServiceId sId = ServiceId.getId(id);
        return serviceMap.get(sId);
    }

    private void setAggregateData(String serviceId, AggregateServiceData serviceData) {
        ServiceId id = ServiceId.getId(serviceId);
        serviceMap.put(id, serviceData);
    }

    @Override
    public void listServices() {
        Set<ServiceId> idSet = ImmutableSet.copyOf(serviceMap.keySet());

        log.debug("DismiStoreImpl::listServices:");
        for (ServiceId id : idSet) {
            log.debug("  id=\"" + id.id() + "\"");

        }
    }

    @Override
    public void removeAllServices() {
        serviceMap.clear();
    }

    @Override
    public Resource addNewService(Service service) {
        Resource serviceResource = new Resource();
        Resource intentResource = new Resource();
        int serviceNr;

        if (null == service) {
            serviceResource.setInvalid();
            return serviceResource;
        }

        // Setting the ServiceID
        serviceNr = serviceCounter;
        serviceCounter++;
        serviceResource.serviceId(serviceNr);
        service.setServiceId(serviceResource.getResource());

        // Setting the IntentID for all included Intents
        List<Intent> list = service.getIntents();
        String intentId;
        for (int i = 0; i < list.size(); i++) {
            intentResource.intentId(serviceNr, i);
            list.get(i).setIntentId(intentResource.getResource());
        }

        // Store the Service in the Service database
        AggregateServiceData serviceData = new AggregateServiceData();
        serviceData.setOriginalService(service);
        setAggregateData(serviceResource.getResource(), serviceData);
        return serviceResource;
    }

    public boolean deleteService(String id) {
        ServiceId serviceId = ServiceId.getId(id);
        //serviceMap.remove(serviceId);
        return true;

    }

    public boolean deleteService(String id, Service service, Tracker tracker) {

        AggregateServiceData serviceDataT = getAggregateServiceData(service.getServiceId());
        Service servicePre = serviceDataT.getOriginalService();
        List<Intent> pre = servicePre.getIntents();
        List<Intent> current = service.getIntents();
        List<Intent> intentOrg = subtractIntents(pre, current);
        if (intentOrg.size() == 0) {
            return deleteService(id);
        }
        service.setIntents(intentOrg);
        ServiceExtended resolvedServicePre = serviceDataT.getResolvedService();
        List<Intent> pree = resolvedServicePre.getIntents();
        List<Intent> currente = service.getIntents();
        List<Intent> intentRes = subtractIntents(pree, currente);
        resolvedServicePre.setIntents(intentRes);
        AggregateServiceData serviceData = new AggregateServiceData();
        serviceData.setOriginalService(service);
        serviceData.setResolvedService(resolvedServicePre, tracker);
        setAggregateData(service.getServiceId(), serviceData);
        return true;
    }

    private List<Intent> subtractIntents(List<Intent> pre, List<Intent> current) {
        List<Intent> merged = new ArrayList<Intent>();
        boolean isAdded = false;
        for (int i = 0; i < current.size(); i++) {
            Intent intentCur = current.get(i);
            isAdded = false;
            for (int j = 0; j < pre.size(); j++) {
                Intent intentPre = pre.get(j);
                if (intentPre.getIntentId().equals(intentCur.getIntentId())) {
                    pre.remove(j);
                    break;
                }

            }
        }
        return pre;
    }

    /**
     * @param id              to identify a service
     * @param service         : Original service
     * @param resolvedService Resolved serviced
     * @param tracker         tracker to contain errors
     */
    public void placeOriginalWithUpdateService(String id, Service service, ServiceExtended resolvedService, Tracker
            tracker) {
        AggregateServiceData serviceDataT = getAggregateServiceData(service.getServiceId());
        Service servicePre = serviceDataT.getOriginalService();
        List<Intent> pre = servicePre.getIntents();
        List<Intent> current = service.getIntents();
        List<Intent> intentOrg = mergerIntents(pre, current);
        service.setIntents(intentOrg);

        ServiceExtended resolvedServicePre = serviceDataT.getResolvedService();

        List<IntentExtended> pree = resolvedServicePre.getIntentsExtended();
        List<IntentExtended> currente = resolvedService.getIntentsExtended();
        List<IntentExtended> intentRes = mergerExtendedIntents(pree, currente);
        resolvedService.setIntentsExtended(intentRes);

        AggregateServiceData serviceData = new AggregateServiceData();
        serviceData.setOriginalService(service);
        serviceData.setResolvedService(resolvedService, tracker);
        setAggregateData(service.getServiceId(), serviceData);
    }

    private List<Intent> mergerIntents(List<Intent> pre, List<Intent> current) {
        List<Intent> merged = new ArrayList<Intent>();
        boolean isAdded = false;
        for (Intent intentPre : pre) {
            isAdded = false;
            for (Intent intentCur : current) {
                System.out.println("");
                if (intentPre.getIntentId().equals(intentCur.getIntentId())) {
                    merged.add(intentCur);
                    isAdded = true;
                    break;
                }

            }
            if (isAdded == false) {
                merged.add(intentPre);
            }
        }
        return merged;
    }

    private List<IntentExtended> mergerExtendedIntents(List<IntentExtended> pre, List<IntentExtended> current) {
        List<IntentExtended> merged = new ArrayList<IntentExtended>();
        boolean isAdded = false;
        for (IntentExtended intentPre : pre) {
            isAdded = false;
            for (IntentExtended intentCur : current) {
                System.out.println("");
                if (intentPre.getIntentId().equals(intentCur.getIntentId())) {
                    merged.add(intentCur);
                    isAdded = true;
                    break;
                }

            }
            if (isAdded == false) {
                merged.add(intentPre);
            }
        }
        return merged;
    }


    @Override
    public Resource addServiceUpdate(Service service) {
        Resource serviceResource = new Resource();
        Resource rfail = new Resource();

        rfail.setInvalid();

        if (null == service) {
            return rfail;
        }

        serviceResource.setResource(service.getServiceId());
        if (!serviceResource.isServiceId()) {
            return rfail;
        }

        //  Get the AggregateServiceData corresponding to the serviceId
        AggregateServiceData serviceData = getAggregateServiceData(service.getServiceId());
        if (null == serviceData) {
            return rfail;
        }

        // Add the serviceUpdate
        if (!serviceData.setServiceUpdate(service)) {
            // The request to add this service update failed, we should not carry on and validate the intents
            return rfail;
        }

        // Update authorized
        setAggregateData(service.getServiceId(), serviceData);
        return serviceResource;
    }

    @Override
    public Service getOriginalService(String id) {

        AggregateServiceData aggregateServiceData = getAggregateServiceData(id);
        if (null == aggregateServiceData) {
            //Should we throw an exception instead?
            return null;
        } else {
            return aggregateServiceData.getOriginalService();
        }
    }

    @Override
    public Service getResolvedService(String id) {
        Service service;

        AggregateServiceData aggregateServiceData = getAggregateServiceData(id);
        if (null == aggregateServiceData) {
            return null;    //  Should we throw an exception instead?
        } else {
            service = aggregateServiceData.getResolvedService();
        }

        if (null == service) {
            return null;    //  Should we throw an exception instead?
        }

        return service;
    }

    @Override
    public Service getOriginalServiceUpdate(String id) {

        AggregateServiceData aggregateServiceData = getAggregateServiceData(id);
        if (null == aggregateServiceData) {
            //Should we throw an exception instead?
            return null;
        } else {
            return aggregateServiceData.getOriginalServiceUpdate();
        }
    }

    @Override
    public boolean setResolvedService(String serviceId, ServiceExtended service, Tracker tracker) {
        if ((null == serviceId) ||
                (null == service) ||
                (null == tracker)) {
            return false;
        }

        AggregateServiceData serviceData = getAggregateServiceData(serviceId);
        if (null == serviceData) {
            log.debug("Failed to retrieve AggregateServiceData with serviceId \"" + serviceId + "\"");
            return false;
        }

        return serviceData.setResolvedService(service, tracker);
    }

    @Override
    public boolean setResolvedServiceUpdate(String serviceId, ServiceExtended service, Tracker tracker) {
        if ((null == serviceId) ||
                (null == service) ||
                (null == tracker)) {
            return false;
        }

        AggregateServiceData serviceData = getAggregateServiceData(serviceId);
        service.setDisplayName(serviceData.getOriginalService().getDisplayName());
        if (null == serviceData) {
            log.debug("Failed to retrieve AggregateServiceData with serviceId \"" + serviceId + "\"");
            return false;
        }
        // Note: Added by Abdul to make happy setResolvedServiceUpdate functions
        serviceData.setServiceUpdate(service);
        return serviceData.setResolvedServiceUpdate(service, tracker);
    }

    @Override
    public void listEndPoints() {
        Set<CpId> idSet = getConnectionPointIds();
        log.debug("DismiStoreImpl::listEndpoints:");
        ConnectionPointList connectionPointList = getConnectionPointsAsList();

        for (ConnectionPoint connectionPoint : connectionPointList) {
            List<EndPoint> endPointList = getEndPointsAsList(connectionPoint);
            for (EndPoint endPoint : endPointList) {
                log.debug(" ConnectionPoint:" + connectionPoint.getName() + " :: " + endPoint.toString());
            }
        }
    }

    @Override
    public Intent getOriginalIntent(String serviceId, String intentId) {
        Service service = getOriginalService(serviceId);
        List<Intent> intentList = service.getIntents();
        if (null == intentList) {
            return null;
        }
        for (Intent intent : intentList) {
            if (intent.getIntentId().compareTo(intentId) == 0) {
                return intent;
            }
        }
        return null;
    }

    private class DismiInternalIntentListener implements IntentListener {

        @Override
        public void event(IntentEvent event) {

            org.onosproject.net.intent.Intent intent = event.subject();
            if (ModelType.isNewModel()) {
                if (intent instanceof ACIPPIntent) {
                    ACIPPIntent ACIPPIntent = (ACIPPIntent) intent;
                    if (!ACIPPIntent.calculated() || ACIPPIntent.calculated() == null) {
                        return;
                    }
                }
            }

            Key key = event.subject().key();
            dismiStateHandler.manageStates(intent, key, event.type());
        }
    }
}
