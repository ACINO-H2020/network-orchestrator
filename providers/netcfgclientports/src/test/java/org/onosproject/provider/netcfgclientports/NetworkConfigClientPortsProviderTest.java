/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.provider.netcfgclientports;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onosproject.core.CoreServiceAdapter;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultPort;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.LinkKey;
import org.onosproject.net.NetTestTools;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistryAdapter;
import org.onosproject.net.config.basics.BasicLinkConfig;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceServiceAdapter;
import org.onosproject.net.link.LinkProviderRegistryAdapter;
import org.onosproject.net.link.LinkProviderServiceAdapter;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit tests for the network config links provider.
 */
public class NetworkConfigClientPortsProviderTest {

    private NetworkConfigClientPortsProvider provider;

    private LinkProviderServiceAdapter providerService;
    private NetworkConfigListener configListener;
    private final TestNetworkConfigRegistry configRegistry =
            new TestNetworkConfigRegistry();

    static Device dev1 = NetTestTools.device("sw1");
    static Device dev2 = NetTestTools.device("sw2");
    static Device dev3 = NetTestTools.device("sw3");
    static PortNumber portNumber1 = PortNumber.portNumber(1);
    static PortNumber portNumber2 = PortNumber.portNumber(2);
    static PortNumber portNumber3 = PortNumber.portNumber(3);
    static ConnectPoint src = new ConnectPoint(dev1.id(), portNumber2);
    static ConnectPoint dst = new ConnectPoint(dev2.id(), portNumber2);

    static DeviceListener deviceListener;

    /**
     * Test device manager. Returns a known set of devices and ports.
     */
    static class TestDeviceManager extends DeviceServiceAdapter {

        @Override
        public Iterable<Device> getAvailableDevices() {
            return ImmutableList.of(dev1, dev2);
        }

        @Override
        public List<Port> getPorts(DeviceId deviceId) {
            return ImmutableList.of(new DefaultPort(dev1, portNumber1, true),
                                    new DefaultPort(dev2, portNumber2, true));
        }

        @Override
        public void addListener(DeviceListener listener) {
            deviceListener = listener;
        }

        @Override
        public Device getDevice(DeviceId deviceId) {
            if (deviceId.equals(dev1.id())) {
                return dev1;
            } else {
                return dev2;
            }
        }
    }

    /**
     * Test network config registry. Captures the network config listener from
     * the service under test.
     */
    private final class TestNetworkConfigRegistry
            extends NetworkConfigRegistryAdapter {


        @Override
        public void addListener(NetworkConfigListener listener) {
            configListener = listener;
        }
    }

    /**
     * Sets up a network config links provider under test and the services
     * required to run it.
     */
    @Before
    public void setUp() {
        provider = new NetworkConfigClientPortsProvider();

        provider.coreService = new CoreServiceAdapter();
        LinkProviderRegistryAdapter linkRegistry =
                new LinkProviderRegistryAdapter();
        provider.providerRegistry = linkRegistry;
        provider.deviceService = new TestDeviceManager();
        provider.netCfgService = configRegistry;

        provider.activate();

        providerService = linkRegistry.registeredProvider();
    }

    /**
     * Tears down the provider under test.
     */
    @After
    public void tearDown() {
        provider.deactivate();
    }

    /**
     * Tests that a network config links provider object can be created.
     * The actual creation is done in the setUp() method.
     */
    @Test
    public void testCreation() {
        assertThat(provider, notNullValue());
        assertThat(provider.configuredLinks, empty());
    }

    /**
     * Tests loading of devices from the device manager.
     */
    @Test
    public void testDeviceLoad() {
        assertThat(provider, notNullValue());
        //assertThat(provider.discoverers.entrySet(), hasSize(2));
    }

    /**
     * Tests removal of a link from the configuration.
     */
    @Test
    public void testRemoveLink() {
        LinkKey key = LinkKey.linkKey(src, dst);
        configListener.event(new NetworkConfigEvent(NetworkConfigEvent.Type.CONFIG_ADDED,
                                                    key,
                                                    BasicLinkConfig.class));

        assertThat(provider.configuredLinks, hasSize(1));

        configListener.event(new NetworkConfigEvent(NetworkConfigEvent.Type.CONFIG_REMOVED,
                                                    key,
                                                    BasicLinkConfig.class));
        assertThat(provider.configuredLinks, hasSize(0));
    }

    /**
     * Tests adding a new device via an event.
     */
    @Test
    public void testAddDevice() {
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.DEVICE_ADDED, dev3));
        //assertThat(provider.discoverers.entrySet(), hasSize(3));
    }

    /**
     * Tests adding a new port via an event.
     */
    @Test
    public void testAddPort() {
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.PORT_ADDED, dev3,
                                             new DefaultPort(dev3, portNumber3, true)));
        //assertThat(provider.discoverers.entrySet(), hasSize(3));
    }

    /**
     * Tests removing a device via an event.
     */
    @Test
    public void testRemoveDevice() {
        //assertThat(provider.discoverers.entrySet(), hasSize(2));
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.DEVICE_ADDED, dev3));
        //assertThat(provider.discoverers.entrySet(), hasSize(3));
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.DEVICE_REMOVED, dev3));
        //assertThat(provider.discoverers.entrySet(), hasSize(2));
    }

    /**
     * Tests removing a port via an event.
     */
    @Test
    public void testRemovePort() {
        //assertThat(provider.discoverers.entrySet(), hasSize(2));
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.PORT_ADDED, dev3,
                                             new DefaultPort(dev3, portNumber3, true)));
        //assertThat(provider.discoverers.entrySet(), hasSize(3));
        deviceListener.event(new DeviceEvent(DeviceEvent.Type.PORT_REMOVED, dev3,
                                             new DefaultPort(dev3, portNumber3, true)));
        //assertThat(provider.discoverers.entrySet(), hasSize(3));
    }
}
