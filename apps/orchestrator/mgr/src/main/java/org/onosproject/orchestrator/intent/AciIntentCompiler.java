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

package org.onosproject.orchestrator.intent;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.graph.ScalarWeight;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.util.Bandwidth;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.DisjointPath;
import org.onosproject.net.ElementId;
import org.onosproject.net.FilteredConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.AciPathIntent;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompilationException;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.IntentException;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentNegotiationException;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.LinkCollectionIntent;
import org.onosproject.net.intent.constraint.AsymmetricPathConstraint;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.EncryptionConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;
import org.onosproject.net.intent.constraint.LatencySensitive;
import org.onosproject.net.intent.constraint.NegotiableConstraint;
import org.onosproject.net.intent.constraint.ObstacleConstraint;
import org.onosproject.net.intent.constraint.RestorationConstraint;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.resource.ContinuousResource;
import org.onosproject.net.resource.DiscreteResource;
import org.onosproject.net.resource.Resource;
import org.onosproject.net.resource.ResourceAllocation;
import org.onosproject.net.resource.ResourceConsumer;
import org.onosproject.net.resource.ResourceId;
import org.onosproject.net.resource.ResourceService;
import org.onosproject.net.resource.Resources;
import org.onosproject.net.topology.PathService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.onosproject.net.AnnotationKeys.getAnnotatedValue;
import static org.onosproject.net.Link.Type.EDGE;

/**
 * TODO.
 */
@Component(immediate = true)
public class AciIntentCompiler implements IntentCompiler<AciIntent> {

    private static final Bandwidth DEFAULT_OPTICAL_BW = Bandwidth.gbps(1);
    private static final Bandwidth DEFAULT_MACSEC_BW = Bandwidth.mbps(10);
    // tag for nodes and ports that indicates encryption capabilities
    private static final String ENCRYPTION = "encryption";
    private static final String DEVICE_ID_NOT_FOUND = "Didn't find device id in the link";
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService configService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ResourceService resourceService;
    @Property(label = "Bandwidth threshold in Mbps to applied Optical encryption ",
            name = "opticalBw",
            longValue = 1_000)
    private Bandwidth opticalBw = DEFAULT_OPTICAL_BW;
    @Property(label = "Bandwidth threshold in Mbps to applied MacSec encryption ",
            name = "macSecBw",
            longValue = 10)
    private Bandwidth macSecBw = DEFAULT_MACSEC_BW;
    private Map<ConnectPoint, Set<ConnectPoint>> allowedPorts = new HashMap<>();
    private ConnectPoint j1p230;
    //private ConnectPoint sa2;

    @Activate
    public void activate() {
        intentManager.registerCompiler(AciIntent.class, this);

        configService.registerProperties(getClass());

        //BLUE
        // J1 <--> J3
        ConnectPoint j1p209 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.132:830"),
                PortNumber.portNumber(516));
        ConnectPoint j3p208 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.134:830"),
                PortNumber.portNumber(517));
        // J1 <--> J2
        ConnectPoint j2p208 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.133:830"),
                PortNumber.portNumber(518));
        Set<ConnectPoint> j1 = new HashSet<>();
        j1.add(j3p208);
        j1.add(j2p208);
        allowedPorts.put(j1p209, j1);
        allowedPorts.put(j3p208, Collections.singleton(j1p209));
        allowedPorts.put(j2p208, Collections.singleton(j1p209));
        //ORANGE
        //J1 <--> J3
        j1p230 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.132:830"),
                PortNumber.portNumber(549));
        ConnectPoint j3p220 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.134:830"),
                PortNumber.portNumber(547));
        allowedPorts.put(j1p230, Collections.singleton(j3p220));
        allowedPorts.put(j3p220, Collections.singleton(j1p230));

        // GREEN
        ConnectPoint j2p209 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.133:830"),
                PortNumber.portNumber(519));
        ConnectPoint j3p209 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.134:830"),
                PortNumber.portNumber(518));
        allowedPorts.put(j2p209, Collections.singleton(j3p209));
        allowedPorts.put(j3p209, Collections.singleton(j2p209));

        //ENCRYPTED PORTs
        ConnectPoint j1p207 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.132:830"),
                PortNumber.portNumber(514));
        ConnectPoint j3p207 = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.134:830"),
                PortNumber.portNumber(516));
        allowedPorts.put(j1p207, Collections.singleton(j3p207));
        allowedPorts.put(j3p207, Collections.singleton(j1p207));

        //ADVA Testbed


        //TODO: to be removed! only for testing in Mininet
        //ORANGE
        ConnectPoint sa2 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000a"),
                PortNumber.portNumber(2));
        ConnectPoint sb2 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000b"),
                PortNumber.portNumber(2));
        allowedPorts.put(sa2, Collections.singleton(sb2));
        allowedPorts.put(sb2, Collections.singleton(sa2));
        //BLUE
        ConnectPoint sa3 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000a"),
                PortNumber.portNumber(3));
        ConnectPoint sb3 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000b"),
                PortNumber.portNumber(3));
        ConnectPoint sc2 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000c"),
                PortNumber.portNumber(2));
        Set<ConnectPoint> sa = new HashSet<>();
        sa.add(sb3);
        sa.add(sc2);
        allowedPorts.put(sa3, sa);
        allowedPorts.put(sb2, Collections.singleton(sb3));
        allowedPorts.put(sc2, Collections.singleton(sa3));

        //GREEN
        ConnectPoint sc3 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000c"),
                PortNumber.portNumber(3));
        ConnectPoint sb4 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000b"),
                PortNumber.portNumber(4));
        allowedPorts.put(sc3, Collections.singleton(sb4));
        allowedPorts.put(sb4, Collections.singleton(sc3));
        log.debug("allowedPorts {}", allowedPorts);


        ConnectPoint s114 = new ConnectPoint(
                DeviceId.deviceId("of:0000000000000001"),
                PortNumber.portNumber(14));
        ConnectPoint s214 = new ConnectPoint(
                DeviceId.deviceId("of:0000000000000002"),
                PortNumber.portNumber(14));
        allowedPorts.put(s114, Collections.singleton(s214));
        allowedPorts.put(s214, Collections.singleton(s114));
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterCompiler(AciIntent.class);
        allowedPorts.clear();
    }

    @Modified
    public void modified(ComponentContext context) {
        if (context == null) {
            opticalBw = DEFAULT_OPTICAL_BW;
            macSecBw = DEFAULT_MACSEC_BW;
            log.info(
                    "Optical Bandwidth Threshold reconfigured to default {} Mbps and " +
                            "MacSec Bandwidth Threshold reconfigured to default {} Mbps",
                    opticalBw.bps() / 1_000_000L, macSecBw.bps() / 1_000_000L);
        } else {
            String optical = Tools.get(context.getProperties(), "opticalBw");
            Bandwidth newOpticalBw =
                    isNullOrEmpty(optical) ? opticalBw : Bandwidth
                            .mbps(Long.parseLong(optical));
            if (newOpticalBw.bps() != opticalBw.bps() / 1_000_000L) {
                opticalBw = newOpticalBw;
                log.info("Optical Bandwidth Threshold reconfigured to {} Mbps",
                         opticalBw.bps() / 1_000_000L);
            }
            String macSec = Tools.get(context.getProperties(), "macSecBw");
            Bandwidth newmacSecBw = isNullOrEmpty(macSec) ? macSecBw : Bandwidth
                    .mbps(Long.parseLong(macSec));
            if (newmacSecBw.bps() != macSecBw.bps() / 1_000_000L) {
                macSecBw = newmacSecBw;
                log.info("MacSec Bandwidth Threshold reconfigured to {} Mbps",
                         macSecBw.bps() / 1_000_000L);
            }
        }
    }

    @Override
    public List<Intent> compile(AciIntent intent, List<Intent> installable) {
        log.debug("ACiIntent requested key {} {}", intent.key(), System.currentTimeMillis());
        List<Intent> intents = new ArrayList<>();
//        if (installable != null && !installable.isEmpty()) {
//            log.info("Recompiling failed intents");
//            intents.addAll(getRecompilation(intent, installable));
//        } else {
        intents.addAll(compile(intent));
//        }
        log.debug("ACiIntent compiled key {} {}", intent.key(), System.currentTimeMillis());
        return intents;
    }

    protected List<Path> getPaths(AciIntent intent,
                                  ElementId one, ElementId two) {

        Set<Path> paths = pathService.getPaths(one, two);
        if (paths.isEmpty()) {
            throw new IntentException("Cannot find a path between " + one + " and " + two);
        }

        final List<Constraint> initialConstraints = intent.constraints();

        ImmutableList<Path> filtered = paths.stream()
                .filter(path -> checkPath(path, initialConstraints))
                .filter(path -> portCheck(path))
                .collect(ImmutableList.toImmutableList());
        if (filtered.isEmpty()) {
            //Try with disjoint path
            Set<DisjointPath> disjointPaths = pathService.getDisjointPaths(one, two);
            if (disjointPaths.isEmpty()) {
                if (paths.size() >= 1 && NegotiableConstraint.negotiationAllowed(intent)) {
                    //Disjoint doesn't work if the topology does not have multiple paths
                    List<Intent> alternativeIntents = Lists.newArrayList();
                    for (Path path : paths) {
                        List<Constraint> pathConstraints = supportedPathConstraints(path, initialConstraints);

                        AciIntent alternativeACIintent = AciIntent.builder()
                                .key(intent.key())
                                .appId(intent.appId())
                                .one(intent.one())
                                .two(intent.two())
                                .priority(intent.priority())
                                .treatment(intent.treatment())
                                .selector(intent.selector())
                                .constraints(pathConstraints)
                                .build();
                        alternativeIntents.add(alternativeACIintent);
                    }
                    throw new IntentNegotiationException(intent, alternativeIntents);
                } else {
                    throw new IntentException("Cannot find a path between " + one + " and " + two);
                }
            }

            ImmutableList<Path> disjointFiltered = FluentIterable.from(disjointPaths)
                    .filter(path -> checkPath(path.backup(), initialConstraints))
                    .transform(path -> path.backup())
                    .toList();

            if (disjointFiltered.isEmpty()) {
                if (!NegotiableConstraint.negotiationAllowed(intent)) {
                    throw new IntentException("The ACI intent " + intent.key() + " constraints cannot be satisfied");
                }
                List<Intent> alternativeIntents = Lists.newArrayList();

                for (DisjointPath path : disjointPaths) {
                    List<Constraint> pathConstraints = supportedPathConstraints(path.primary(), initialConstraints);

                    //Only for NetSoft demo:
                    // we add the same intent with HighAvailability
                    AciIntent alternativeACIintent = AciIntent.builder()
                            .key(intent.key())
                            .appId(intent.appId())
                            .one(intent.one())
                            .two(intent.two())
                            .priority(intent.priority())
                            .treatment(intent.treatment())
                            .selector(intent.selector())
                            .constraints(pathConstraints)
                            .build();
                    alternativeIntents.add(alternativeACIintent);

                    if (path.backup() != null) {

                        List<Constraint> backupPathConstraints = supportedPathConstraints(path.backup(), initialConstraints);
                        //Only for NetSoft demo:
                        // we add the same intent with HighAvailability
                        AciIntent alternativeACIintentBackup = AciIntent.builder()
                                .key(intent.key())
                                .appId(intent.appId())
                                .one(intent.one())
                                .two(intent.two())
                                .priority(intent.priority())
                                .treatment(intent.treatment())
                                .selector(intent.selector())
                                .constraints(backupPathConstraints)
                                .build();
                        alternativeIntents.add(alternativeACIintentBackup);
                    }
                }

                throw new IntentNegotiationException(intent, alternativeIntents);
            }
            return disjointFiltered;
        }
        // TODO: let's be more intelligent about this eventually
        return filtered;
    }


    private boolean portCheck(Path path) {

        Iterator<Link> iter = path.links().iterator();
        while (iter.hasNext()) {
            Set<ConnectPoint> dstCP = allowedPorts.get(iter.next().src());
            if (dstCP != null && !dstCP.isEmpty()) {
                while (iter.hasNext()) {
                    if (dstCP.contains(iter.next().dst())) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private boolean portCheck(Path path, ConnectPoint one, ConnectPoint two) {
        Iterator<Link> iter = path.links().iterator();


        while (iter.hasNext()) {
            Link link = iter.next();
            if (link.type() == Link.Type.EDGE && iter.hasNext()) {
                link = iter.next();
            }
            ConnectPoint dstCP = null;
            ConnectPoint test = link.src();
            if (test.equals(one)) {
                dstCP = two;
            } else if (test.equals(two)) {
                dstCP = one;
            }
            if (dstCP != null) {
                while (iter.hasNext()) {
                    if (dstCP.equals(link.dst())) {
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    /**
     * Checks if a given connect point supports encryption.
     *
     * @param connectPoint the connection point to be checked
     * @return does the connection point support encryption and false for hosts
     */
    boolean isPortEncrypted(ConnectPoint connectPoint) {
        // TODO: Special handling for hosts?
        if (connectPoint.elementId() instanceof DeviceId) {
            Device device = deviceService.getDevice(connectPoint.deviceId());
            Port port = deviceService.getPort(device.id(), connectPoint.port());
            String encryption = port.annotations().value(ENCRYPTION);
            return encryption != null;
        } else {
            return false;
        }
    }

    /**
     * Checks if a given connect point supports encryption and is of given type.
     *
     * @param connectPoint the connection point to be checked
     * @param type         the relevant type
     * @return does the connection point support encryption and is of given type
     */
    boolean isPortEncryptedAndType(ConnectPoint connectPoint,
                                   Device.Type type) {
        // this assumes that isPortEncrypted returns false for hosts
        return isPortEncrypted(connectPoint) && type
                .equals(deviceService.getDevice(connectPoint.deviceId())
                                .type());
    }

    /**
     * Checks if the path passes through encrypted intefaces.
     *
     * @param path the path to be checked
     * @return not passing any encrypted interfaces
     */
    private boolean isUnencrypted(Path path) {
        for (Link link : path.links()) {
            if (isPortEncrypted(link.src()) || isPortEncrypted(link.dst())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if there is an WDM encrypted hop along the path.
     *
     * @param path the path to be inspected
     * @return if the path contains an encrypted hop
     */
    boolean isWdmEncrypted(Path path) {
        boolean source = false, destination = false;
        for (Link link : path.links()) {
            source |= isPortEncryptedAndType(link.src(), Device.Type.ROADM);
            destination |=
                    isPortEncryptedAndType(link.dst(), Device.Type.ROADM);
        }
        return source && destination;
    }

    /**
     * Checks if the switches along the path support encryption.
     *
     * @param path the path to be inspected
     * @return are all devices encrypted
     */
    boolean isMacEncrypted(Path path) {
        // this is needed to skip the first hop from the host
        for (Link link : path.links()) {
            if (link.src().elementId() instanceof DeviceId) {
                Device src = deviceService.getDevice(link.src().deviceId());
                String encryption = src.annotations().value(ENCRYPTION);
                if (Device.Type.SWITCH
                        .equals(src.type()) && encryption != null) {
                    Device dst = deviceService.getDevice(link.dst().deviceId());
                    encryption = dst.annotations().value(ENCRYPTION);
                    if (Device.Type.SWITCH
                            .equals(dst.type()) && encryption != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Intent> getRecompilation(AciIntent intent,
                                          List<Intent> installable) {
        //TODO: manage asymmetric!

        Optional<RestorationConstraint> restorationConstraint =
                intent.constraints().stream()
                        .filter(constraint -> constraint instanceof RestorationConstraint)
                        .map(x -> (RestorationConstraint) x).findAny();
        if (!restorationConstraint.isPresent()) {
            //if no RestorationConstraint is involved only a single switch use the default behaviour
            return recompile(intent);
        } else {
            switch (restorationConstraint.get().restorationType()) {
                case IP:
                    log.info("Recompiling using IP Restoration");
                    return recompileIpRestoration(intent, findIppath(intent));
                case OPTICAL:
                    log.info("Recompiling using Optical Restoration");
                    //TODO: here we need to use an optical intent
                    return recompile(intent);
                default:
                    return Collections.emptyList();
            }
        }
    }

    private Collection<AciIntent> findIppath(AciIntent intent) {
        Collection<AciIntent> intents = new LinkedList<>();
        for (Intent src : intentService.getIntents()) {
            if (src instanceof AciIntent) {
                if (((AciIntent) src).one().equals(intent.one())) {
                    for (Intent dst : intentService.getIntents()) {
                        if (dst instanceof AciIntent) {
                            if (((AciIntent) dst).two().equals(intent.two())) {
                                if (((AciIntent) src).two()
                                        .equals(((AciIntent) dst).one())) {
                                    intents.add((AciIntent) src);
                                    intents.add((AciIntent) dst);
                                    return intents;
                                }
                            }
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Intent> recompileIpRestoration(AciIntent intent,
                                                Collection<AciIntent> aciIntents) {
        if (aciIntents == null || aciIntents.isEmpty()) {
            log.error("No optical paths installed: IP restoration failed");
            return Collections.emptyList();
        }
        Iterator<AciIntent> iter = aciIntents.iterator();
        List<Intent> intents = new ArrayList<>();

        AciIntent src = iter.next();
        Path path = getPaths(intent, src.one(), src.two()).iterator().next();
        List<Link> srcLinks = new ArrayList<>();
        srcLinks.add(path.links().get(0));
        srcLinks.add(DefaultLink.builder()
                             .providerId(path.links().get(0).providerId())
                             .src(path.links().get(1).src())
                             .dst(path.links().get(path.links().size() - 2)
                                          .dst())
                             .type(path.links().get(1).type())
                             .state(Link.State.ACTIVE)
                             .annotations(path.links().get(1).annotations())
                             .build());

        Path srcPath = new DefaultPath(path.providerId(), srcLinks,
                                       new ScalarWeight(1));

        Host one = hostService.getHost(intent.one());
        Host two = hostService.getHost(intent.two());
        intents.addAll(createLinkIntent(srcPath, one, two, intent));

        AciIntent dst = iter.next();

        Path pathh = getPaths(intent, dst.two(), dst.one()).iterator().next();
        List<Link> dstLinks = new ArrayList<>();
        dstLinks.add(pathh.links().get(0));
        dstLinks.add(DefaultLink.builder()
                             .providerId(pathh.links().get(0).providerId())
                             .src(pathh.links().get(1).src())
                             .dst(pathh.links().get(path.links().size() - 2)
                                          .dst())
                             .type(pathh.links().get(1).type())
                             .state(Link.State.ACTIVE)
                             .annotations(pathh.links().get(1).annotations())
                             .build());
/*        dstLinks.add(new DefaultLink(path.links().get(0).providerId(),
                                     path.links().get(1).src(),
                                     path.links().get(path.links().size() - 2).dst(),
                                     path.links().get(1).type(),
                                     Link.State.ACTIVE,
                                     path.links().get(1).annotations()));*/
//        dstLinks.add(path.links().get(path.links().size() - 1));
        Path dstPath = new DefaultPath(pathh.providerId(), dstLinks,
                                       new ScalarWeight(1));

        intents.addAll(createLinkIntent(dstPath, two, one, intent));
        if (intents.isEmpty()) {
            log.error("IP compilation failed!");
        }
        return intents;
    }


    private List<Intent> compile(AciIntent intent) {

        boolean isAsymmetric =
                intent.constraints().contains(new AsymmetricPathConstraint());
        boolean isEncrypted =
                intent.constraints().contains(new EncryptionConstraint());
        Host hostOne = hostService.getHost(intent.one());
        Host hostTwo = hostService.getHost(intent.two());
        if (hostOne == null || hostTwo == null) {
            log.error("Hosts are null");
            throw new IntentCompilationException("Hosts do not exist");
        }

        List<Path> filtered;
        if (!isEncrypted) {
            filtered = FluentIterable
                    .from(getPaths(intent, intent.one(), intent.two()))
                    //.filter(path -> checkPath(path, constraints))
                    .filter(path -> {
                        for (Link link : path.links()) {
                            if (link.src().equals(j1p230) || link.dst()
                                    .equals(j1p230)) {
                                return true;
                            }
                        }
                        return false;

                    })
                    .toList();
            if (filtered.isEmpty()) {
                filtered = getPaths(intent, intent.one(), intent.two()).stream()
                        //.filter(path -> checkPath(path, constraints))
                        .filter(this::portCheck)
                        .filter(this::isUnencrypted)
                        .collect(Collectors.toList());
            }
            //Encryption constrain
        } else {
            log.info("Traffic must be encrypted");


            Device deviceOne =
                    deviceService.getDevice(hostOne.location().deviceId());
            Device deviceTwo =
                    deviceService.getDevice(hostTwo.location().deviceId());
            if (deviceOne == null || deviceTwo == null) {
                throw new IntentCompilationException("Devices does not exist!");
            }
            if (deviceOne.id().equals(deviceTwo.id())) {
                throw new IntentCompilationException(
                        "Ingress and Egress points must be different");
            }
            Optional<BandwidthConstraint> bwConstraint = intent.constraints()
                    .stream().filter(x -> x instanceof BandwidthConstraint)
                    .map(BandwidthConstraint.class::cast).findAny();
            Optional<LatencySensitive> latencyConstraint = intent.constraints()
                    .stream().filter(x -> x instanceof LatencySensitive)
                    .map(LatencySensitive.class::cast).findAny();


            //if the requested bandwidth is major than the optical bandwidth threshold
            //or if the traffic is latency sensitive the compiler chooses the Optical encryption.
            if (bwConstraint.isPresent() &&
                    bwConstraint.get().bandwidth()
                            .compareTo(opticalBw) < 0 &&
                    bwConstraint.get().bandwidth()
                            .compareTo(macSecBw) >= 0) {
                log.info("Applying MacSec encryption");
                // MACsec encryption
                filtered = getPaths(intent, intent.one(), intent.two())
                        .stream()
                        .filter(this::isMacEncrypted)
                        .collect(
                                Collectors.toList());
            } else if ((bwConstraint.isPresent() &&
                    bwConstraint.get().bandwidth()
                            .compareTo(opticalBw) >= 0)) {
                log.info("Applying Optical encryption");
                //Optical encryption
                filtered = getPaths(intent, intent.one(), intent.two())
                        .stream()
                        .filter(this::isWdmEncrypted)
                        .collect(
                                Collectors.toList());
            } else {
                //IP encryption
                log.info("Applying IP encryption");

                //generate the optical intent
                generateOpticalIntent(deviceOne, deviceTwo, intent);

                //Find the two ports of the tunnel
                Optional<Port> portOne =
                        deviceService.getPorts(deviceOne.id()).stream()
                                .filter(port -> port.annotations()
                                        .value(AnnotationKeys.PORT_NAME)
                                        .equals("br0-gre") && port
                                        .isEnabled()).findAny();
                Optional<Port> portTwo =
                        deviceService.getPorts(deviceTwo.id()).stream()
                                .filter(port -> port.annotations()
                                        .value(AnnotationKeys.PORT_NAME)
                                        .equals("br0-gre") && port
                                        .isEnabled()).findAny();

                if (!portOne.isPresent() || !portTwo.isPresent()) {
                    //createTunnels(deviceOne, deviceTwo);
                    throw new IntentCompilationException(
                            "Tunnel has not been created!");
                }

                //filter the path to use the tunnel ports
                filtered = FluentIterable.from(pathService.getPaths(
                        intent.one(), intent.two()))
                        .filter(path -> portCheck(path,
                                                  new ConnectPoint(
                                                          deviceOne.id(),
                                                          portOne.get()
                                                                  .number()),
                                                  new ConnectPoint(
                                                          deviceTwo.id(),
                                                          portTwo.get()
                                                                  .number()))
                        ).toList();
            }
        }
        if (filtered.isEmpty()) {
            throw new IntentCompilationException(
                    "Path not found with given constraints");
        }

        Path pathOne = filtered.iterator().next();
        Path pathTwo = invertPath(pathOne);

        List<Intent> intents =
                createLinkIntent(pathOne, hostOne, hostTwo, intent);
        intents.addAll(createLinkIntent(pathTwo, hostTwo, hostOne, intent));
        return intents;
    }

    private void generateOpticalIntent(Device deviceOne, Device deviceTwo,
                                       AciIntent intent) {
        //Test!
        Optional<Port> portOneOptical =
                deviceService.getPorts(deviceOne.id()).stream()
                        .filter(port -> port.annotations()
                                .value(AnnotationKeys.PORT_NAME)
                                .equals("int")).findAny();
        Optional<Port> portTwoOptical =
                deviceService.getPorts(deviceTwo.id()).stream()
                        .filter(port -> port.annotations()
                                .value(AnnotationKeys.PORT_NAME)
                                .equals("int")).findAny();
        if (!portOneOptical.isPresent() || !portTwoOptical.isPresent()) {
            throw new IntentCompilationException(
                    "Tunnel has not been created!");
        }

        Set<Host> hostsOne = hostService.getHostsByMac(
                MacAddress.valueOf(portOneOptical.get().annotations()
                                           .value(AnnotationKeys.PORT_MAC)));

        Set<Host> hostsTwo = hostService.getHostsByMac(
                MacAddress.valueOf(portTwoOptical.get().annotations()
                                           .value(AnnotationKeys.PORT_MAC)));

        if (hostsOne.size() > 1 && hostsTwo.size() > 1) {
            log.error("Hosts are multiple!");
        }
        Host hostOne = hostsOne.iterator().next();
        Host hostTwo = hostsTwo.iterator().next();

        for (Intent existingIntent : intentService.getIntents()) {
            if (existingIntent instanceof HostToHostIntent) {
                HostToHostIntent intentFound =
                        (HostToHostIntent) existingIntent;
                if ((intentFound.one().equals(hostOne.id()) && intentFound.two()
                        .equals(hostTwo.id()))
                        || (intentFound.two()
                        .equals(hostOne.id()) && intentFound.one()
                        .equals(hostTwo.id()))) {
                    //TODO: check the intent state?
                    IntentState state =
                            intentService.getIntentState(intentFound.key());
                    if (state != IntentState.WITHDRAWN &&
                            state != IntentState.WITHDRAW_REQ &&
                            state != IntentState.WITHDRAWING &&
                            state != IntentState.PURGE_REQ) {
                        log.info("Optical intent already created!");
                        return;
                    }
                }
            }
        }


        ObstacleConstraint oc = new ObstacleConstraint(
                DeviceId.deviceId("restproxy:ofceth182"),
                DeviceId.deviceId("restproxy:ofcwdm184"),
                DeviceId.deviceId("restproxy:ofcwdm183"),
                DeviceId.deviceId("restproxy:ofceth181"));

        Intent h2h = HostToHostIntent.builder()
                .appId(intent.appId())
                .one(hostOne.id())
                .two(hostTwo.id())
                .selector(DefaultTrafficSelector.emptySelector())
                .treatment(DefaultTrafficTreatment.emptyTreatment())
                .constraints(ImmutableList.of(oc))
                .priority(intent.priority())
                .build();

        intentService.submit(h2h);

        //End test!

//        for (Intent existingIntent : intentService.getIntents()) {
//            if (existingIntent instanceof AciPathIntent) {
//                AciPathIntent aciExistingIntent = (AciPathIntent) existingIntent;
//                DeviceId intentSrc = aciExistingIntent.path().src().deviceId();
//                DeviceId intentDst = aciExistingIntent.path().dst().deviceId();
//                if ((intentSrc.equals(deviceOne.id()) && intentDst.equals(deviceTwo.id()))
//                        || (intentSrc.equals(deviceTwo.id()) && intentDst.equals(deviceOne.id()))) {
//                    //TODO: check the intent state?
//                    log.info("Optical intent already created!");
//                    return;
//                }
//            }
//        }
//
//        Set<Path> paths = pathService.getPaths(deviceOne.id(), deviceTwo.id());
//        if (paths.isEmpty()) {
//            log.error("Optical path not found");
//            throw new IntentCompilationException("Tunnel has not been created!");
//        }
//
//
//
//        intentService.submit(AciPathIntent.builder()
//                .appId(intent.appId())
//                .selector(DefaultTrafficSelector.emptySelector())
//                .treatment(DefaultTrafficTreatment.emptyTreatment())
//                .path(paths.iterator().next())
//                .constraints(Collections.emptyList())
//                .priority(intent.priority())
//                .build());
    }

    private void generateOpticalIntentForMacSec(Device deviceOne,
                                                Device deviceTwo,
                                                AciIntent intent) {

        for (Intent existingIntent : intentService.getIntents()) {
            if (existingIntent instanceof AciPathIntent) {
                AciPathIntent aciExistingIntent =
                        (AciPathIntent) existingIntent;
                DeviceId intentSrc = aciExistingIntent.path().src().deviceId();
                DeviceId intentDst = aciExistingIntent.path().dst().deviceId();
                if ((intentSrc.equals(deviceOne.id()) && intentDst
                        .equals(deviceTwo.id()))
                        || (intentSrc.equals(deviceTwo.id()) && intentDst
                        .equals(deviceOne.id()))) {
                    log.info("Optical intent already created!");
                    return;
                }
            }
        }
        Set<Path> paths = pathService.getPaths(deviceOne.id(), deviceTwo.id());
        if (paths.isEmpty()) {
            log.error("Optical path not found");
        }

        intentService.submit(AciPathIntent.builder()
                                     .appId(intent.appId())
                                     .selector(DefaultTrafficSelector
                                                       .emptySelector())
                                     .treatment(DefaultTrafficTreatment
                                                        .emptyTreatment())
                                     .path(paths.iterator().next())
                                     .constraints(Collections.emptyList())
                                     .priority(intent.priority())
                                     .build());
    }


    private List<Intent> recompile(AciIntent intent) {

        boolean isAsymmetric =
                intent.constraints().contains(new AsymmetricPathConstraint());
        Path pathOne =
                getPaths(intent, intent.one(), intent.two()).iterator().next();
        Path pathTwo = isAsymmetric ?
                getPaths(intent, intent.two(), intent.one()).iterator()
                        .next() : invertPath(pathOne);

        Host one = hostService.getHost(intent.one());
        Host two = hostService.getHost(intent.two());

        List<Intent> intents = createLinkIntent(pathOne, one, two, intent);
        intents.addAll(createLinkIntent(pathTwo, two, one, intent));
        return intents;
    }


    // Inverts the specified path. This makes an assumption that each link in
    // the path has a reverse link available. Under most circumstances, this
    // assumption will hold.
    private Path invertPath(Path path) {
        List<Link> reverseLinks = new ArrayList<>(path.links().size());
        for (Link link : path.links()) {
            reverseLinks.add(0, reverseLink(link));
        }
        return new DefaultPath(path.providerId(), reverseLinks, path.weight());
    }

    // Produces a reverse variant of the specified link.
    private Link reverseLink(Link link) {
        return DefaultLink.builder().providerId(link.providerId())
                .src(link.dst())
                .dst(link.src())
                .type(link.type())
                .state(link.state())
                .isExpected(link.isExpected())
                .build();
    }

    /**
     * Validates the specified path against the given constraints.
     *
     * @param path        path to be checked
     * @param constraints path constraints
     * @return true if the path passes all constraints
     */
    protected boolean checkPath(Path path, List<Constraint> constraints) {

        if (path instanceof DisjointPath) {
            DisjointPath pathToCheck = (DisjointPath) path;
            for (Constraint constraint : constraints) {
                if (!constraint.validate(pathToCheck, resourceService::isAvailable)) {
                    if (!constraint.validate(pathToCheck.backup(), resourceService::isAvailable)) {
                        return false;
                    } else {
                        pathToCheck.useBackup();
                    }
                }
            }
            return true;
        }

        for (Constraint constraint : constraints) {
            if (!constraint.validate(path, resourceService::isAvailable)) {
                return false;
            }
        }
        return true;
    }

    private List<Constraint> supportedPathConstraints(Path path, List<Constraint> initialConstraints) {
        List<ConnectPoint> pathCPs =
                path.links().stream()
                        .flatMap(l -> Stream.of(l.src(), l.dst()))
                        .collect(Collectors.toList());

        double bandwidthLeft = 0;
        double currentBandwidth = 0;

        for (ConnectPoint point : pathCPs) {
            if (point.elementId() instanceof DeviceId) {
                currentBandwidth = leftResources(point);

                if (currentBandwidth == 0) {
                    bandwidthLeft = 0;
                    break;
                }

                if (bandwidthLeft == 0) {
                    bandwidthLeft = currentBandwidth;
                }

                if (currentBandwidth < bandwidthLeft) {
                    bandwidthLeft = currentBandwidth;
                }
            }
        }

        Bandwidth bw = Bandwidth.bps(bandwidthLeft);
        BandwidthConstraint bwConstraint = new BandwidthConstraint(bw);

        double pathLatency = path.links().stream().mapToDouble(this::cost).sum();

        LatencyConstraint latencyConstraint = new LatencyConstraint(Duration.of((long) pathLatency, ChronoUnit.NANOS));

        List<Constraint> pathConstraints = Lists.newArrayList(bwConstraint, latencyConstraint);

        return pathConstraints.stream()
                .filter(pathConstraint -> {
                    for (Constraint c : initialConstraints) {
                        if ((pathConstraint instanceof BandwidthConstraint) &&
                                (c instanceof BandwidthConstraint)) {
                            return true;
                        }
                        if ((pathConstraint instanceof LatencyConstraint) &&
                                (c instanceof LatencyConstraint)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(pathConstraint -> {
                    for (Constraint initialConst : initialConstraints) {
                        if ((pathConstraint instanceof BandwidthConstraint) &&
                                (initialConst instanceof BandwidthConstraint)) {
                            if (((BandwidthConstraint) pathConstraint).bandwidth()
                                    .isGreaterThan(((BandwidthConstraint) initialConst).bandwidth())) {
                                return initialConst;
                            }
                        }
                        if ((pathConstraint instanceof LatencyConstraint) &&
                                (initialConst instanceof LatencyConstraint)) {
                            return pathConstraint;
                        }
                    }
                    return pathConstraint;
                })
                .collect(Collectors.toList());
    }

    private double cost(Link link) {
        //Check only links, not EdgeLinks
        if (link.type() != Link.Type.EDGE) {
            return link.annotations().value(AnnotationKeys.LATENCY) != null
                    ? getAnnotatedValue(link, AnnotationKeys.LATENCY) : 0;
        } else {
            return 0;
        }
    }

    /**
     * Returns the amount of resources left with respect to the current allocation
     *
     * @param connectPoint requested resource
     * @return true if there is enough resource volume. Otherwise, false.
     */
    // computational complexity: O(n) where n is the number of allocations
    private double leftResources(ConnectPoint connectPoint) {

        DiscreteResource resource = Resources.discrete(connectPoint.deviceId(), connectPoint.port()).resource();

        Set<Resource> resourceValues = resourceService.getRegisteredResources(resource.id());

        double original = resourceValues.stream()
                .filter(x -> x instanceof ContinuousResource)
                .map(x -> (ContinuousResource) x)
                .mapToDouble(ContinuousResource::value)
                .sum();

        Collection<ResourceAllocation> resourceAllocations =
                resourceService.getResourceAllocations(resource.id(), Bandwidth.class);

        double allocated = resourceAllocations.stream()
                .filter(x -> x.resource() instanceof ContinuousResource)
                .map(x -> (ContinuousResource) x.resource())
                .mapToDouble(ContinuousResource::value)
                .sum();

        return original - allocated;
    }

    /**
     * Allocates the bandwidth specified as intent constraint on each link
     * composing the intent, if a bandwidth constraint is specified.
     *
     * @param intent        the intent requesting bandwidth allocation
     * @param connectPoints the connect points composing the intent path computed
     */
    protected void allocateBandwidth(ConnectivityIntent intent,
                                     List<ConnectPoint> connectPoints) {
        // Retrieve bandwidth constraint if exists
        List<Constraint> constraints = intent.constraints();

        if (constraints == null) {
            return;
        }

        Optional<Constraint> constraint =
                constraints.stream()
                        .filter(c -> c instanceof BandwidthConstraint)
                        .findAny();

        // If there is no bandwidth constraint continue
        if (!constraint.isPresent()) {
            return;
        }

        BandwidthConstraint bwConstraint = (BandwidthConstraint) constraint.get();

        double bw = bwConstraint.bandwidth().bps();

        // If a resource group is set on the intent, the resource consumer is
        // set equal to it. Otherwise it's set to the intent key
        ResourceConsumer newResourceConsumer =
                intent.resourceGroup() != null ? intent.resourceGroup() : intent.key();

        // Get the list of current resource allocations
        Collection<ResourceAllocation> resourceAllocations =
                resourceService.getResourceAllocations(newResourceConsumer);

        // Get the list of resources already allocated from resource allocations
        List<Resource> resourcesAllocated =
                resourcesFromAllocations(resourceAllocations);

        // Get the list of resource ids for resources already allocated
        List<ResourceId> idsResourcesAllocated = resourceIds(resourcesAllocated);

        // Create the list of incoming resources requested. Exclude resources
        // already allocated.
        List<Resource> incomingResources =
                resources(connectPoints, bw).stream()
                        .filter(r -> !resourcesAllocated.contains(r))
                        .collect(Collectors.toList());

        if (incomingResources.isEmpty()) {
            return;
        }

        // Create the list of resources to be added, meaning their key is not
        // present in the resources already allocated
        List<Resource> resourcesToAdd =
                incomingResources.stream()
                        .filter(r -> !idsResourcesAllocated.contains(r.id()))
                        .collect(Collectors.toList());

        // Resources to updated are all the new valid resources except the
        // resources to be added
        List<Resource> resourcesToUpdate = Lists.newArrayList(incomingResources);
        resourcesToUpdate.removeAll(resourcesToAdd);

        // If there are no resources to update skip update procedures
        if (!resourcesToUpdate.isEmpty()) {
            // Remove old resources that need to be updated
            // TODO: use transaction updates when available in the resource service
            List<ResourceAllocation> resourceAllocationsToUpdate =
                    resourceAllocations.stream()
                            .filter(rA -> resourceIds(resourcesToUpdate).contains(rA.resource().id()))
                            .collect(Collectors.toList());
            log.debug("Releasing bandwidth for intent {}: {} bps", newResourceConsumer, resourcesToUpdate);
            resourceService.release(resourceAllocationsToUpdate);

            // Update resourcesToAdd with the list of both the new resources and
            // the resources to update
            resourcesToAdd.addAll(resourcesToUpdate);
        }

        // Look also for resources allocated using the intent key and -if any-
        // remove them
        if (intent.resourceGroup() != null) {
            // Get the list of current resource allocations made by intent key
            Collection<ResourceAllocation> resourceAllocationsByKey =
                    resourceService.getResourceAllocations(intent.key());

            resourceService.release(Lists.newArrayList(resourceAllocationsByKey));
        }

        // Allocate resources
        log.debug("Allocating bandwidth for intent {}: {} bps", newResourceConsumer, resourcesToAdd);
        List<ResourceAllocation> allocations =
                resourceService.allocate(newResourceConsumer, resourcesToAdd);

        if (allocations.isEmpty()) {
            log.debug("No resources allocated for intent {}", newResourceConsumer);
        }

        log.debug("Done allocating bandwidth for intent {}", newResourceConsumer);
    }

    /**
     * Produces a list of resources from a list of resource allocations.
     *
     * @param rAs the list of resource allocations
     * @return a list of resources retrieved from the resource allocations given
     */
    private static List<Resource> resourcesFromAllocations(Collection<ResourceAllocation> rAs) {
        return rAs.stream()
                .map(ResourceAllocation::resource)
                .collect(Collectors.toList());
    }

    /**
     * Creates a list of continuous bandwidth resources given a list of connect
     * points and a bandwidth.
     *
     * @param cps the list of connect points
     * @param bw  the bandwidth expressed as a double
     * @return the list of resources
     */
    private static List<Resource> resources(List<ConnectPoint> cps, double bw) {
        return cps.stream()
                // Make sure the element id is a valid device id
                .filter(cp -> cp.elementId() instanceof DeviceId)
                // Create a continuous resource for each CP we're going through
                .map(cp -> Resources.continuous(cp.deviceId(), cp.port(),
                                                Bandwidth.class).resource(bw))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of resource ids given a list of resources.
     *
     * @param resources the list of resources
     * @return the list of resource ids retrieved from the resources given
     */
    private static List<ResourceId> resourceIds(List<Resource> resources) {
        return resources.stream()
                .map(Resource::id)
                .collect(Collectors.toList());
    }


    // Creates a path intent from the specified path and original connectivity intent.
    private List<Intent> createLinkIntent(Path path, Host src, Host dst,
                                          AciIntent intent) {
        List<Intent> intents = new ArrayList<>();
        if (dst.ipAddresses().isEmpty()) {
            log.error("Destination host IP is null!");
        }

        // Try to allocate bandwidth
        List<ConnectPoint> pathCPs =
                path.links().stream()
                        .flatMap(l -> Stream.of(l.src(), l.dst()))
                        .collect(Collectors.toList());

        allocateBandwidth(intent, pathCPs);

        Link ingressLink = path.links().get(0);
        Link egressLink = path.links().get(path.links().size() - 1);

        FilteredConnectPoint ingressPoint = getFilteredPointFromLink(ingressLink);
        FilteredConnectPoint egressPoint = getFilteredPointFromLink(egressLink);

        // TODO: is this still needed? The IP was killing OpenFlow!
        // Made changed here to work with old model
        for (IpAddress ip : dst.ipAddresses()) {
            TrafficSelector selector =
                    DefaultTrafficSelector.builder(intent.selector())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            .matchEthDst(dst.mac())
                            .matchEthSrc(src.mac())
                            .matchIPDst(IpPrefix.valueOf(ip, 32))
                            .build();

            // TODO: why do we need this? The constraints are immutalbe !!!
            //            if (!intent.constraints().contains(DomainConstraint.domain())) {
            //                intent.constraints().add(DomainConstraint.domain());
            //            }
            // Only links between devices are relevant
            Set<Link> coreLinks = path.links()
                    .stream()
                    .filter(link -> !link.type().equals(EDGE))
                    .collect(Collectors.toSet());
            // The ingress connection point is the one from a host to a device
/*        Set<FilteredConnectPoint> ingress = path.links()
                .stream()
                .filter(link -> link.src().elementId() instanceof HostId && link
                        .dst()
                        .elementId() instanceof DeviceId)
                .map(link -> new FilteredConnectPoint(link.dst()))
                .collect(GuavaCollectors.toImmutableSet());
        // The egress connection point is the one from a device to a host
        Set<FilteredConnectPoint> egress = path.links()
                .stream()
                .filter(link -> link.src().elementId() instanceof DeviceId
                        && link.dst().elementId() instanceof HostId)
                .map(link -> new FilteredConnectPoint(link.src()))
                .collect(GuavaCollectors.toImmutableSet());*/
            // the parent's key is used to identify intents that belong together
            intents.add(LinkCollectionIntent.builder()
                                .key(intent.key())
                                .filteredIngressPoints(ImmutableSet.of(
                                        ingressPoint
                                ))
                                .filteredEgressPoints(ImmutableSet.of(
                                        egressPoint
                                ))
                                .appId(intent.appId())
                                .selector(selector)
                                .treatment(intent.treatment())
                                .links(coreLinks)
                                .constraints(intent.constraints())
                                .priority(intent.priority())
                                .build());
        }
        return intents;
    }

    private FilteredConnectPoint getFilteredPointFromLink(Link link) {
        FilteredConnectPoint filteredConnectPoint;
        if (link.src().elementId() instanceof DeviceId) {
            filteredConnectPoint = new FilteredConnectPoint(link.src());
        } else if (link.dst().elementId() instanceof DeviceId) {
            filteredConnectPoint = new FilteredConnectPoint(link.dst());
        } else {
            throw new IntentCompilationException(DEVICE_ID_NOT_FOUND);
        }
        return filteredConnectPoint;
    }

//    // Creates a path intent from the specified path and original connectivity intent.
//    private List<Intent> createIpIntent(Path path, Host src, Host dst,
//                                        AciIntent intent) {
//        List<Intent> intents = new ArrayList<>();
//        if (dst.ipAddresses().isEmpty()) {
//            log.error("Destination host ip is null!");
//        }
//        // Made changed here to work with old model
//        for (IpAddress ip : dst.ipAddresses()) {
//            TrafficSelector selector =
//                    DefaultTrafficSelector.builder(intent.selector())
//                            .matchIPDst(IpPrefix.valueOf(ip, 32))
//                            .matchEthDst(dst.mac()).matchEthSrc(src.mac())
//                            .build();
//            intents.add(AciIPIntent.builder()
//                                .appId(intent.appId())
//                                .selector(selector)
//                                .treatment(intent.treatment())
//                                .path(path)
//                                .constraints(intent.constraints())
//                                .priority(intent.priority())
//                                .build());
//        }
//        return intents;
//    }

}
