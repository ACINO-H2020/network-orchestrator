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
package org.onosproject.net.optical.disaggregator;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.packet.ChassisId;
import org.onlab.util.Frequency;
import org.onosproject.net.Annotations;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.Device.Type;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.optical.device.DisaggregatedType;
import org.onosproject.net.optical.device.RoadmArchitecture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.onosproject.net.AnnotationKeys.PORT_NAME;
import static org.onosproject.net.DefaultAnnotations.merge;
import static org.onosproject.net.Device.Type.OTN;
import static org.onosproject.net.Device.Type.ROADM;
import static org.onosproject.net.Link.Type.OPTICAL;
import static org.onosproject.net.Port.Type.OCH;
import static org.onosproject.net.Port.Type.OMS;
import static org.onosproject.net.optical.device.DisaggregatedType.BOTTOM_ROADM;
import static org.onosproject.net.optical.device.DisaggregatedType.DISAGGREGATED_TYPE_ANNOTATION;
import static org.onosproject.net.optical.device.DisaggregatedType.TOP_ROADM;
import static org.onosproject.net.optical.device.DisaggregatedType.TRANSPONDER;
import static org.onosproject.net.optical.device.DisaggregatedType.WSS_ROADM;
import static org.onosproject.net.optical.device.OmsPortHelper.omsPortDescription;
import static org.onosproject.net.optical.device.RoadmArchitecture.ARCH_ANNOTATION;
import static org.onosproject.net.optical.device.RoadmArchitecture.BOTTOM_ROADM_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.CDC;
import static org.onosproject.net.optical.device.RoadmArchitecture.ORIGINAL_NODE_ANNOTATION;
import static org.onosproject.net.optical.device.RoadmArchitecture.TOP_ROADM_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.TRANSPONDER_ID;
import static org.onosproject.net.optical.device.RoadmArchitecture.WSS_ROADM_ID;
import static org.onosproject.net.optical.device.port.ClientPortMapper.DST_PORT_NAME_ANNOTATION;
import static org.onosproject.net.optical.device.port.ClientPortMapper.NETWORK_PORT;
import static org.onosproject.net.optical.device.port.ClientPortMapper.SRC_PORT_NAME_ANNOTATION;

@Service
@Component(immediate = true)
@Beta
public class DisaggregatorServiceImpl implements DisaggregatorService {

    private static final Logger LOG =
            LoggerFactory.getLogger(DisaggregatorServiceImpl.class);
    //FIXME: Default value to be checked
    private static final Frequency DEFAULT_MIN_FREQ = Frequency.ofTHz(184.50);
    private static final Frequency DEFAULT_MAX_FREQ = Frequency.ofTHz(195.90);
    private static final Frequency DEFAULT_GRID_SPACE = Frequency.ofGHz(50.0);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    public DisaggregatorServiceImpl() {
    }

    @Override
    public Set<DeviceId> getDisaggregatedDevIds(DeviceId devId,
                                                RoadmArchitecture roadmArchitecture,
                                                List<PortDescription> portDescriptions) {

        switch (roadmArchitecture) {
            case CDC:
                return colorDirectionContentionLessIds(devId, portDescriptions.stream()
                        .filter(port -> port.type() == OCH)
                        .collect(Collectors.toList()));
            case C:
                LOG.warn("Not supported");
                break;
            case D:
                return directionLessIds(devId, portDescriptions.stream()
                        .filter(port -> port.type() == OCH)
                        .collect(Collectors.toList()));
            case CD:
                LOG.warn("Not supported");
                break;
            default:
                LOG.warn("Not supported");
                break;

        }
        return Collections.EMPTY_SET;
    }

    private Set<DeviceId> directionLessIds(DeviceId devId, List<PortDescription> networkPorts) {
        // - for each network ports: 1 T (devId + Tx)
        // - top (devId + top)
        // - bottom (devId + bottom)
        Set<DeviceId> devIds = new HashSet<>();

        devIds.addAll(getTransponderId(devId, networkPorts));

        devIds.add(DeviceId.deviceId(devId.toString() + "_" + BOTTOM_ROADM_ID));
        devIds.add(DeviceId.deviceId(devId.toString() + "_" + TOP_ROADM_ID));
        return devIds;
    }

    private Set<DeviceId> colorDirectionContentionLessIds(DeviceId devId, List<PortDescription> networkPorts) {
        // - for each network ports: 1 T (devId + Tx)
        // - one node wss
        Set<DeviceId> devIds = new HashSet<>();

        devIds.addAll(getTransponderId(devId, networkPorts));

        devIds.add(DeviceId.deviceId(devId.toString() + "_" + WSS_ROADM_ID));
        return devIds;
    }

    /**
     * Helper method to extract device ids of the transponder based on network ports.
     *
     * @param devId device id the original device
     * @param networkPorts network ports of the original device
     * @return a set of transponder device ids
     */
    private Set<DeviceId> getTransponderId(DeviceId devId, List<PortDescription> networkPorts) {
        Set<DeviceId> devIds = new HashSet<>();
        for (int index = 0; index < networkPorts.size(); index++) {
            devIds.add(DeviceId.deviceId(devId.toString() + "_" + TRANSPONDER_ID + "_" + index));
        }
        return devIds;
    }

    @Override
    public DeviceDescription getDisaggregatedDevDescription(DeviceId disaggregatedDevId,
                                                            DeviceDescription deviceDescription) {
        try {
            RoadmArchitecture roadmArchitecture = RoadmArchitecture.valueOf(
                    deviceDescription.annotations().value(ARCH_ANNOTATION));
            switch (roadmArchitecture) {
                case CDC:
                    return disaggregatedDescription(disaggregatedDevId, deviceDescription);

                case C:
                    LOG.warn("Not supported");
                    break;
                case D:
                    return disaggregatedDescription(disaggregatedDevId, deviceDescription);
                case CD:
                    LOG.warn("Not supported");
                    break;
                default:
                    LOG.warn("Not supported");
                    break;

            }
        } catch (IllegalArgumentException e) {
            LOG.error("Failed disaggreation process", e);
            throw e;
        }
        return deviceDescription;
    }

    private DeviceDescription disaggregatedDescription(DeviceId disaggregatedDevId,
                                                       DeviceDescription deviceDescription) {

        // - for each network port: 1 Transponder (type OTN)
        // - one top ROADM (type ROADM)
        // - one bottom ROADM: type ROADM

        DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();

        String disDevIdString = disaggregatedDevId.toString();
        if (disDevIdString.contains(BOTTOM_ROADM_ID)) {
            annotations.set(DISAGGREGATED_TYPE_ANNOTATION, BOTTOM_ROADM.toString());
            return getDeviceDescription(disaggregatedDevId, deviceDescription, annotations, disDevIdString, ROADM);
        }
        if (disDevIdString.contains(TOP_ROADM_ID)) {
            annotations.set(DISAGGREGATED_TYPE_ANNOTATION, TOP_ROADM.toString());
            return getDeviceDescription(disaggregatedDevId, deviceDescription, annotations, disDevIdString, ROADM);
        }

        if (disDevIdString.contains(WSS_ROADM_ID)) {
            annotations.set(DISAGGREGATED_TYPE_ANNOTATION, WSS_ROADM.toString());
            return getDeviceDescription(disaggregatedDevId, deviceDescription, annotations, disDevIdString, ROADM);
        }

        if (disDevIdString.contains("_" + TRANSPONDER_ID + "_")) {
            annotations.set(DISAGGREGATED_TYPE_ANNOTATION, TRANSPONDER.toString());
            return getDeviceDescription(disaggregatedDevId, deviceDescription, annotations, disDevIdString, OTN);
        }

        LOG.warn("Disaggregation process failed for ROADM {}: missing information", disaggregatedDevId);
        return deviceDescription;
    }


    private DeviceDescription getDeviceDescription(DeviceId disaggregatedDevId,
                                                   DeviceDescription deviceDescription,
                                                   DefaultAnnotations.Builder annotations,
                                                   String disDevIdString,
                                                   Type type) {
        annotations.set("name", disDevIdString);
        annotations.set(ORIGINAL_NODE_ANNOTATION, deviceDescription.deviceUri().toString());
        return new DefaultDeviceDescription(disaggregatedDevId.uri(),
                type,
                deviceDescription.manufacturer(),
                deviceDescription.hwVersion(),
                deviceDescription.swVersion(),
                "",
                new ChassisId(),
                merge(annotations.build(), deviceDescription.annotations()));
    }

    @Override
    //FIXME: method too long: split in mutiple methods
    public List<PortDescription> getDisaggregatedPortDescriptions(DeviceId disaggregatedDevId,
                                                                  List<PortDescription> portDescs) {
        Device dev = deviceService.getDevice(disaggregatedDevId);
        DisaggregatedType disaggregatedType = getDisaggregatedType(dev);

        List<PortDescription> linePortDescs = portDescs.stream().filter(port -> port.type() == OMS)
                .sorted(Comparator.comparingLong(p -> p.portNumber().toLong())).collect(Collectors.toList());

        //Bottom ROADM: one line port (OMS) to be created + line ports (OMS) that are given
        if (disaggregatedType == BOTTOM_ROADM) {
            rewritePortNumber(linePortDescs, 2);
            List<PortDescription> bottomDescs = new ArrayList<>(linePortDescs);
            bottomDescs.add(omsPortDescription(
                    PortNumber.portNumber(1),
                    true,
                    DEFAULT_MIN_FREQ,
                    DEFAULT_MAX_FREQ,
                    DEFAULT_GRID_SPACE
            ));
            return bottomDescs;
        }

        List<PortDescription> networkPortDescs = portDescs.stream().filter(port -> port.type() == OCH)
                .sorted(Comparator.comparingLong(p -> p.portNumber().toLong())).collect(Collectors.toList());

        //Top ROADM: one line port (OMS) for each Transponder (network port) +
        //one line port (OMS).
        //Ports are networkPort + 1
        if (disaggregatedType == TOP_ROADM) {
            List<PortDescription> topDescs = new ArrayList<>();
            for (int i = 1; i <= networkPortDescs.size() + 1; i++) {
                topDescs.add(omsPortDescription(
                        PortNumber.portNumber(i),
                        true,
                        DEFAULT_MIN_FREQ,
                        DEFAULT_MAX_FREQ,
                        DEFAULT_GRID_SPACE
                ));
            }
            return topDescs;
        }

        //WSS ROADM: one line port (OMS) for each Transponder (network port) [1, network port size]
        // + line ports (OMS) that are given [networt size +1, line port size]
        if(disaggregatedType == WSS_ROADM) {
            rewritePortNumber(linePortDescs, networkPortDescs.size() + 1);
            List<PortDescription> wssDescs = new ArrayList<>(linePortDescs);
            for (int i = 1; i <= networkPortDescs.size(); i++) {
                wssDescs.add(omsPortDescription(
                        PortNumber.portNumber(i),
                        true,
                        DEFAULT_MIN_FREQ,
                        DEFAULT_MAX_FREQ,
                        DEFAULT_GRID_SPACE
                ));
            }
            return wssDescs;
        }


        //for each T, get 1 or more client port + 1 netport
        if (disaggregatedType == TRANSPONDER) {

            List<PortDescription> clientPortDescs = portDescs.stream().filter(port ->
                    port.type() != OCH && port.type() != OMS)
                    .sorted(Comparator.comparingLong(p -> p.portNumber().toLong()))
                    .collect(Collectors.toList());


            //Port annotations value NETWORK_PORT -> set of PortDescription
            Map<String, List<PortDescription>> map = clientPortDescs.stream().collect(
                    Collectors.groupingBy(p -> p.annotations().value(NETWORK_PORT)));

            List<String> keys = map.keySet().stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());
            int portIndex = Integer.parseInt(disaggregatedDevId.toString().split(TRANSPONDER_ID + "_")[1]);
            String netPortName = keys.get(portIndex);

            List<PortDescription> clientPortDescsResult = new ArrayList<>(map.get(netPortName));
            rewritePortNumber(clientPortDescsResult, 1);

            //OCH port NETWORK PORT
            Optional<PortDescription> netPort = networkPortDescs.stream().filter(p -> netPortName.equals(
                    p.annotations().value(PORT_NAME))).findAny();

            if (netPort.isPresent()) {
                clientPortDescsResult.add(DefaultPortDescription.builder(netPort.get())
                        .withPortNumer(PortNumber.portNumber(clientPortDescsResult.size() + 1)).build());
            } else {
                LOG.warn("Network port with name {} does not exist", netPortName);
            }
            return clientPortDescsResult;
        }
        return Collections.EMPTY_LIST;
    }

    private void rewritePortNumber(List<PortDescription> linePortDescs, int indexOffSet) {
        for (int i = 0; i < linePortDescs.size(); i++) {
            linePortDescs.set(i, DefaultPortDescription.builder(linePortDescs.get(i))
                    .withPortNumer(PortNumber.portNumber(i + indexOffSet)).build());
        }
    }

    @Override
    public Set<LinkDescription> getDisaggregatedLinks(DeviceId disaggregatedDevId,
                                                      Set<LinkDescription> originalLinkDescs) {

        //TODO: we can annotate some information of the original LinkDescription to prune the already discovered links
        //T <--> TOP reordered ports and create the link
        //TOP <--> Bottom reordered and create the link
        //Bottom <--> Bottom (line ports) originalLinkDescription

        Device dev = deviceService.getDevice(disaggregatedDevId);
        DisaggregatedType disaggregatedType = getDisaggregatedType(dev);
        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        List<Port> disaggregatedPorts = deviceService.getPorts(disaggregatedDevId);

        if (disaggregatedType == BOTTOM_ROADM) {
            disaggregatedLinkDescs.addAll(bottomRoadmLinks(dev,
                    disaggregatedPorts,
                    originalLinkDescs));
        }

        if (disaggregatedType == TOP_ROADM) {
            disaggregatedLinkDescs.addAll(topRoadmLinks(dev));
        }

        //one line port (OMS) for each Transponder (network port) + line ports (OMS) that are given
        if(disaggregatedType == WSS_ROADM) {
            disaggregatedLinkDescs.addAll(wssRoadmLinks(dev,
                    disaggregatedPorts,
                    originalLinkDescs));
        }

        if (disaggregatedType == TRANSPONDER) {
            int index = 2;
            if (dev.annotations().value(ARCH_ANNOTATION).equals(CDC.toString())) {
                index = 1;
            }

            disaggregatedLinkDescs.addAll(transponderLinks(dev, index));
        }


        return disaggregatedLinkDescs;
    }

    private Collection<LinkDescription> wssRoadmLinks(Device dev,
                                                      List<Port> disaggregatedPorts,
                                                      Set<LinkDescription> originalLinkDescs) {

        Collection<LinkDescription> disaggregatedLinkDescs = linkWssTxp(dev);

        //BOTTOM BOTTOM
        List<Device> devs = Lists.newArrayList(deviceService.getDevices(ROADM));
        //FIXME: must be exposed by the driver. This is also harcoded
        DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
        annotations.set("LengthInKm", "1.0");
        annotations.set("PropagationSpeed", "200000");
        annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));

        disaggregatedLinkDescs.addAll(getwssWsslinks(dev, disaggregatedPorts, originalLinkDescs, devs));

        return disaggregatedLinkDescs;

    }


    private Set<LinkDescription> bottomRoadmLinks(Device dev,
                                                  List<Port> disaggregatedPorts,
                                                  Set<LinkDescription> originalLinkDescs) {

        List<Device> devs = Lists.newArrayList(deviceService.getDevices(ROADM));
        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        //FIXME: must be exposed by the driver. This is also harcoded
        DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
        annotations.set("LengthInKm", "1.0");
        annotations.set("PropagationSpeed", "200000");
        annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));

        //Link between TOP and BOTTOM
        Optional<Device> dstBotDevice = devs.stream().filter(d -> d.annotations()
                .value(DISAGGREGATED_TYPE_ANNOTATION).equals(TOP_ROADM.toString()) &&
                isSameOriginalNode(dev, d)).findAny();
        if (dstBotDevice.isPresent()) {
            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), PortNumber.portNumber(1)),
                    new ConnectPoint(dstBotDevice.get().id(), PortNumber.portNumber(1)),
                    OPTICAL, annotations.build()));

        } else {
            LOG.warn("Missing TOP ROAM in the disaggregated topology");
        }
        disaggregatedLinkDescs.addAll(getBottomBottomlinks(dev, disaggregatedPorts, originalLinkDescs, devs));


        return disaggregatedLinkDescs;
    }

    private Set<LinkDescription> getBottomBottomlinks(Device dev, List<Port> disaggregatedPorts, Set<LinkDescription> originalLinkDescs, List<Device> devs) {
        //Links between BOTTOM and BOTTOM

        //Filter the links of the disaggregated ROADM
        //FIXME: equals ignore case not the best
        List<LinkDescription> bottomLinks = originalLinkDescs.stream().filter(l -> l.src().deviceId().toString()
                .equalsIgnoreCase(dev.annotations().value(ORIGINAL_NODE_ANNOTATION))).collect(Collectors.toList());
        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        for (LinkDescription link : bottomLinks) {

            Optional<Port> srcPort = disaggregatedPorts.stream().filter(p ->
                    link.annotations().value(SRC_PORT_NAME_ANNOTATION).equals(p.annotations().value(PORT_NAME)))
                    .findAny();
            if (!srcPort.isPresent()) {
                LOG.warn("Impossible to find the original source port of the link {}", link);
                break;
            }

            //FIXME: equals ignore case not the best
            Optional<Device> dstDevice = devs.stream().filter(d -> link.dst().deviceId().toString().equalsIgnoreCase(
                    d.annotations().value(ORIGINAL_NODE_ANNOTATION)) && getDisaggregatedType(d) == BOTTOM_ROADM)
                    .findAny();
            if (!dstDevice.isPresent()) {
                LOG.warn("Impossible to find the destination device of the link {}", link);
                break;
            }
            Optional<Port> dstPort = deviceService.getPorts(dstDevice.get().id()).stream().filter(p ->
                    link.annotations().value(DST_PORT_NAME_ANNOTATION).equals(p.annotations().value(PORT_NAME)))
                    .findAny();

            if (!dstPort.isPresent()) {
                LOG.warn("Impossible to find the original destination port of the link {}", link);
                break;
            }

            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), srcPort.get().number()),
                    new ConnectPoint(dstDevice.get().id(), dstPort.get().number()),
                    link.type(), link.annotations()));
        }

        return disaggregatedLinkDescs;
    }

    //FIXME: dupilicate methods (different only roadm type...)
    private Set<LinkDescription> getwssWsslinks(Device dev, List<Port> disaggregatedPorts, Set<LinkDescription> originalLinkDescs, List<Device> devs) {
        //Links between BOTTOM and BOTTOM

        //Filter the links of the disaggregated ROADM
        //FIXME: equals ignore case not the best
        List<LinkDescription> bottomLinks = originalLinkDescs.stream().filter(l -> l.src().deviceId().toString()
                .equalsIgnoreCase(dev.annotations().value(ORIGINAL_NODE_ANNOTATION))).collect(Collectors.toList());
        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        for (LinkDescription link : bottomLinks) {

            Optional<Port> srcPort = disaggregatedPorts.stream().filter(p ->
                    link.annotations().value(SRC_PORT_NAME_ANNOTATION).equals(p.annotations().value(PORT_NAME)))
                    .findAny();
            if (!srcPort.isPresent()) {
                LOG.warn("Impossible to find the original source port of the link {}", link);
                break;
            }

            //FIXME: equals ignore case not the best
            Optional<Device> dstDevice = devs.stream().filter(d -> link.dst().deviceId().toString().equalsIgnoreCase(
                    d.annotations().value(ORIGINAL_NODE_ANNOTATION)) && getDisaggregatedType(d) == WSS_ROADM)
                    .findAny();
            if (!dstDevice.isPresent()) {
                LOG.warn("Impossible to find the destination device of the link {}", link);
                break;
            }
            Optional<Port> dstPort = deviceService.getPorts(dstDevice.get().id()).stream().filter(p ->
                    link.annotations().value(DST_PORT_NAME_ANNOTATION).equals(p.annotations().value(PORT_NAME)))
                    .findAny();

            if (!dstPort.isPresent()) {
                LOG.warn("Impossible to find the original destination port of the link {}", link);
                break;
            }

            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), srcPort.get().number()),
                    new ConnectPoint(dstDevice.get().id(), dstPort.get().number()),
                    link.type(), link.annotations()));
        }

        return disaggregatedLinkDescs;
    }


    private Set<LinkDescription> topRoadmLinks(Device dev) {

        List<Device> roadms = Lists.newArrayList(deviceService.getDevices(ROADM));
        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        //Link between TOP and BOTTOM
        Optional<Device> dstBottomDevice = roadms.stream().filter(d -> d.annotations()
                .value(DISAGGREGATED_TYPE_ANNOTATION).equals(BOTTOM_ROADM.toString()) &&
                isSameOriginalNode(dev, d)).findAny();
        if (dstBottomDevice.isPresent()) {
            DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
            annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));
            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), PortNumber.portNumber(1)),
                    new ConnectPoint(dstBottomDevice.get().id(), PortNumber.portNumber(1)),
                    OPTICAL, annotations.build()));
        } else {
            LOG.warn("Missing TOP ROAM in the disaggregated topology");
        }
        disaggregatedLinkDescs.addAll(linkTopTxp(dev));


        return disaggregatedLinkDescs;
    }

    private Set<LinkDescription> linkTopTxp(Device dev) {

        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        //Link between ROADM (could be TOP or WSS) and Transponders
        List<Device> transponders = Lists.newArrayList(deviceService.getDevices(OTN)).stream()
                .filter(d -> d.annotations().value(DISAGGREGATED_TYPE_ANNOTATION).equals(TRANSPONDER.toString()) &&
                isSameOriginalNode(dev, d)).collect(Collectors.toList());

        transponders = transponders.stream().sorted(Comparator.comparing(d -> d.id().toString()))
                .collect(Collectors.toList());

        //FIXME port index: this correspond to the port to ROADM of the TXP
        int portIndex = 2;
        for (Device transponder : transponders) {
            Optional<Port> dstPort = deviceService.getPorts(transponder.id()).stream()
                    .filter(p -> p.type().equals(OCH)).findAny();
            DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
            annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));
            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), PortNumber.portNumber(portIndex)),
                    new ConnectPoint(transponder.id(), dstPort.get().number()),
                    OPTICAL, annotations.build()));
            portIndex++;

        }
        return disaggregatedLinkDescs;
    }

    private Set<LinkDescription> linkWssTxp(Device dev) {

        Set<LinkDescription> disaggregatedLinkDescs = new HashSet<>();
        //Link between ROADM (could be TOP or WSS) and Transponders
        List<Device> transponders = Lists.newArrayList(deviceService.getDevices(OTN)).stream()
                .filter(d -> d.annotations().value(DISAGGREGATED_TYPE_ANNOTATION).equals(TRANSPONDER.toString()) &&
                        isSameOriginalNode(dev, d)).collect(Collectors.toList());

        transponders = transponders.stream().sorted(Comparator.comparing(d -> d.id().toString()))
                .collect(Collectors.toList());

        //FIXME port index: this correspond to the port to ROADM of the TXP
        int portIndex = 1;
        for (Device transponder : transponders) {
            Optional<Port> dstPort = deviceService.getPorts(transponder.id()).stream()
                    .filter(p -> p.type().equals(OCH)).findAny();
            DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
            annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));
            disaggregatedLinkDescs.add(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), PortNumber.portNumber(portIndex)),
                    new ConnectPoint(transponder.id(), dstPort.get().number()),
                    OPTICAL, annotations.build()));
            portIndex++;

        }
        return disaggregatedLinkDescs;
    }

    private Set<LinkDescription> transponderLinks(Device dev, int index) {
        //index = 1 for wss, index = 2 for TOP
        List<Device> roadms = Lists.newArrayList(deviceService.getDevices(ROADM));
        //Link between Transponder and ROADM (could be TOP or WSS)
        Optional<Device> dstTopDevice = roadms.stream().filter(d -> isTxpRoadm(d.annotations()) &&
                isSameOriginalNode(dev, d)).findAny();

        if (dstTopDevice.isPresent()) {

            //Reorder the transponders to respect the same port number of the TOP
            List<DeviceId> transponderIds = Lists.newArrayList(deviceService.getDevices(OTN)).stream()
                    .filter(d -> d.annotations().value(DISAGGREGATED_TYPE_ANNOTATION).equals(TRANSPONDER.toString()) &&
                            isSameOriginalNode(dev, d))
                    .map(Device::id)
                    .sorted(Comparator.comparing(DeviceId::toString))
                    .collect(Collectors.toList());
            //TODO: offset...
            int transponderIndex = transponderIds.indexOf(dev.id()) + index;

            Optional<Port> srcPort = deviceService.getPorts(dev.id()).stream()
                    .filter(p -> p.type().equals(OCH)).findAny();
            if (!srcPort.isPresent()) {
                LOG.warn("Impossible to find the original destination port of the internal link between " +
                        "TOP ROADM and Transponder");
                return Collections.EMPTY_SET;
            }

            DefaultAnnotations.Builder annotations = DefaultAnnotations.builder();
            annotations.set(ORIGINAL_NODE_ANNOTATION, dev.annotations().value(ORIGINAL_NODE_ANNOTATION));
            return Collections.singleton(new DefaultLinkDescription(
                    new ConnectPoint(dev.id(), srcPort.get().number()),
                    new ConnectPoint(dstTopDevice.get().id(), PortNumber.portNumber(transponderIndex)),
                    OPTICAL, annotations.build()));

        } else {
            LOG.warn("Missing TOP or WSS ROAM in the disaggregated topology");
        }
        return Collections.EMPTY_SET;
    }

    private boolean isTxpRoadm(Annotations a) {
        if(a.value(DISAGGREGATED_TYPE_ANNOTATION).equals(TOP_ROADM.toString())
                        || a.value(DISAGGREGATED_TYPE_ANNOTATION).equals(WSS_ROADM.toString())) {
            return true;
        }
        return false;
    }

    private boolean isSameOriginalNode(Device dev, Device x) {
        return x.annotations().value(ORIGINAL_NODE_ANNOTATION)
                .equals(dev.annotations().value(ORIGINAL_NODE_ANNOTATION));
    }

    private DisaggregatedType getDisaggregatedType(Device dev) {
        return DisaggregatedType.valueOf(
                dev.annotations().value(DISAGGREGATED_TYPE_ANNOTATION));
    }

}



