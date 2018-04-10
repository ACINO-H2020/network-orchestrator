/*
 * Copyright 2016 Open Networking Laboratory
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.util.GuavaCollectors;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.DeviceId;
import org.onosproject.net.DisjointPath;
import org.onosproject.net.ElementId;
import org.onosproject.net.FilteredConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompilationException;
import org.onosproject.net.intent.IntentException;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentNegotiationException;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.LinkCollectionIntent;
import org.onosproject.net.intent.SDWANProviderOneIntent;
import org.onosproject.net.topology.PathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO.
 */
@Component(immediate = true)
public class SDWANProviderOneIntentCompiler extends ServiceProviderIntentCompiler<SDWANProviderOneIntent> {


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    private List<Key> intentsInNegotiation = Lists.newArrayList();

    private List<ConnectPoint> allowedPorts = Lists.newArrayList();

    @Activate
    public void activate() {

        ConnectPoint j1MininetConnectionPoint = new ConnectPoint(
                DeviceId.deviceId("of:00000000000000AA"),
                PortNumber.portNumber(3));

        ConnectPoint j1ConnectionPoint = new ConnectPoint(
                DeviceId.deviceId("netconf:10.95.86.132:830"),
                PortNumber.portNumber(545));

        allowedPorts.add(j1MininetConnectionPoint);
        allowedPorts.add(j1ConnectionPoint);

        intentManager.registerCompiler(SDWANProviderOneIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterCompiler(SDWANProviderOneIntent.class);
        intentsInNegotiation.clear();
    }

    @Override
    public List<Intent> compile(SDWANProviderOneIntent intent, List<Intent> installable) {

        log.debug("Compilation started");
        List<Intent> intents = new ArrayList<>();
        intents.addAll(compile(intent));
        log.debug("Compilation concluded");
        return intents;
    }

    private List<Path> getPaths(SDWANProviderOneIntent intent,
                                ElementId one, ElementId two) {

        boolean isInstallable;

        if (intentsInNegotiation.contains(intent.key())) {
            intentsInNegotiation.remove(intent.key());
            isInstallable = true;
        } else {
            intentsInNegotiation.add(intent.key());
            isInstallable = false;
        }


        Set<Path> paths = pathService.getPaths(one, two);
        if (paths.isEmpty()) {
            throw new IntentException("Cannot find a path between " + one + " and " + two);
        }

        final List<Constraint> initialConstraints = intent.constraints();

        ImmutableList<Path> filtered = paths.stream()
                .filter(path -> portCheck(path, allowedPorts))
                .filter(path -> checkPath(path, initialConstraints))
                .collect(ImmutableList.toImmutableList());
        if (filtered.isEmpty()) {
            //Try with disjoint path
            Set<DisjointPath> disjointPaths = pathService.getDisjointPaths(one, two);

            ImmutableList<Path> portFilteredPrimary = disjointPaths.stream()
                    .filter(disjointPath -> portCheck(disjointPath.primary(), allowedPorts))
                    .map(DisjointPath::primary)
                    .collect(ImmutableList.toImmutableList());

            ImmutableList<Path> portFilteredBackup = disjointPaths.stream()
                    .filter(disjointPath -> portCheck(disjointPath.backup(), allowedPorts))
                    .map(DisjointPath::backup)
                    .collect(ImmutableList.toImmutableList());

            ImmutableList<Path> constraintFilteredPrimary = portFilteredPrimary.stream()
                    .filter(path -> checkPath(path, initialConstraints))
                    .collect(ImmutableList.toImmutableList());

            ImmutableList<Path> constraintFilteredBackup = portFilteredBackup.stream()
                    .filter(path -> checkPath(path, initialConstraints))
                    .collect(ImmutableList.toImmutableList());

            if (constraintFilteredPrimary.isEmpty() && constraintFilteredBackup.isEmpty()) {

                List<Intent> alternativeIntents = Lists.newArrayList();

                //Primary path
                for (Path path : portFilteredPrimary) {
                    List<Constraint> pathConstraints = supportedPathConstraints(path, initialConstraints);
                    SDWANProviderOneIntent alternativeACIintent = SDWANProviderOneIntent.builder()
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

                //Backup path
                for (Path path : portFilteredBackup) {
                    List<Constraint> pathConstraints = supportedPathConstraints(path, initialConstraints);
                    SDWANProviderOneIntent alternativeACIintent = SDWANProviderOneIntent.builder()
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

            } else if (!isInstallable) {

                List<Intent> alternativeIntents = Lists.newArrayList();
                alternativeIntents.add(intent);
                throw new IntentNegotiationException(intent, alternativeIntents);

            }

            if (constraintFilteredPrimary.isEmpty()) {
                return constraintFilteredBackup;
            } else {
                return constraintFilteredPrimary;
            }

        } else if (!isInstallable) {

            List<Intent> alternativeIntents = Lists.newArrayList();
            alternativeIntents.add(intent);
            throw new IntentNegotiationException(intent, alternativeIntents);

        }
        // TODO: let's be more intelligent about this eventually
        return filtered;
    }


    private List<Intent> compile(SDWANProviderOneIntent intent) {

        Host hostOne = hostService.getHost(intent.one());
        Host hostTwo = hostService.getHost(intent.two());
        if (hostOne == null || hostTwo == null) {
            log.error("Hosts are null");
            throw new IntentCompilationException("Hosts do not exist");
        }

        List<Path> filtered = getPaths(intent, intent.one(), intent.two());

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


    // Creates a path intent from the specified path and original connectivity intent.
    private List<Intent> createLinkIntent(Path path, Host src, Host dst,
                                          SDWANProviderOneIntent intent) {
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

        // TODO: is this still needed? The IP was killing OpenFlow!
        // Made changed here to work with old model
        for (IpAddress ip : dst.ipAddresses()) {
            TrafficSelector selector =
                    DefaultTrafficSelector.builder(intent.selector())
                            .matchEthType(Ethernet.TYPE_IPV4)
                            //.matchEthDst(dst.mac())
                            //.matchEthSrc(src.mac())
                            .matchIPDst(IpPrefix.valueOf(ip, 32))
                            .build();

            // Only links between devices are relevant
            Set<Link> links = path.links()
                    .stream()
                    .filter(link -> link.src().elementId() instanceof DeviceId
                            && link.dst().elementId() instanceof DeviceId)
                    .collect(GuavaCollectors.toImmutableSet());
            // The ingress connection point is the one from a host to a device
            Set<FilteredConnectPoint> ingress = path.links()
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
                    .collect(GuavaCollectors.toImmutableSet());
            // the parent's key is used to identify intents that belong together
            intents.add(LinkCollectionIntent.builder()
                                .key(intent.key())
                                .filteredIngressPoints(ingress)
                                .filteredEgressPoints(egress)
                                .appId(intent.appId())
                                .selector(selector)
                                .treatment(intent.treatment())
                                .links(links)
                                .constraints(intent.constraints())
                                .priority(intent.priority())
                                .build());
        }
        return intents;
    }

}
