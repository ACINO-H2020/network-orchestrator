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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.LinkKey;
import org.onosproject.net.Port;
import org.onosproject.net.config.NetworkConfigEvent;
import org.onosproject.net.config.NetworkConfigListener;
import org.onosproject.net.config.NetworkConfigRegistry;
import org.onosproject.net.config.basics.BasicLinkConfig;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Provider to pre-discover links and devices based on a specified network
 * config.
 */

@Component(immediate = true)
public class NetworkConfigClientPortsProvider
        extends AbstractProvider
        implements ProbedLinkProvider {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetworkConfigRegistry netCfgService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    private LinkProviderService providerService;

    private static final String PROVIDER_NAME =
            "org.onosproject.provider.netcfgclientports";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ApplicationId appId;

    private final InternalDeviceListener deviceListener = new InternalDeviceListener();
    private final InternalConfigListener cfgListener = new InternalConfigListener();

    protected Set<LinkKey> configuredLinks = new HashSet<>();

    public NetworkConfigClientPortsProvider() {
        super(new ProviderId("ipopto", PROVIDER_NAME));
    }

    private void loadLinks() {
        netCfgService.getSubjects(LinkKey.class)
                .forEach(linkKey -> configuredLinks.add(linkKey));
    }

    @Activate
    protected void activate() {
        log.info("Activated");
        appId = coreService.registerApplication(PROVIDER_NAME);
        providerService = providerRegistry.register(this);
        deviceService.addListener(deviceListener);
        netCfgService.addListener(cfgListener);
        loadLinks();
        createLinks();
    }

    @Deactivate
    protected void deactivate() {
        deviceService.removeListener(deviceListener);
        netCfgService.removeListener(cfgListener);
        providerRegistry.unregister(this);
        providerService = null;
        log.info("Deactivated");
    }

    /**
     * Loads available devices and registers their ports to be probed.
     */
    private void createLinks() {

        configuredLinks.forEach(this::updateLinks);
    }

    private void updateLinks(ConnectPoint connectPoint) {

        configuredLinks.stream()
                .filter(linkKey -> {
                    Device sourceDev = deviceService.getDevice(linkKey.src().deviceId());
                    Device destinationDev = deviceService.getDevice(linkKey.dst().deviceId());

                    if (sourceDev == null || destinationDev == null) {
                        return false;
                    }
                    // Checking if the corresponding source/destination is reachable
                    return ((linkKey.src().equals(connectPoint) && deviceService.getPort(linkKey.dst()) != null) ||
                            (linkKey.dst().equals(connectPoint) && deviceService.getPort(linkKey.src()) != null));
                })
                .forEach(linkKey -> {
                    DefaultLinkDescription linkDescription =
                            new DefaultLinkDescription(linkKey.src(), linkKey.dst(),
                                                       Link.Type.DIRECT);
                    providerService.linkDetected(linkDescription);
                });

    }

    private void updateLinks(LinkKey linkKey) {

        Device sourceDev = deviceService.getDevice(linkKey.src().deviceId());
        Device destinationDev = deviceService.getDevice(linkKey.dst().deviceId());

        if (sourceDev == null || destinationDev == null) {
            return;
        }

        if (sourceDev.type() == Device.Type.ROUTER && destinationDev.type() == Device.Type.OTN) {
            DefaultLinkDescription linkDescription =
                    new DefaultLinkDescription(linkKey.src(), linkKey.dst(),
                                               Link.Type.DIRECT);
            providerService.linkDetected(linkDescription);
        } else if (sourceDev.type() == Device.Type.OTN && destinationDev.type() == Device.Type.ROUTER) {
            DefaultLinkDescription linkDescription =
                    new DefaultLinkDescription(linkKey.src(), linkKey.dst(),
                                               Link.Type.DIRECT);
            providerService.linkDetected(linkDescription);
        }

    }
    /**
     * Processes device events.
     */
    private class InternalDeviceListener implements DeviceListener {
        @Override
        public void event(DeviceEvent event) {
            if (event.type() == DeviceEvent.Type.PORT_STATS_UPDATED) {
                return;
            }
            Device device = event.subject();

            log.trace("{} {} {}", event.type(), event.subject(), event);

            if (device.type() != Device.Type.ROUTER && device.type() != Device.Type.OTN) {
                return;
            }

            Port port = event.port();
            if (device == null) {
                log.error("Device is null.");
                return;
            }


            switch (event.type()) {
                case PORT_ADDED:
                    updateLinks(new ConnectPoint(port.element().id(),
                                                 port.number()));
                    break;
                case PORT_UPDATED:
                    if (port.isEnabled()) {
                        updateLinks(new ConnectPoint(port.element().id(),
                                                     port.number()));
                    } else {
                        providerService.linksVanished(new ConnectPoint(port.element().id(),
                                                                       port.number()));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class InternalConfigListener implements NetworkConfigListener {

        private void addLink(LinkKey linkKey) {
            configuredLinks.add(linkKey);
            updateLinks(linkKey);
        }

        private void removeLink(LinkKey linkKey) {
            DefaultLinkDescription linkDescription =
                    new DefaultLinkDescription(linkKey.src(), linkKey.dst(),
                                               Link.Type.DIRECT);
            configuredLinks.remove(linkKey);
            providerService.linkVanished(linkDescription);
        }

        @Override
        public void event(NetworkConfigEvent event) {
            if (event.configClass().equals(BasicLinkConfig.class)) {
                log.debug("net config event of type {} for basic link {}",
                         event.type(), event.subject());
                LinkKey linkKey = (LinkKey) event.subject();
                if (event.type() == NetworkConfigEvent.Type.CONFIG_ADDED) {
                    addLink(linkKey);
                } else if (event.type() == NetworkConfigEvent.Type.CONFIG_REMOVED) {
                    removeLink(linkKey);
                }
                log.info("Link reconfigured");
            }
        }
    }

}
