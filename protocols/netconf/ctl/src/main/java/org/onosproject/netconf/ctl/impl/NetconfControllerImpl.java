/*
 * Copyright 2015-present Open Networking Foundation
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

package org.onosproject.netconf.ctl.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.onlab.packet.IpAddress;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.key.DeviceKey;
import org.onosproject.net.key.DeviceKeyId;
import org.onosproject.net.key.DeviceKeyService;
import org.onosproject.net.key.UsernamePassword;
import org.onosproject.netconf.NetConfNotification;
import org.onosproject.netconf.NetconfController;
import org.onosproject.netconf.NetconfDevice;
import org.onosproject.netconf.NetconfDeviceFactory;
import org.onosproject.netconf.NetconfDeviceInfo;
import org.onosproject.netconf.NetconfDeviceListener;
import org.onosproject.netconf.NetconfDeviceOutputEvent;
import org.onosproject.netconf.NetconfDeviceOutputEventListener;
import org.onosproject.netconf.NetconfException;
import org.onosproject.netconf.config.NetconfDeviceConfig;
import org.onosproject.netconf.config.NetconfSshClientLib;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Security;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.getIntegerProperty;
import static org.onlab.util.Tools.groupedThreads;

/**
 * The implementation of NetconfController.
 */
@Component(immediate = true)
@Service
public class NetconfControllerImpl implements NetconfController, NetConfNotification {

    private static final String ETHZ_SSH2 = "ethz-ssh2";

    protected static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;
    private static final String PROP_NETCONF_CONNECT_TIMEOUT = "netconfConnectTimeout";
    // FIXME @Property should not be static
    @Property(name = PROP_NETCONF_CONNECT_TIMEOUT, intValue = DEFAULT_CONNECT_TIMEOUT_SECONDS,
            label = "Time (in seconds) to wait for a NETCONF connect.")
    protected static int netconfConnectTimeout = DEFAULT_CONNECT_TIMEOUT_SECONDS;

    private static final String PROP_NETCONF_REPLY_TIMEOUT = "netconfReplyTimeout";
    protected static final int DEFAULT_REPLY_TIMEOUT_SECONDS = 5;
    // FIXME @Property should not be static
    @Property(name = PROP_NETCONF_REPLY_TIMEOUT, intValue = DEFAULT_REPLY_TIMEOUT_SECONDS,
            label = "Time (in seconds) waiting for a NetConf reply")
    protected static int netconfReplyTimeout = DEFAULT_REPLY_TIMEOUT_SECONDS;

    private static final String PROP_NETCONF_IDLE_TIMEOUT = "netconfIdleTimeout";
    protected static final int DEFAULT_IDLE_TIMEOUT_SECONDS = 300;
    // FIXME @Property should not be static
    @Property(name = PROP_NETCONF_IDLE_TIMEOUT, intValue = DEFAULT_IDLE_TIMEOUT_SECONDS,
            label = "Time (in seconds) SSH session will close if no traffic seen")
    protected static int netconfIdleTimeout = DEFAULT_IDLE_TIMEOUT_SECONDS;

    private static final String SSH_LIBRARY = "sshLibrary";
    private static final String APACHE_MINA_STR = "apache-mina";
    @Property(name = SSH_LIBRARY, value = APACHE_MINA_STR,
            label = "Ssh Library instead of apache_mina (i.e. ethz-ssh2")
    protected NetconfSshClientLib sshLibrary = NetconfSshClientLib.APACHE_MINA;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService cfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceKeyService deviceKeyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry netCfgService;

    public static final Logger log = LoggerFactory
            .getLogger(NetconfControllerImpl.class);

    private Map<DeviceId, NetconfDevice> netconfDeviceMap = new ConcurrentHashMap<>();

    private final NetconfDeviceOutputEventListener downListener = new DeviceDownEventListener();

    protected Set<NetconfDeviceListener> netconfDeviceListeners = new CopyOnWriteArraySet<>();
    protected NetconfDeviceFactory deviceFactory = new DefaultNetconfDeviceFactory();

    protected final ExecutorService executor =
            Executors.newCachedThreadPool(groupedThreads("onos/netconfdevicecontroller",
                                                         "connection-reopen-%d", log));

    @Activate
    public void activate(ComponentContext context) {
        cfgService.registerProperties(getClass());
        modified(context);
        Security.addProvider(new BouncyCastleProvider());
        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        netconfDeviceMap.values().forEach(device -> {
            device.getSession().removeDeviceOutputListener(downListener);
            device.disconnect();
        });
        cfgService.unregisterProperties(getClass(), false);
        netconfDeviceListeners.clear();
        netconfDeviceMap.clear();
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        log.info("Stopped");
    }

    @Modified
    public void modified(ComponentContext context) {
        if (context == null) {
            netconfReplyTimeout = DEFAULT_REPLY_TIMEOUT_SECONDS;
            netconfConnectTimeout = DEFAULT_CONNECT_TIMEOUT_SECONDS;
            netconfIdleTimeout = DEFAULT_IDLE_TIMEOUT_SECONDS;
            sshLibrary = NetconfSshClientLib.APACHE_MINA;
            log.info("No component configuration");
            return;
        }

        Dictionary<?, ?> properties = context.getProperties();

        String newSshLibrary;

        int newNetconfReplyTimeout = getIntegerProperty(
                properties, PROP_NETCONF_REPLY_TIMEOUT, netconfReplyTimeout);
        int newNetconfConnectTimeout = getIntegerProperty(
                properties, PROP_NETCONF_CONNECT_TIMEOUT, netconfConnectTimeout);
        int newNetconfIdleTimeout = getIntegerProperty(
                properties, PROP_NETCONF_IDLE_TIMEOUT, netconfIdleTimeout);

        newSshLibrary = get(properties, SSH_LIBRARY);

        if (newNetconfConnectTimeout < 0) {
            log.warn("netconfConnectTimeout is invalid - less than 0");
            return;
        } else if (newNetconfReplyTimeout <= 0) {
            log.warn("netconfReplyTimeout is invalid - 0 or less.");
            return;
        } else if (newNetconfIdleTimeout <= 0) {
            log.warn("netconfIdleTimeout is invalid - 0 or less.");
            return;
        }

        netconfReplyTimeout = newNetconfReplyTimeout;
        netconfConnectTimeout = newNetconfConnectTimeout;
        netconfIdleTimeout = newNetconfIdleTimeout;
        if (newSshLibrary != null) {
            sshLibrary = NetconfSshClientLib.getEnum(newSshLibrary);
        }
        log.info("Settings: {} = {}, {} = {}, {} = {}, {} = {}",
                 PROP_NETCONF_REPLY_TIMEOUT, netconfReplyTimeout,
                 PROP_NETCONF_CONNECT_TIMEOUT, netconfConnectTimeout,
                 PROP_NETCONF_IDLE_TIMEOUT, netconfIdleTimeout,
                 SSH_LIBRARY, sshLibrary);
    }

    @Override
    public void addDeviceListener(NetconfDeviceListener listener) {
        if (!netconfDeviceListeners.contains(listener)) {
            netconfDeviceListeners.add(listener);
        }
    }

    @Override
    public void removeDeviceListener(NetconfDeviceListener listener) {
        netconfDeviceListeners.remove(listener);
    }

    @Override
    public NetconfDevice getNetconfDevice(DeviceId deviceInfo) {
        return netconfDeviceMap.get(deviceInfo);
    }

    @Override
    public NetconfDevice getNetconfDevice(IpAddress ip, int port) {
        for (DeviceId info : netconfDeviceMap.keySet()) {
            if (info.uri().getSchemeSpecificPart().equals(ip.toString() + ":" + port)) {
                return netconfDeviceMap.get(info);
            }
        }
        return null;
    }

    @Override
    public NetconfDevice connectDevice(DeviceId deviceId) throws NetconfException {
        NetconfDeviceConfig netCfg  = netCfgService.getConfig(
                deviceId, NetconfDeviceConfig.class);
        NetconfDeviceInfo deviceInfo = null;

        if (netconfDeviceMap.containsKey(deviceId)) {
            log.debug("Device {} is already present", deviceId);
            return netconfDeviceMap.get(deviceId);
        } else if (netCfg != null) {
            log.debug("Device {} is present in NetworkConfig", deviceId);
            deviceInfo = new NetconfDeviceInfo(netCfg);

        } else {
            log.debug("Creating NETCONF device {}", deviceId);
            Device device = deviceService.getDevice(deviceId);
            String ip;
            int port;
            if (device != null) {
                ip = device.annotations().value("ipaddress");
                port = Integer.parseInt(device.annotations().value("port"));
            } else {
                String[] info = deviceId.toString().split(":");
                if (info.length == 3) {
                    ip = info[1];
                    port = Integer.parseInt(info[2]);
                } else {
                    ip = Arrays.asList(info).stream().filter(el -> !el.equals(info[0])
                            && !el.equals(info[info.length - 1]))
                            .reduce((t, u) -> t + ":" + u)
                            .get();
                    log.debug("ip v6 {}", ip);
                    port = Integer.parseInt(info[info.length - 1]);
                }
            }
            try {
                DeviceKey deviceKey = deviceKeyService.getDeviceKey(
                        DeviceKeyId.deviceKeyId(deviceId.toString()));
                if (deviceKey.type() == DeviceKey.Type.USERNAME_PASSWORD) {
                    UsernamePassword usernamepasswd = deviceKey.asUsernamePassword();

                    deviceInfo = new NetconfDeviceInfo(usernamepasswd.username(),
                                                       usernamepasswd.password(),
                                                       IpAddress.valueOf(ip),
                                                       port);

                } else if (deviceKey.type() == DeviceKey.Type.SSL_KEY) {
                    String username = deviceKey.annotations().value(AnnotationKeys.USERNAME);
                    String password = deviceKey.annotations().value(AnnotationKeys.PASSWORD);
                    String sshkey = deviceKey.annotations().value(AnnotationKeys.SSHKEY);

                    deviceInfo = new NetconfDeviceInfo(username,
                                                       password,
                                                       IpAddress.valueOf(ip),
                                                       port,
                                                       sshkey);
                } else {
                    log.error("Unknown device key for device {}", deviceId);
                }
            } catch (NullPointerException e) {
                throw new NetconfException("No Device Key for device " + deviceId, e);
            }
        }
        NetconfDevice netconfDevicedevice = createDevice(deviceInfo);
        netconfDevicedevice.getSession().addDeviceOutputListener(downListener);
        return netconfDevicedevice;
    }

    @Override
    public void disconnectDevice(DeviceId deviceId, boolean remove) {
        if (!netconfDeviceMap.containsKey(deviceId)) {
            log.warn("Device {} is not present", deviceId);
        } else {
            stopDevice(deviceId, remove);
        }
    }

    private void stopDevice(DeviceId deviceId, boolean remove) {
        netconfDeviceMap.get(deviceId).disconnect();
        netconfDeviceMap.remove(deviceId);
        if (remove) {
            for (NetconfDeviceListener l : netconfDeviceListeners) {
                l.deviceRemoved(deviceId);
            }
        }
    }

    @Override
    public void removeDevice(DeviceId deviceId) {
        if (!netconfDeviceMap.containsKey(deviceId)) {
            log.warn("Device {} is not present", deviceId);
            for (NetconfDeviceListener l : netconfDeviceListeners) {
                l.deviceRemoved(deviceId);
            }
        } else {
            netconfDeviceMap.remove(deviceId);
            for (NetconfDeviceListener l : netconfDeviceListeners) {
                l.deviceRemoved(deviceId);
            }
        }
    }

    private NetconfDevice createDevice(NetconfDeviceInfo deviceInfo) throws NetconfException {
        NetconfDevice netconfDevice = deviceFactory.createNetconfDevice(deviceInfo);
        netconfDeviceMap.put(deviceInfo.getDeviceId(), netconfDevice);
        for (NetconfDeviceListener l : netconfDeviceListeners) {
            l.deviceAdded(deviceInfo.getDeviceId());
        }
        return netconfDevice;
    }


    @Override
    public Map<DeviceId, NetconfDevice> getDevicesMap() {
        return netconfDeviceMap;
    }

    @Override
    public Set<DeviceId> getNetconfDevices() {
        return netconfDeviceMap.keySet();
    }

    @Override
    public void portChanged(DeviceId deviceId, PortDescription portDescription) {
        for (NetconfDeviceListener l : netconfDeviceListeners) {
            l.portChange(deviceId, portDescription);
        }
    }


    //Device factory for the specific NetconfDeviceImpl
    private class DefaultNetconfDeviceFactory implements NetconfDeviceFactory {

        @Override
        public NetconfDevice createNetconfDevice(NetconfDeviceInfo netconfDeviceInfo)
                throws NetconfException {
            if (NetconfSshClientLib.ETHZ_SSH2.equals(netconfDeviceInfo.sshClientLib().orElse(null)) ||
                    NetconfSshClientLib.ETHZ_SSH2.equals(sshLibrary)) {
                log.info("Creating NETCONF session to {} with {}",
                            netconfDeviceInfo.name(), NetconfSshClientLib.ETHZ_SSH2);
                return new DefaultNetconfDevice(netconfDeviceInfo,
                            new NetconfSessionImpl.SshNetconfSessionFactory());
            }
            log.info("Creating NETCONF session to {} with {}",
                    netconfDeviceInfo.getDeviceId(), NetconfSshClientLib.APACHE_MINA);
            return new DefaultNetconfDevice(netconfDeviceInfo);
        }
    }

    //Listener for closed session with devices, gets triggered when devices goes down
    // or sends the end pattern ]]>]]>
    private class DeviceDownEventListener implements NetconfDeviceOutputEventListener {

        @Override
        public void event(NetconfDeviceOutputEvent event) {
            DeviceId did = event.getDeviceInfo().getDeviceId();
            if (event.type().equals(NetconfDeviceOutputEvent.Type.DEVICE_UNREGISTERED)) {
                removeDevice(did);
            } else if (event.type().equals(NetconfDeviceOutputEvent.Type.SESSION_CLOSED)) {
                log.info("Trying to reestablish connection with device {}", did);
                executor.execute(() -> {
                    try {
                        NetconfDevice device = netconfDeviceMap.get(did);
                        if (device != null) {
                            device.getSession().checkAndReestablish();
                            log.info("Connection with device {} was reestablished", did);
                        } else {
                            log.warn("The device {} is not in the system", did);
                        }

                    } catch (NetconfException e) {
                        log.error("The SSH connection with device {} couldn't be " +
                                "reestablished due to {}. " +
                                "Marking the device as unreachable", e.getMessage());
                        log.debug("Complete exception: ", e);
                        removeDevice(did);
                    }
                });
            }
        }

        @Override
        public boolean isRelevant(NetconfDeviceOutputEvent event) {
            return getDevicesMap().containsKey(event.getDeviceInfo().getDeviceId());
        }
    }
}
