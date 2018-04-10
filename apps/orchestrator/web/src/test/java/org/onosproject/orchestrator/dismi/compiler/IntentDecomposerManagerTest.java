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

package org.onosproject.orchestrator.dismi.compiler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.packet.IpAddress;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.Annotations;
import org.onosproject.net.DefaultHost;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.HostLocation;
import org.onosproject.net.host.HostServiceAdapter;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentServiceAdapter;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.MockIdGenerator;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.aciIntents.ModelType;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Resource;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.onosproject.orchestrator.dismi.store.CpId;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.orchestrator.dismi.validation.DismiValidationServiceImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class IntentDecomposerManagerTest {

    private IntentDecomposerManager intentDecomposerManager;
    private TestDismiStore dismiStore;
    private TestHostService testHostService;
    private TestIntentService testIntentService;

    @Before
    public void setUp() {

        testHostService = new TestHostService();
        dismiStore = new TestDismiStore();
        testIntentService = new TestIntentService();
        TestCoreService testCoreService = new TestCoreService();

        testHostService.addHost(IpAddress.valueOf("192.168.1.2"), MacAddress.valueOf("10:10:10:10:10:02"));
        testHostService.addHost(IpAddress.valueOf("192.168.1.3"), MacAddress.valueOf("10:10:10:10:10:03"));

        ConnectionPoint sourceConnectionPoint = new ConnectionPoint();
        sourceConnectionPoint.setName("source");
        IPEndPoint sourceIp = new IPEndPoint();
        sourceIp.setInAddr("192.168.1.2/32");
        sourceIp.routerId("null:1");
        sourceIp.setPortId(1);
        dismiStore.addEndPoint(sourceConnectionPoint, sourceIp);

        ConnectionPoint destinationConnectionPoint = new ConnectionPoint();
        destinationConnectionPoint.setName("destination");
        IPEndPoint dstIp = new IPEndPoint();
        dstIp.setInAddr("192.168.1.3/32");
        dstIp.routerId("null:2");
        dstIp.setPortId(1);
        dismiStore.addEndPoint(destinationConnectionPoint, dstIp);

        intentDecomposerManager = new IntentDecomposerManager();
        intentDecomposerManager.coreService = testCoreService;
        intentDecomposerManager.dismiStore = dismiStore;
        intentDecomposerManager.aciStoreIface = new TestAciIface();
        intentDecomposerManager.intentService = testIntentService;
        intentDecomposerManager.hostService = testHostService;
        intentDecomposerManager.activate();

        MockIdGenerator.cleanBind();

    }

    @After
    public void tearDown() {
        intentDecomposerManager = null;
        MockIdGenerator.unbind();
    }

    @Test
    public void decomposeConnectionBidirectional() {

        OnosFeatures.setBidirectional(true);
        OnosFeatures.setUnidirectional(false);

        ModelType.setNewModel(false);

        intentDecomposerManager.decompose("1", createConnectionIntent(), null,
                                          DismiValidationServiceImpl.ValidationTypeEnum.Create);

        assertEquals(1, testIntentService.getIntentCount());

        for (org.onosproject.net.intent.Intent aciIntent : intentDecomposerManager.intentService.getIntents()) {
            assertThat(aciIntent, instanceOf(AciIntent.class));
        }

        intentDecomposerManager.intentService.withdraw(null);

    }

    @Test
    public void decomposeConnectionUnidirectional() {

        OnosFeatures.setBidirectional(false);
        OnosFeatures.setUnidirectional(true);

        ModelType.setNewModel(false);

        intentDecomposerManager.decompose("1", createConnectionIntent(), null,
                                          DismiValidationServiceImpl.ValidationTypeEnum.Create);

        assertEquals(2, testIntentService.getIntentCount());

        for (org.onosproject.net.intent.Intent aciIntent : intentDecomposerManager.intentService.getIntents()) {
            assertThat(aciIntent, instanceOf(AciIntent.class));
        }

        intentDecomposerManager.intentService.withdraw(null);


    }

    @Test
    public void decomposeConnectionUnidirectionalNewModel() {
        OnosFeatures.setBidirectional(false);
        OnosFeatures.setUnidirectional(true);

        ModelType.setNewModel(true);

        intentDecomposerManager.decompose("1", createConnectionIntent(), null,
                                          DismiValidationServiceImpl.ValidationTypeEnum.Create);

        assertEquals(2, testIntentService.getIntentCount());

        for (org.onosproject.net.intent.Intent aciIntent : intentDecomposerManager.intentService.getIntents()) {
            assertThat(aciIntent, instanceOf(ACIPPIntent.class));
        }

        intentDecomposerManager.intentService.withdraw(null);
    }

    @Test
    public void decomposeConnectionBidirectionalNewModel() {
        OnosFeatures.setBidirectional(true);
        OnosFeatures.setUnidirectional(false);

        ModelType.setNewModel(true);

        intentDecomposerManager.decompose("1", createConnectionIntent(), null,
                                          DismiValidationServiceImpl.ValidationTypeEnum.Create);

        assertEquals(1, testIntentService.getIntentCount());

        for (org.onosproject.net.intent.Intent aciIntent : intentDecomposerManager.intentService.getIntents()) {
            assertThat(aciIntent, instanceOf(ACIPPIntent.class));
        }


        intentDecomposerManager.intentService.withdraw(null);
    }

    @Test
    public void decomposePath() {

        OnosFeatures.setBidirectional(true);
        OnosFeatures.setUnidirectional(false);

        ModelType.setNewModel(false);

        intentDecomposerManager.decompose("1", createPathIntent(), null,
                                          DismiValidationServiceImpl.ValidationTypeEnum.Create);

        assertEquals(1, testIntentService.getIntentCount());

        for (org.onosproject.net.intent.Intent aciIntent : intentDecomposerManager.intentService.getIntents()) {
            assertThat(aciIntent, instanceOf(AciIntent.class));
        }

        intentDecomposerManager.intentService.withdraw(null);

    }

    private IntentExtended createConnectionIntent() {

        IntentExtended dismiIntent = new IntentExtended();
        Connection action = new Connection();

        Subject source = new Subject();
        ConnectionPoint sourceConnectionPoint = new ConnectionPoint();
        sourceConnectionPoint.setName("source");
        source.setConnectionPoint(sourceConnectionPoint);

        Subject destination = new Subject();
        ConnectionPoint destinationConnectionPoint = new ConnectionPoint();
        destinationConnectionPoint.setName("destination");
        destination.setConnectionPoint(destinationConnectionPoint);

        action.setSource(source);
        action.setDestination(destination);

        dismiIntent.setAction(action);
        dismiIntent.setIntentId("intent1");

        return dismiIntent;
    }

    private IntentExtended createPathIntent() {

        IntentExtended dismiIntent = new IntentExtended();
        Path action = new Path();

        Subject source = new Subject();
        ConnectionPoint sourceConnectionPoint = new ConnectionPoint();
        sourceConnectionPoint.setName("source");
        source.setConnectionPoint(sourceConnectionPoint);

        Subject destination = new Subject();
        ConnectionPoint destinationConnectionPoint = new ConnectionPoint();
        destinationConnectionPoint.setName("destination");
        destination.setConnectionPoint(destinationConnectionPoint);

        action.setSource(source);
        action.setDestination(destination);

        dismiIntent.setAction(action);
        dismiIntent.setIntentId("intent1");

        return dismiIntent;
    }

    private class TestCoreService extends CoreServiceAdapter {
        @Override
        public ApplicationId registerApplication(String name) {
            return new DefaultApplicationId(0, name);
        }
    }

    private class TestAciIface implements AciStoreIface {

        private Map<DismiIntentId, Set<AciIntentKeyStatus>> aciDatabase = Maps.newHashMap();

        @Override
        public Set<AciIntentKeyStatus> getKeys(DismiIntentId dismiID) {
            return aciDatabase.computeIfAbsent(dismiID, k -> Sets.newHashSet());
        }

        @Override
        public boolean removeDismiIntent(DismiIntentId dismiID) {
            aciDatabase.remove(dismiID);
            return true;
        }

        @Override
        public void updateKey(DismiIntentId dismiID, AciIntentKeyStatus key) {

        }

        @Override
        public void put(DismiIntentId dismiID, Set<AciIntentKeyStatus> keys) {
            aciDatabase.put(dismiID, keys);
        }

        @Override
        public Set<DismiIntentId> listDismiIntentId() {
            return aciDatabase.keySet();
        }

        @Override
        public void addKeyIntent(Key intentKey, org.onosproject.net.intent.Intent onosIntent) {

        }

        @Override
        public org.onosproject.net.intent.Intent removeIntentKey(Key intentKey) {
            return null;
        }

        @Override
        public void updateAbstractLinkList(DismiIntentId dismiIntentId, Key key, AbstractionLinkList abstractionLinks) {
            Set<AciIntentKeyStatus> aciIntentKeyStatuses = getKeys(dismiIntentId);

            if (aciIntentKeyStatuses.isEmpty()) {

                AciIntentKeyStatus newIntentKeyStatus = new AciIntentKeyStatus(key, IntentEvent.Type.INSTALL_REQ, false);
                newIntentKeyStatus.setAbstractionLinks(abstractionLinks);
                updateKey(dismiIntentId, newIntentKeyStatus);

            } else {

                Set<AciIntentKeyStatus> updatedaciIntentKeyStatuses = new HashSet<AciIntentKeyStatus>();

                boolean matching = false;

                for (AciIntentKeyStatus keyStatus : aciIntentKeyStatuses) {

                    if (keyStatus.getIntentKey().equals(key)) {

                        matching = true;

                        AciIntentKeyStatus aciIntentKeyStatus = new AciIntentKeyStatus();
                        aciIntentKeyStatus.setIntentKey(keyStatus.getIntentKey());
                        aciIntentKeyStatus.setStatus(keyStatus.getStatus());
                        aciIntentKeyStatus.setCalculated(keyStatus.isCalculated());
                        aciIntentKeyStatus.setAbstractionLinks(abstractionLinks);

                        updatedaciIntentKeyStatuses.add(aciIntentKeyStatus);
                    } else {
                        updatedaciIntentKeyStatuses.add(keyStatus);
                    }
                }

                if (!matching) {
                    AciIntentKeyStatus newIntentKeyStatus = new AciIntentKeyStatus(key, IntentEvent.Type.INSTALL_REQ, false);
                    newIntentKeyStatus.setAbstractionLinks(abstractionLinks);
                    updatedaciIntentKeyStatuses.add(newIntentKeyStatus);
                }

                put(dismiIntentId, updatedaciIntentKeyStatuses);
            }
        }
    }

    private class TestDismiStore implements DismiStoreIface {

        private Map<CpId, Set<EndPoint>> connectionPointMap = Maps.newHashMap();

        @Override
        public Set<ConnectionPoint> getConnectionPoints() {
            return null;
        }

        @Override
        public ConnectionPointList getConnectionPointsAsList() {
            return null;
        }

        @Override
        public boolean connectionPointExists(ConnectionPoint connectionPoint) {
            return false;
        }

        @Override
        public boolean updateConnectionPoint(ConnectionPoint currentCP, ConnectionPoint newCP, Set<EndPoint> endPointSet) {
            return false;
        }

        @Override
        public boolean updateConnectionPoint(ConnectionPoint cp1, ConnectionPoint cp2) {
            return false;
        }

        @Override
        public boolean deleteConnectionPoint(ConnectionPoint connectionPoint) {
            return false;
        }

        @Override
        public Set<EndPoint> getEndPoints(ConnectionPoint point) {

            Set<CpId> idSet = connectionPointMap.keySet();

            String name = point.getName();

            if (null != name) {
                for (CpId id : idSet) {
                    if (id.toString().compareTo(name) == 0) {
                        return connectionPointMap.get(id);
                    }
                }
            }

            return Sets.newHashSet();
        }

        @Override
        public EndPointList getEndPointsAsList(ConnectionPoint point) {

            Set<EndPoint> set = getEndPoints(point);
            EndPointList list = new EndPointList();
            list.addAll(set);

            return list;
        }

        @Override
        public void listEndPoints() {

        }

        @Override
        public void addEndPoint(ConnectionPoint connectionPoint, EndPoint e) {

            connectionPointMap.computeIfPresent(CpId.getId(connectionPoint.getName()),
                                                (cpId, endPoints) -> {
                                                    endPoints.add(e);
                                                    return endPoints;
                                                });

            connectionPointMap.computeIfAbsent(CpId.getId(connectionPoint.getName()), (cpId -> Sets.newHashSet(e)));

        }

        @Override
        public List<Service> getServicesAsList() {
            return null;
        }

        @Override
        public void removeAllServices() {

        }

        @Override
        public Resource addNewService(Service service) {
            return null;
        }

        @Override
        public Resource addServiceUpdate(Service service) {
            return null;
        }

        @Override
        public Service getOriginalService(String id) {
            return null;
        }

        @Override
        public Service getOriginalServiceUpdate(String id) {
            return null;
        }

        @Override
        public Service getResolvedService(String id) {
            return null;
        }

        @Override
        public boolean setResolvedService(String id, ServiceExtended service, Tracker tracker) {
            return false;
        }

        @Override
        public boolean setResolvedServiceUpdate(String id, ServiceExtended service, Tracker tracker) {
            return false;
        }

        @Override
        public void listServices() {

        }

        @Override
        public void placeOriginalWithUpdateService(String id, Service service, ServiceExtended resolvedService, Tracker tracker) {

        }

        @Override
        public Intent getOriginalIntent(String serviceId, String intentId) {
            return null;
        }

        @Override
        public boolean deleteService(String id) {
            return false;
        }

        @Override
        public boolean deleteService(String id, Service service, Tracker tracker) {
            return false;
        }
    }

    /**
     * Represents a fake IntentService class that easily allows to store and
     * retrieve intents without implementing the IntentService logic.
     */
    private class TestIntentService extends IntentServiceAdapter {

        private Set<org.onosproject.net.intent.Intent> intents;

        public TestIntentService() {
            intents = Sets.newHashSet();
        }

        @Override
        public void submit(org.onosproject.net.intent.Intent intent) {
            intents.add(intent);
        }

        @Override
        public void withdraw(org.onosproject.net.intent.Intent intent) {
            intents.clear();
        }

        @Override
        public long getIntentCount() {
            return intents.size();
        }

        @Override
        public Iterable<org.onosproject.net.intent.Intent> getIntents() {
            return intents;
        }

        @Override
        public org.onosproject.net.intent.Intent getIntent(Key intentKey) {
            for (org.onosproject.net.intent.Intent intent : intents) {
                if (intent.key().equals(intentKey)) {
                    return intent;
                }
            }
            return null;
        }
    }

    // Fake entity to give out hosts.
    private class TestHostService extends HostServiceAdapter {

        private Map<HostId, Host> hosts = new HashMap<>();

        public void addHost(IpAddress ipAddress, MacAddress macAddress) {
            HostId hostId = HostId.hostId(macAddress);
            ProviderId providerId = ProviderId.NONE;
            VlanId vlan = VlanId.NONE;
            HostLocation location = HostLocation.NONE;
            Set<IpAddress> ips = Sets.newHashSet(ipAddress);
            Annotations annotations = null;
            hosts.put(hostId, new DefaultHost(providerId, hostId, macAddress, vlan, location, ips, annotations));
        }

        @Override
        public Host getHost(HostId hostId) {
            return hosts.get(hostId);
        }

        @Override
        public Iterable<Host> getHosts() {
            return hosts.values();
        }
    }
}