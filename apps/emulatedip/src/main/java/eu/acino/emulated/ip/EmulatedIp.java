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

/**
 * This class has been implemented for the ACINO emulated environment to make the IP and Optical emulated testbeds work together.
 * This class creates an IP link between two routers when an ACI-PP-Intent between two client ports of two ROADMS is installed.
 * If the ACI-PP-Intent is removed or failed, the corresponding IP link will be removed.
 */

package eu.acino.emulated.ip;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkProviderRegistry;
import org.onosproject.net.link.LinkProviderService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.ProbedLinkProvider;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;

import java.util.Optional;

import static org.onosproject.net.Device.Type.OTN;
import static org.onosproject.net.Device.Type.ROUTER;
import static org.onosproject.net.Device.Type.SWITCH;
import static org.onosproject.net.Link.Type;
import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class EmulatedIp extends AbstractProvider
        implements ProbedLinkProvider {

    private final Logger log = getLogger(getClass());

    private static final String PROVIDER_NAME =
            "org.onosproject.provider.emulatedLinkProvider";

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkProviderRegistry providerRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    private ApplicationId appId;
    private LinkProviderService providerService;
    private IntentListener internalIntentListener = new InternalIntentListener();

    public EmulatedIp() {
        super(new ProviderId("emulated", PROVIDER_NAME));
    }
    @Activate
    public void activate() {

        appId = coreService.registerApplication("eu.acino.emulatedip");
        providerService = providerRegistry.register(this);
        intentService.addListener(internalIntentListener);
        log.info("Started {}", appId.id());
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        intentService.removeListener(internalIntentListener);
        log.info("Stopped");
    }

    /**
     * This class listens to ACI-PP-Intents status changes and reacts by calling the relevant actions
     */
    private class InternalIntentListener implements IntentListener {

        @Override
        public void event(IntentEvent event) {
            //log.info("Event Type " + event.type() + " " + event.subject());

            switch(event.type()) {
                case INSTALLED:
                    if (event.subject() instanceof AciIntent) {
                        AciIntent intent = (AciIntent) event.subject();
                        log.info("INSTALLED ACiIntent {}", intent);
                        createLink(intent);

                    } /*else if (event.subject() instanceof ACIPPIntent) {

                        ACIPPIntent intent = (ACIPPIntent) event.subject();
                        log.info("INSTALLED ACiPPIntent {}", intent);
                        Device srcDevice = deviceService.getDevice(intent.src().deviceId());
                        Device dstDevice = deviceService.getDevice(intent.dst().deviceId());

                        if (srcDevice.type() == OTN && dstDevice.type() == OTN) {
                            if (intent.calculated()) {
                                createLink(intent);
                            }
                        }
                    }*/
                    break;
                case INSTALL_REQ:
                    if (event.subject() instanceof ACIPPIntent) {

                        ACIPPIntent intent = (ACIPPIntent) event.subject();

                        Device srcDevice = deviceService.getDevice(intent.src().deviceId());
                        Device dstDevice = deviceService.getDevice(intent.dst().deviceId());

                        if (srcDevice.type() == OTN && dstDevice.type() == OTN) {
                            if (intent.calculated()) {
                                log.info("Optical ACiPPIntent INSTALLATION_REQ {}", intent);
                                createLink(intent);
                            }
                        } /*else if (srcDevice.type() == ROUTER && dstDevice.type() == ROUTER) {
                            if (intent.calculated() && intent.resources().size() == 1) {
                                log.info("IP ACiPPIntent INSTALL_REQ {}", intent);
                                createLink(intent);
                            }
                        }*/
                    }
                    break;
                case WITHDRAWN:
                    if (event.subject() instanceof AciIntent) {
                        AciIntent intent = (AciIntent) event.subject();
                        log.info("WITHDRAWN ACiIntent {}", intent);
                        removeLink(intent);
                    } else if (event.subject() instanceof ACIPPIntent) {
                        ACIPPIntent intent = (ACIPPIntent) event.subject();
                        log.info("WITHDRAWN ACiPPIntent {}", intent);
                        removeLink(intent);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * This method creates the IP link given an ACI-PP-Intent between two client ports
     * @param intent the ACI-PP-Intent
     */
    private void createLink(Intent intent) {
        Optional<Pair<ConnectPoint, ConnectPoint>> connectPointPair = findConnectPoints(intent);

        if(connectPointPair.isPresent()) {

            DefaultAnnotations annotations = DefaultAnnotations.builder()
                    .set("layer", "IP")
                    .build();
            providerService.linkDetected(
                    new DefaultLinkDescription(connectPointPair.get().getLeft(),
                                               connectPointPair.get().getRight(),
                                               Type.INDIRECT,
                                               annotations));
            providerService.linkDetected(
                    new DefaultLinkDescription(connectPointPair.get().getRight(),
                                           connectPointPair.get().getLeft(),
                                           Type.INDIRECT,
                                           annotations));
        }
    }
    /**
     * This method removes the IP link given an ACI-PP-Intent between two client ports
     * @param intent the ACI-PP-Intent
     */
    private void removeLink(Intent intent) {
        Optional<Pair<ConnectPoint, ConnectPoint>> connectPointPair = findConnectPoints(intent);
        if(connectPointPair.isPresent()) {
            DefaultAnnotations annotations = DefaultAnnotations.builder()
                    .set("layer", "IP")
                    .build();
            providerService.linkVanished(
                    new DefaultLinkDescription(connectPointPair.get().getLeft(),
                                               connectPointPair.get().getRight(),
                                               Type.INDIRECT,
                                               annotations));
            providerService.linkVanished(
                    new DefaultLinkDescription(connectPointPair.get().getRight(),
                                               connectPointPair.get().getLeft(),
                                               Type.INDIRECT,
                                               annotations));
        }

    }

    /**
     * Returns the two connection points at the router layer given an ACI-PP-Intent between two ROADM client ports
     * @param intent the ACI-PP-Intent
     * @return If the two connection points are found, the method returns an Optional object which containd the two connection points stored as (src,dst). Otherwise the Optional object is empty.
     */
    private Optional<Pair<ConnectPoint, ConnectPoint>> findConnectPoints(Intent intent) {
        Device srcDev;
        Device dstDev;
        Host one;
        Host two;

        if (intent instanceof AciIntent) {

            AciIntent aciIntent = (AciIntent) intent;
            if (aciIntent.one() == null || aciIntent.two() == null) {
                log.warn("Intent src or dst are null of intent {}", intent);
                return Optional.empty();
            }

            try {
                one = hostService.getHost(aciIntent.one());
                two = hostService.getHost(aciIntent.two());
                srcDev = deviceService.getDevice(one.location().deviceId());
                dstDev = deviceService.getDevice(two.location().deviceId());
            } catch (Exception e) {
                log.error("Get device through an exception {}", e);
                return Optional.empty();
            }
        } else {

            ACIPPIntent acippIntent = (ACIPPIntent) intent;
            if (acippIntent.src() == null || acippIntent.dst() == null) {
                log.warn("Intent src or dst are null of intent {}", intent);
                return Optional.empty();
            }

            srcDev = deviceService.getDevice(acippIntent.src().deviceId());
            dstDev = deviceService.getDevice(acippIntent.dst().deviceId());

            if(srcDev.type() == OTN && dstDev.type() == OTN) {
                Optional<ConnectPoint> srcCP = linkService.getDeviceEgressLinks(srcDev.id()).stream()
                        .filter(x -> {
                            Device deviceToCheck = deviceService.getDevice(x.dst().deviceId());
                            return (deviceToCheck.type() == ROUTER || deviceToCheck.type() == SWITCH);
                        })
                        .map(x -> x.dst()).findAny();
                if (!srcCP.isPresent()) {
                    log.warn("Impossible to find Optical Intent source connection point");
                    return Optional.empty();
                }

                Optional<ConnectPoint> dstCP = linkService.getDeviceEgressLinks(dstDev.id()).stream()
                        .filter(x -> {
                            Device deviceToCheck = deviceService.getDevice(x.dst().deviceId());
                            return (deviceToCheck.type() == ROUTER || deviceToCheck.type() == SWITCH);
                        })
                        .map(x -> x.dst()).findAny();
                if (!dstCP.isPresent()) {
                    log.warn("Impossible to find corresponding Optical Intent destination connection point");
                    return Optional.empty();
                }
                return Optional.of(Pair.of(srcCP.get(), dstCP.get()));
            } else if(srcDev.type() == ROUTER && dstDev.type() == ROUTER) {
                return Optional.of(Pair.of(acippIntent.src(), acippIntent.dst()));
            }


        }

        if(srcDev.type() == ROUTER && dstDev.type() == ROUTER) {
            Optional<ConnectPoint> srcCP = linkService.getDeviceEgressLinks(srcDev.id()).stream()
                    .filter(x-> deviceService.getDevice(x.dst().deviceId()).type() == OTN)
                    .filter(x -> intent.resources().contains(x))
                    .map(x->x.src()).findAny();
            if(!srcCP.isPresent()) {
                log.warn("Impossible to find corresponding source connection point");
                return Optional.empty();
            }

            Optional<ConnectPoint> dstCP = linkService.getDeviceEgressLinks(dstDev.id()).stream()
                    .filter(x-> deviceService.getDevice(x.dst().deviceId()).type() == OTN)
                    .filter(x -> intent.resources().contains(x))
                    .map(x->x.src()).findAny();
            if(!dstCP.isPresent()) {
                log.warn("Impossible to find corresponding destination connection point");
                return Optional.empty();
            }

            return Optional.of(Pair.of(srcCP.get(), dstCP.get()));

        }
        return Optional.empty();
    }

}
