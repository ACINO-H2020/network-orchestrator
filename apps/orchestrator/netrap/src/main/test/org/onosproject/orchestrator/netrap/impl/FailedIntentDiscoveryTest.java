/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.orchestrator.netrap.impl;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onlab.graph.ScalarWeight;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.core.IdGenerator;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultDevice;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.ProviderId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.onosproject.net.Device.Type.ROADM;
import static org.onosproject.net.Link.State;
import static org.onosproject.net.Link.Type.INDIRECT;
import static org.onosproject.net.link.LinkEvent.Type.LINK_REMOVED;
import static org.onosproject.net.link.LinkEvent.Type.LINK_UPDATED;


public class FailedIntentDiscoveryTest {
    private FailedIntentDiscovery failedIntentDiscovery = new FailedIntentDiscovery();

    //Services
    private DeviceService deviceService;
    private LinkService linkService;
    private IntentService intentService;
    private Capture<LinkListener> linkListCapture;
    private Capture<DeviceListener> devListCapture;

    //Init
    private IdGenerator idGenerator;
    private final ApplicationId appId = new DefaultApplicationId(0, "NetRapFailureTest");

    //Devices for testing
    private static final DeviceId DEVICE_ID_1 = DeviceId.deviceId("test:1");
    private static final DeviceId DEVICE_ID_2 = DeviceId.deviceId("test:2");
    private static final DeviceId DEVICE_ID_3 = DeviceId.deviceId("test:3");
    private static final Device dev3 = new DefaultDevice(ProviderId.NONE, DEVICE_ID_3, ROADM, null, null, null, null, null);

    //Connection points
    private static final ConnectPoint CP11 = new ConnectPoint(DEVICE_ID_1, PortNumber.portNumber(1));
    private static final ConnectPoint CP12 = new ConnectPoint(DEVICE_ID_1, PortNumber.portNumber(2));
    private static final ConnectPoint CP21 = new ConnectPoint(DEVICE_ID_2, PortNumber.portNumber(1));
    private static final ConnectPoint CP22 = new ConnectPoint(DEVICE_ID_2, PortNumber.portNumber(2));
    private static final ConnectPoint CP31 = new ConnectPoint(DEVICE_ID_3, PortNumber.portNumber(1));
    private static final ConnectPoint CP32 = new ConnectPoint(DEVICE_ID_3, PortNumber.portNumber(2));

    //Links
    private static Link link11_21 = DefaultLink.builder().src(CP11).dst(CP21).providerId(ProviderId.NONE)
            .state(State.ACTIVE).type(INDIRECT).build();
    private static Link link11_22 = DefaultLink.builder().src(CP11).dst(CP22).providerId(ProviderId.NONE)
            .state(State.ACTIVE).type(INDIRECT).build();
    private static Link link12_21 = DefaultLink.builder().src(CP12).dst(CP21).providerId(ProviderId.NONE)
            .state(State.ACTIVE).type(INDIRECT).build();
    private static Link link22_31 = DefaultLink.builder().src(CP22).dst(CP31).providerId(ProviderId.NONE)
            .state(State.ACTIVE).type(INDIRECT).build();

    //Intents
    private final Key key1 = Key.of(1, appId);
    private final Key key2 = Key.of(2, appId);
    private final Key key3 = Key.of(3, appId);
    private final Key key4 = Key.of(4, appId);
    private final Key key5 = Key.of(5, appId);

    private static final List<Intent> intents = new ArrayList<>();


    @Before
    public void setUp() throws Exception {

        //Init device service
        deviceService = createMock(DeviceService.class);
        devListCapture = Capture.newInstance();
        failedIntentDiscovery.deviceService = deviceService;
        deviceService.addListener(capture(devListCapture));
        expectLastCall();
        replay(deviceService);

        //Init link service
        linkService = createMock(LinkService.class);
        failedIntentDiscovery.linkService = linkService;
        linkListCapture = Capture.newInstance();
        linkService.addListener(capture(linkListCapture));
        expectLastCall();
        replay(linkService);

        //Init intent service
        intentService = createMock(IntentService.class);
        failedIntentDiscovery.intentService = intentService;

        idGenerator = new IdGenerator() {
            int counter = 1;

            @Override
            public long getNewId() {
                return counter++;
            }
        };
        Intent.unbindIdGenerator(idGenerator);
        Intent.bindIdGenerator(idGenerator);

        //Init Intents
        ACIPPIntent intent1 = ACIPPIntent.builder().src(CP11).dst(CP21).key(key1)
                .path(new DefaultPath(ProviderId.NONE,
                                      Collections.singletonList(link11_21),
                                      new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .calculated(true)
                .appId(appId).build();

        ACIPPIntent intent2 = ACIPPIntent.builder().src(CP11).dst(CP31).key(key2)
                .path(new DefaultPath(ProviderId.NONE,
                                      Arrays.asList(link11_21, link22_31),
                                      new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .calculated(true)
                .appId(appId).build();

        ACIPPIntent intent3 = ACIPPIntent.builder().src(CP12).dst(CP32).key(key3)
                .path(new DefaultPath(ProviderId.NONE,
                                      Arrays.asList(link12_21, link22_31),
                                      new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .calculated(true)
                .appId(appId).build();
        ACIPPIntent intent4 = ACIPPIntent.builder().src(CP12).dst(CP32).key(key4)
                .path(new DefaultPath(ProviderId.NONE,
                                      Arrays.asList(link12_21, link22_31),
                                      new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .backupPath(new DefaultPath(ProviderId.NONE,
                                            Collections.singletonList(link11_22),
                                            new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .calculated(true)
                .appId(appId).build();
        ACIPPIntent intent5 = ACIPPIntent.builder().src(CP12).dst(CP32).key(key5)
                .path(new DefaultPath(ProviderId.NONE,
                                      Arrays.asList(link11_21, link22_31),
                                      new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .backupPath(new DefaultPath(ProviderId.NONE,
                                            Collections.singletonList(link11_21),
                                            new ScalarWeight(0), DefaultAnnotations.EMPTY))
                .calculated(true)
                .appId(appId).build();
        intents.add(intent1);
        intents.add(intent2);
        intents.add(intent3);
        intents.add(intent4);
        intents.add(intent5);

        //Activate failedIntentDisovery
        failedIntentDiscovery.activate();
        reset(deviceService);
        reset(linkService);

    }

    @After
    public void tearDown() throws Exception {
        intents.clear();
        Intent.unbindIdGenerator(idGenerator);
    }

    @Test
    public void testLinkFailure() {
        expect(intentService.getIntents()).andReturn(Collections.unmodifiableList(intents));
        Capture<ACIPPIntent> intentCapture = Capture.newInstance(CaptureType.ALL);
        intentService.submit(capture(intentCapture));
        expectLastCall().times(2);
        replay(intentService);
        linkListCapture.getValue().event(new LinkEvent(LINK_REMOVED, link11_21));
        verify(intentService);

        List<Key> intentsToUpdate = new ArrayList<>(Arrays.asList(key1, key2));

        intentCapture.getValues().forEach(x -> {
                                              assertTrue("The intent to be updates are " + key1 + " and " + key2,
                                                         intentsToUpdate.remove(x.key()));
                                              assertNull("Path should be null for intent " + x, x.path());
                                              assertFalse("Calculated should be false for intent " + x, x.calculated());
                                          }
        );
        assertEquals("Not all the intents have been updated", 0, intentsToUpdate.size());
    }

    @Test
    public void testLinkInactive() {
        expect(intentService.getIntents()).andReturn(Collections.unmodifiableList(intents));
        Capture<ACIPPIntent> intentCapture = Capture.newInstance(CaptureType.ALL);
        intentService.submit(capture(intentCapture));
        expectLastCall().times(2);
        replay(intentService);
        Link link11_21_inactive = DefaultLink.builder().src(CP11).dst(CP21).providerId(ProviderId.NONE)
                .state(State.INACTIVE).type(INDIRECT).build();
        linkListCapture.getValue().event(new LinkEvent(LINK_UPDATED, link11_21_inactive));
        verify(intentService);

        List<Key> intentsToUpdate = new ArrayList<>(Arrays.asList(key1, key2));

        intentCapture.getValues().forEach(x -> {
                                              assertTrue("The intent to be updates are " + key1 + " and " + key2,
                                                         intentsToUpdate.remove(x.key()));
                                              assertNull("Path should be null for intent " + x, x.path());
                                              assertFalse("Calculated should be false for intent " + x, x.calculated());
                                          }
        );
        assertEquals("Not all the intents have been updated", 0, intentsToUpdate.size());
    }

    @Test
    public void testDeviceFailure() {
        expect(intentService.getIntents()).andReturn(Collections.unmodifiableList(intents));
        Capture<ACIPPIntent> intentCapture = Capture.newInstance(CaptureType.ALL);
        intentService.submit(capture(intentCapture));
        expectLastCall().times(2);
        replay(intentService);
        devListCapture.getValue().event(new DeviceEvent(DeviceEvent.Type.DEVICE_REMOVED, dev3));

        verify(intentService);

        List<Key> intentsToUpdate = new ArrayList<>(Arrays.asList(key2, key3));

        intentCapture.getValues().forEach(x -> {
                                              assertTrue("The intent to be updates are " + key2 + " and " + key3,
                                                         intentsToUpdate.remove(x.key()));
                                              assertNull("Path should be null for intent " + x, x.path());
                                              assertFalse("Calculated should be false for intent " + x, x.calculated());
                                          }
        );
        assertEquals("Not all the intents have been updated", 0, intentsToUpdate.size());
    }

    @Test
    public void testDeviceUnAvailable() {
        expect(intentService.getIntents()).andReturn(Collections.unmodifiableList(intents));
        Capture<ACIPPIntent> intentCapture = Capture.newInstance(CaptureType.ALL);
        intentService.submit(capture(intentCapture));
        expectLastCall().times(2);
        replay(intentService);

        expect(deviceService.isAvailable(dev3.id())).andReturn(false);
        replay(deviceService);

        devListCapture.getValue().event(new DeviceEvent(DeviceEvent.Type.DEVICE_AVAILABILITY_CHANGED, dev3));

        verify(intentService);

        List<Key> intentsToUpdate = new ArrayList<>(Arrays.asList(key2, key3));

        intentCapture.getValues().forEach(x -> {
                                              assertTrue("The intent to be updates are " + key2 + " and " + key3,
                                                         intentsToUpdate.remove(x.key()));
                                              assertNull("Path should be null for intent " + x, x.path());
                                              assertFalse("Calculated should be false for intent " + x, x.calculated());
                                          }
        );
        assertEquals("Not all the intents have been updated", 0, intentsToUpdate.size());
    }

}