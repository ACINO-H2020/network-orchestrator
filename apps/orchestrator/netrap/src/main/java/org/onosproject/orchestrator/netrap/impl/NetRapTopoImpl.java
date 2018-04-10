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

package org.onosproject.orchestrator.netrap.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.wpl.xrapc.Constants;
import com.wpl.xrapc.XrapErrorReply;
import com.wpl.xrapc.XrapGetReply;
import com.wpl.xrapc.XrapGetRequest;
import com.wpl.xrapc.XrapPostRequest;
import com.wpl.xrapc.XrapReply;
import com.wpl.xrapc.XrapResource;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.codec.CodecContext;
import org.onosproject.codec.CodecService;
import org.onosproject.codec.JsonCodec;
import org.onosproject.net.Annotations;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.optical.OchPort;
import org.onosproject.net.optical.device.OchPortHelper;
import org.onosproject.net.topology.Topology;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;
import org.onosproject.orchestrator.netrap.api.NetRapRegistryService;
import org.onosproject.orchestrator.netrap.api.NetRapService;
import org.onosproject.orchestrator.netrap.api.NetRapTopoService;
import org.onosproject.orchestrator.netrap.model.NetRapLink;
import org.onosproject.orchestrator.netrap.model.NetRapNode;
import org.onosproject.orchestrator.netrap.model.NetRapTopology;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;


@Component(immediate = true)
@Service
public class NetRapTopoImpl extends XrapResource implements NetRapTopoService {

    private static final Integer OPTO_LAYER = 0;
    private static final Integer IP_LAYER = 1;
    private final Logger log = getLogger(NetRapTopoImpl.class);
    /* Needed by topologyResource */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CodecService codecService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private static final String NETRAP_LATITUDE = "latitude";

    private static final String NETRAP_LONGITUDE = "longitude";
    // Creates a dependecy loop if resolved using @Reference
    protected NetRapService NetRapService = null;
    protected NetRapRegistryService netRapRegistryService = null;

    @Activate
    protected void activate() {
        log.info("Starting NetRapTopoService ..!");
        setRoute("/topology/");
        log.info("NetRapTopoService started!");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopping NetRapTopoService...");
    }

    // Uses ONOS device and topology model
    private byte[] buildJsonTopologyOld() throws RuntimeException {
        String errormsg;
        buildTopo:
        {
            log.debug("GET: TopologyService handle: " + topologyService);
            Topology top = topologyService.currentTopology();
            if (top == null) {
                errormsg = "unable to find topology";
                break buildTopo;
            }
            log.debug("Got topology: " + top.toString());
            CodecContext codecContext = new TestContext(codecService);

            // device list
            ObjectNode root = new ObjectMapper().createObjectNode();

            Iterable<Link> linkList = linkService.getLinks();
            JsonCodec<Link> linkJsonCodec = codecService.getCodec(Link.class);
            if (linkJsonCodec == null || linkList == null) {
                log.error("LinkList or linkcodec is null!");
                errormsg = "No links found";
                break buildTopo;
            }
            ArrayNode linkArray = linkJsonCodec.encode(linkList, codecContext);
            root.set("links", linkArray);

            Iterable<Device> deviceList = deviceService.getAvailableDevices();

            JsonCodec<Device> deviceJsonCodec = codecService.getCodec(Device.class);
            if (deviceJsonCodec == null || deviceList == null) {
                log.error("DeviceList or deviceCodec is null!");
                errormsg = "No devices found";
                break buildTopo;
            }

            ArrayNode data = deviceJsonCodec.encode(deviceList, codecContext);

            ArrayNode devices = new ObjectMapper().createArrayNode();
            JsonCodec<Port> portJsonCodec = codecService.getCodec(Port.class);
            for (Device dev : deviceList) {
                ObjectNode devData = deviceJsonCodec.encode(dev, codecContext);
                List<Port> portList = deviceService.getPorts(dev.id());
                ArrayNode result = portJsonCodec.encode(portList, codecContext);
                devData.set("ports", result);
                devices.add(devData);
            }
            root.set("nodes", devices);
            return root.toString().getBytes();
        }
        throw new RuntimeException(errormsg);
    }

    private boolean isBottomRoadm(Device dev) {
        // ROADM devices with only OMS port neighbours
        // Should be a node per port
        Set<Link> nodeLinks = linkService.getDeviceLinks(dev.id());
        for (Link l : nodeLinks) {
            DeviceId dstDevId = l.dst().deviceId();
            PortNumber dstPortNumber = l.dst().port();
            if (dstDevId.equals(dev.id())) {
                dstDevId = l.src().deviceId();
                dstPortNumber = l.src().port();
            }
            Port oppositePort = deviceService.getPort(dstDevId, dstPortNumber);
            if (oppositePort.type() != Port.Type.OMS) {
                return false;
            }
        }
        return true;
    }


    private Pair<List<NetRapNode>, List<NetRapLink>> createBottomRoadms(Device dev) {
        List<Port> portList = deviceService.getPorts(dev.id());
        log.debug("Creating bottom ROADM nodes");
        long topPort = -1;
        List<NetRapNode> newnodes = new ArrayList<>();
        List<NetRapLink> newlinks = new ArrayList<>();
        // Figure out which port is the one connect to the topRoadm
        Set<Link> links = linkService.getDeviceLinks(dev.id());
        // figure out which port is connected to something not a bottom roadm
        for (Link link : links) {
            if (link.src().deviceId().equals(dev.id())) {
                if (!isBottomRoadm(deviceService.getDevice(link.dst().deviceId()))) {
                    topPort = link.src().port().toLong();
                    break;
                }
            }
        }

        if (topPort == -1) {
            log.error("Cannot create bottom Roadm!");
            log.error("Couldn't figure out how to connect to client ports! ");
            return null;
        }

        Double anginc = 2 * Math.PI / (portList.size() + 1);
        Double angle = 0.0;
        for (Port p : portList) {
            NetRapNode roadm = new NetRapNode();
            roadm.setName(dev.id() + "/" + p.number().toLong());
            roadm.setLatitude(0.0);
            roadm.setLongitude(0.0);
            roadm.setMTTR(1.0);
            roadm.setMTBF(1000.0);
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("IPNode", "false");
            attributes.put("type", "BottomRoadm");
            Annotations annotations = dev.annotations();
            double x = 0.0, y = 0.0;
            for (String key : annotations.keys()) {
                if (key.equals(NETRAP_LATITUDE)) {
                    x = Double.valueOf(annotations.value(key));
                }
                //roadm.setLatitude(Double.valueOf(annotations.value(key)));
                else if (key.equals(NETRAP_LONGITUDE)) {
                    y = Double.valueOf(annotations.value(key));
                }
                //roadm.setLongitude(Double.valueOf(annotations.value(key)));
                else {
                    attributes.put(key, annotations.value(key));
                }
            }

            double xprim = x + 1.0 * Math.cos(angle);
            double yprim = y + 1.0 * Math.sin(angle);
            roadm.setLatitude(xprim);
            roadm.setLongitude(yprim);
            angle -= anginc;
            roadm.setAttributes(attributes);
            newnodes.add(roadm);

            if (p.number().toLong() != topPort) {

                newlinks.add(createOptoLink(null, dev.id() + "/" + p.number().toLong(), dev.id() + "/" + topPort, 2 * 80.0, p.number().toLong(), topPort));
                newlinks.add(createOptoLink(null, dev.id() + "/" + topPort, dev.id() + "/" + p.number().toLong(), 2 * 80.0, topPort, p.number().toLong()));

                /*
                newlinks.add(createOptoLink(null, dev.id() + "/" + p.number().toLong(), dev.id() + "/" + topPort, 2*80.0, topPort, p.number().toLong() ));
                newlinks.add(createOptoLink(null, dev.id() + "/" + topPort, dev.id() + "/" + p.number().toLong(), 2*80.0, p.number().toLong(),topPort));
                */
            }
        }

        Pair<List<NetRapNode>, List<NetRapLink>> nodeslinks = new ImmutablePair<List<NetRapNode>, List<NetRapLink>>(newnodes, newlinks);

        return nodeslinks;

    }

    NetRapNode createTopRoadm(Device dev) {
        NetRapNode roadm = new NetRapNode();
        roadm.setName(dev.id().toString());
        roadm.setLatitude(0.0);
        roadm.setLongitude(0.0);
        roadm.setMTTR(1.0);
        roadm.setMTBF(1000.0);
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("IPNode", "false");
        attributes.put("type", "TopRoadm");
        Annotations annotations = dev.annotations();
        for (String key : annotations.keys()) {
            if (key.equals(NETRAP_LATITUDE)) {
                roadm.setLatitude(Double.valueOf(annotations.value(key)));
            } else if (key.equals(NETRAP_LONGITUDE)) {
                roadm.setLongitude(Double.valueOf(annotations.value(key)));
            } else {
                attributes.put(key, annotations.value(key));
            }
        }
        roadm.setAttributes(attributes);
        return roadm;
    }

    NetRapNode createOtn(Device dev) {
        NetRapNode roadm = new NetRapNode();
        roadm.setName(dev.id().toString());
        roadm.setLatitude(0.0);
        roadm.setLongitude(0.0);
        roadm.setMTTR(1.0);
        roadm.setMTBF(1000.0);
        HashMap<String, String> attributes = new HashMap<>();

        //OpticalDevice
        // TODO: figure out how to get the OtuClt port
        List<Port> ports = deviceService.getPorts(dev.id());

        for (Port port : ports) {
            if (port.type() == Port.Type.OCH) {
                Optional<OchPort> ochPort = OchPortHelper.asOchPort(port);
                attributes.put("color", Integer.toString(ochPort.get().lambda().spacingMultiplier()));
            }
        }

        attributes.put("IPNode", "IPtransponder");
        attributes.put("type", "Transponder");
        Annotations annotations = dev.annotations();
        for (String key : annotations.keys()) {
            if (key.equals(NETRAP_LATITUDE)) {
                roadm.setLatitude(Double.valueOf(annotations.value(key)));
            } else if (key.equals(NETRAP_LONGITUDE)) {
                roadm.setLongitude(Double.valueOf(annotations.value(key)));
            } else {
                attributes.put(key, annotations.value(key));
            }
        }
        roadm.setAttributes(attributes);
        return roadm;
    }

    NetRapNode createSwitch(Device dev) {
        NetRapNode roadm = new NetRapNode();
        roadm.setName(dev.id().toString());
        roadm.setLatitude(0.0);
        roadm.setLongitude(0.0);
        roadm.setMTTR(1.0);
        roadm.setMTBF(1000.0);
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put("IPNode", "true");
        attributes.put("do not attach IP links to me", "true");
        attributes.put("type", "Router");
        Annotations annotations = dev.annotations();
        for (String key : annotations.keys()) {
            if (key.equals(NETRAP_LATITUDE)) {
                roadm.setLatitude(Double.valueOf(annotations.value(key)));
            } else if (key.equals(NETRAP_LONGITUDE)) {
                roadm.setLongitude(Double.valueOf(annotations.value(key)));
            } else {
                attributes.put(key, annotations.value(key));
            }
        }
        roadm.setAttributes(attributes);
        return roadm;
    }

    private NetRapLink createIPLink(Link link, String source, String destination, Double linkSpeed, long srcnum, long dstnum) {
        NetRapLink newlink = new NetRapLink();
        HashMap<String, String> attributes = new HashMap<>();

        newlink.setSrc(source);
        newlink.setDst(destination);
        newlink.setLayer(IP_LAYER);
        newlink.setCapacity(linkSpeed);
        // Put some default values, these can be overwritten by annotations
        // 24 hours repair time
        newlink.setMTTR(24.0);
        // two years between failures
        newlink.setMTBF(24.0 * 365.0 * 2.0);
        // 200 000 km/s
        newlink.setPropagationSpeed(200000);
        // 100 kilometer links
        newlink.setLengthInKm(1.0);


        Annotations annotations = link.annotations();
        for (String key : annotations.keys()) {
            if (key.equals("MTTR")) {
                newlink.setMTTR(Double.valueOf(annotations.value(key)));
            } else if (key.equals("MTBF")) {
                newlink.setMTBF(Double.valueOf(annotations.value(key)));
            } else if (key.equals("LengthInKm")) {
                newlink.setLengthInKm(Double.valueOf(annotations.value(key)));
            } else if (key.equals("PropagationSpeed")) {
                newlink.setPropagationSpeed(Integer.valueOf(annotations.value(key)));
            } else {
                attributes.put(key, annotations.value(key));
            }
        }
        attributes.put("srcPort", Long.toString(srcnum));
        attributes.put("dstPort", Long.toString(dstnum));
        attributes.put("do not delete me", "true");
        newlink.setAttributes(attributes);

        return newlink;
    }


    private NetRapLink createOptoLink(Link link, String source, String destination, Double linkSpeed, long srcnum, long dstnum) {
        NetRapLink newlink = new NetRapLink();
        HashMap<String, String> attributes = new HashMap<>();
        newlink.setSrc(source);
        newlink.setDst(destination);
        newlink.setLayer(OPTO_LAYER);
        newlink.setCapacity(linkSpeed);

        // set default values
        newlink.setMTTR(24.0);
        // two years between failures
        newlink.setMTBF(24.0 * 365.0 * 2.0);
        // 200 000 km/s
        newlink.setPropagationSpeed(200000);
        // 100 kilometer links
        newlink.setLengthInKm(1.0);

        if (link != null) {
            Annotations annotations = link.annotations();
            for (String key : annotations.keys()) {
                if (key.equals("MTTR")) {
                    newlink.setMTTR(Double.valueOf(annotations.value(key)));
                } else if (key.equals("MTBF")) {
                    newlink.setMTBF(Double.valueOf(annotations.value(key)));
                } else if (key.equals("LengthInKm")) {
                    newlink.setLengthInKm(Double.valueOf(annotations.value(key)));
                } else if (key.equals("PropagationSpeed")) {
                    newlink.setPropagationSpeed(Integer.valueOf(annotations.value(key)));
                } else {
                    attributes.put(key, annotations.value(key));
                }
            }
        } else {
            // these are internal links, should be pretty short
            newlink.setLengthInKm(1.0);
        }
        attributes.put("srcPort", Long.toString(srcnum));
        attributes.put("dstPort", Long.toString(dstnum));
        newlink.setAttributes(attributes);

        return newlink;
    }

    private NetRapLink createLink(Link link) {
        DeviceId dstDevId = link.dst().deviceId();
        Device dstDev = deviceService.getDevice(dstDevId);
        Port dstPort = deviceService.getPort(dstDevId, link.dst().port());

        DeviceId srcDevId = link.src().deviceId();
        Device srcDev = deviceService.getDevice(srcDevId);
        Port srcPort = deviceService.getPort(srcDevId, link.src().port());

        long srcPNum = srcPort.number().toLong();
        long dstPNum = dstPort.number().toLong();

        // 1) Switch-switch, router-router, switch-router, switch-otn, router-otn
        if (link.type() == Link.Type.DIRECT) {
            if (srcPort.type() == Port.Type.COPPER && dstPort.type() == Port.Type.COPPER) {
                // TODO: should be min rather than max, however, some ports have speed 0..
                double linkSpeed = Math.max(srcPort.portSpeed(), dstPort.portSpeed()) / 1000.0;
                return createIPLink(link, srcDev.id().toString(), dstDev.id().toString(), linkSpeed, dstPNum, srcPNum);
            } else if (srcPort.type() == Port.Type.COPPER && dstPort.type() == Port.Type.ODUCLT) {
                double linkSpeed = dstPort.portSpeed() / 1000.0;
                NetRapLink newlink = createIPLink(link, srcDev.id().toString(), dstDev.id().toString(), linkSpeed, dstPNum, srcPNum);
                newlink.getAttributes().put("do not delete me", "true");
                return newlink;
            } else if (srcPort.type() == Port.Type.ODUCLT && dstPort.type() == Port.Type.COPPER) {
                double linkSpeed = srcPort.portSpeed() / 1000.0;
                NetRapLink newlink = createIPLink(link, srcDev.id().toString(), dstDev.id().toString(), linkSpeed, dstPNum, srcPNum);
                newlink.getAttributes().put("do not delete me", "true");
                return newlink;
            } else {
                log.error("Direct link that isn't between copper/copper or copper/oduclt!");
                return null;
            }
        } // 2) otn-roadm, roadm-roadm

        // TODO: Capacity is hardcoded here, extract from OMS frequencies and such..
        else if (link.type() == Link.Type.OPTICAL) {
            if (srcPort.type() == Port.Type.OCH && dstPort.type() == Port.Type.OMS) {
                String destname = null;

                if (isBottomRoadm(dstDev)) {
                    destname = dstDev.id().toString() + "/" + dstPort.number().toLong();
                } else {
                    destname = dstDev.id().toString();
                }

                return createOptoLink(link, srcDev.id().toString(), destname, 80.0, dstPNum, srcPNum);


            } else if (srcPort.type() == Port.Type.OMS && dstPort.type() == Port.Type.OCH) {
                String srcname = null;

                if (isBottomRoadm(srcDev)) {
                    srcname = srcDev.id().toString() + "/" + srcPort.number().toLong();
                } else {
                    srcname = srcDev.id().toString();
                }
                // needs to be 2x80 for some reason
                return createOptoLink(link, srcname, dstDev.id().toString(), 80.0, dstPNum, srcPNum);
            } else if (srcPort.type() == Port.Type.OMS && dstPort.type() == Port.Type.OMS) {
                String destname = null, srcname = null;

                if (isBottomRoadm(dstDev)) {
                    destname = dstDev.id().toString() + "/" + dstPort.number().toLong();
                } else {
                    destname = dstDev.id().toString();
                }

                if (isBottomRoadm(srcDev)) {
                    srcname = srcDev.id().toString() + "/" + srcPort.number().toLong();
                } else {
                    srcname = srcDev.id().toString();
                }


                return createOptoLink(link, srcname, destname, 80.0, dstPNum, srcPNum);
            } else {
                log.error("Optical link that isn't between och/oms or oms/oms!");
                return null;
            }
        } else {
            return null;
        }
    }

    private byte[] buildJsonTopology() throws RuntimeException {
        Gson gson = new Gson();
        NetRapTopology n2ptopo = buildNetRapTopology();
        return gson.toJson(n2ptopo).getBytes();
    }

    private NetRapTopology buildNetRapTopology() throws RuntimeException {
        String errormsg = "";
        NetRapTopology n2ptopo = new NetRapTopology();

        buildTopo:
        {
            Topology top = topologyService.currentTopology();
            if (top == null) {
                errormsg = "unable to find topology";
                break buildTopo;
            }

            Iterable<Device> deviceList = deviceService.getAvailableDevices();

            if (deviceList == null) {
                log.error("DeviceList or deviceCodec is null!");
                errormsg = "No devices found";
                break buildTopo;
            }


            for (Device dev : deviceList) {
                switch (dev.type()) {
                    case ROADM:
                        if (isBottomRoadm(dev)) {
                            // Create a node per port
                            // IPNode = false
                            // name = null:0000000000a/1, etc

                            // A bit weird since bottom roadms are split into multiple links and nodes ..
                            Pair<List<NetRapNode>, List<NetRapLink>> nodeslinks = createBottomRoadms(dev);
                            if (nodeslinks != null) {
                                nodeslinks.getLeft().forEach(n2ptopo::addNodesItem);
                                nodeslinks.getRight().forEach(n2ptopo::addLinksItem);
                            }
                        } else {
                            //log.info("Creating top ROADM node");
                            // Create a single node
                            // IPNode = False
                            // name = null:0000000000a
                            NetRapNode newnode = createTopRoadm(dev);
                            //  log.info("Created node " + newnode);
                            n2ptopo.addNodesItem(newnode);
                        }
                        break;
                    case OTN:
                        //log.info("Found OTN device");
                        // create a single node
                        // IPNode = True
                        NetRapNode newnode = createOtn(dev);
                        n2ptopo.addNodesItem(newnode);
                        break;
                    case SWITCH:
                    case ROUTER:
                        //log.info("Found SWITCH/ROUTER device");
                        newnode = createSwitch(dev);
                        n2ptopo.addNodesItem(newnode);
                        break;
                    default:
                        log.warn("Found unknown device");
                        break;
                }
            }


            Iterable<Link> linkList = linkService.getLinks();
            if (linkList == null) {
                log.error("LinkList is null!");
                errormsg = "No links found";
                break buildTopo;
            }
            for (Link link : linkList) {
                // ignore links tagged with netRap
                Set<String> keys = link.annotations().keys();
                if (keys.contains("netRap") == false) {
                    NetRapLink netRapLink = createLink(link);

                    if (netRapLink == null) {
                        continue;
                    }
                    if (link.state() == Link.State.ACTIVE) {
                        netRapLink.setActive(true);
                    } else {
                        netRapLink.setActive(false);
                    }
                    n2ptopo.addLinksItem(netRapLink);

                } else {
                    log.error("################################");
                    log.error("FOUND LINK TAGGED WITH NETRAP!!");
                    log.error("################################");
                }
            }
            return n2ptopo;
        }
        throw new RuntimeException(errormsg);
    }

    public XrapReply GET(XrapGetRequest request) {
        try {
            byte[] body = buildJsonTopology();
            XrapGetReply rep = new XrapGetReply();
            rep.setContentType("application/json");
            rep.setEtag("*");
            rep.setStatusCode(Constants.OK_200);
            rep.setRequestId(request.getRequestId());
            rep.setDateModified(new Date().getTime());
            rep.setRouteid(request.getRouteid());
            rep.setBody(body);
            return rep;
        } catch (RuntimeException e) {
            XrapErrorReply rep = new XrapErrorReply();
            rep.setErrorText(e.getMessage());
            rep.setStatusCode(Constants.InternalError_500);
            rep.setRequestId(request.getRequestId());
            rep.setRouteid(request.getRouteid());
            return rep;
        }
    }

    @Override
    public void updateTopology() {
        sendTopology();
    }

    @Override
    public NetRapTopology getTopology() {
        try {
            NetRapTopology top = buildNetRapTopology();
            return top;
        } catch (RuntimeException e) {
            log.error("Could not build topology: " + e);
            return null;
        }
    }

    @Override
    public boolean sendTopology(ByteBuffer address) {
        if (NetRapService == null) {
            NetRapService = DefaultServiceDirectory.getService(NetRapService.class);
            log.info("Found netrap service:" + NetRapService);
        }

        if (NetRapService == null) {
            log.error("No NetRapService found!");
            return false;
        }

        try {
            byte[] body = buildJsonTopology();
            XrapPostRequest req = new XrapPostRequest("/topology", new String(body));
            req.setRouteid(address);
            log.trace("Posting topology " + req + " to " + Arrays.toString(Hex.encodeHex(address.array())));
            XrapReply rep = NetRapService.sendOne(address, req);
            //log.info("Got reply : " + rep);
        } catch (RuntimeException e) {
            log.info("Error building topology: " + e.getMessage());
            return false;
        }

        // TODO: need to think about how to handle this properly
        // PONTUS: October 24, trying to fix duplicate demands in Net2Plan
        log.info("sending all intents!");
        NetRapIntentService netRapIntentService = DefaultServiceDirectory.getService(NetRapIntentService.class);
        netRapIntentService.sendIntents(address);
        return true;
    }

    public boolean sendTopology() {
        if (NetRapService == null) {
            NetRapService = DefaultServiceDirectory.getService(NetRapService.class);
            log.info("Found netrap service:" + NetRapService);
        }

        if (NetRapService == null) {
            log.error("No NetRapService found!");
            return false;
        }

        try {
            byte[] body = buildJsonTopology();
            XrapPostRequest req = new XrapPostRequest("/topology", new String(body));
            log.debug("Posting topology " + req + " to ALL");
            NetRapService.sendAny(req);
        } catch (RuntimeException e) {
            log.info("Error building topology: " + e.getMessage());
            return false;
        }

        // TODO: need to think about how to handle this properly
        // PONTUS: October 24, trying to fix duplicate demands in Net2Plan
        log.info("sending all intents!");
        NetRapIntentService netRapIntentService = DefaultServiceDirectory.getService(NetRapIntentService.class);
        netRapIntentService.sendIntents();
        return true;
    }

    // wtf?
    private class TestContext implements CodecContext {
        private ObjectMapper mapper = new ObjectMapper();
        private CodecService codecService;

        public TestContext(CodecService codecService) {
            this.codecService = codecService;

        }

        @Override
        public ObjectMapper mapper() {
            return mapper;
        }

        @Override
        public <T> JsonCodec<T> codec(Class<T> entityClass) {
            return codecService.getCodec(entityClass);
        }

        @Override
        public <T> T getService(Class<T> serviceClass) {
            return DefaultServiceDirectory.getService(serviceClass);
        }
    }

}


