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
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.DeviceId;
import org.onosproject.net.ElementId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.AciOpticalIntent;
import org.onosproject.net.intent.AciPathIntent;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.IntentException;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.net.topology.PathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.onosproject.net.DefaultEdgeLink.createEdgeLink;

/**
 * Created by michele on 29/07/16.
 */
@Component(immediate = true)
public class AciOpticalIntentCompiler implements IntentCompiler<AciOpticalIntent> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PathService pathService;

    private Map<ConnectPoint, Set<ConnectPoint>> allowedPorts = new HashMap<ConnectPoint, Set<ConnectPoint>>();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private IntentExtensionService intentManager;

    private static final ProviderId PID =
            new ProviderId("core", "org.onosproject.core", true);

    @Activate
    public void activate() {
        intentManager.registerCompiler(AciOpticalIntent.class, this);
        //BLUE
        // A196 <--> A197
        ConnectPoint a6p2 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.196"),
                PortNumber.portNumber(2));
        ConnectPoint a7p4 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.197"),
                PortNumber.portNumber(4));
        // A196 <--> A198
        ConnectPoint a8p4 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.198"),
                PortNumber.portNumber(208));
        Set<ConnectPoint> j1 = new HashSet<ConnectPoint>();
        j1.add(a7p4);
        j1.add(a8p4);
        allowedPorts.put(a6p2, j1);
        allowedPorts.put(a7p4, Collections.singleton(a6p2));
        allowedPorts.put(a8p4, Collections.singleton(a6p2));
        //ORANGE
        //A196 <--> A197
        ConnectPoint a6p1 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.196"),
                PortNumber.portNumber(1));
        ConnectPoint a7p1 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.197"),
                PortNumber.portNumber(2));
        allowedPorts.put(a6p1, Collections.singleton(a7p1));
        allowedPorts.put(a7p1, Collections.singleton(a6p1));

        // GREEN
        //A198 <--> A197
        ConnectPoint a8p3 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.198"),
                PortNumber.portNumber(3));
        ConnectPoint a7p3 = new ConnectPoint(
                DeviceId.deviceId("restproxy:10.95.86.197"),
                PortNumber.portNumber(3));
        allowedPorts.put(a8p3, Collections.singleton(a7p3));
        allowedPorts.put(a7p3, Collections.singleton(a8p3));


        //TODO: to be removed! only for testing in Mininet
        //ORANGE
        ConnectPoint sd1 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000d"),
                PortNumber.portNumber(1));
        ConnectPoint se1 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000e"),
                PortNumber.portNumber(1));
        allowedPorts.put(sd1, Collections.singleton(se1));
        allowedPorts.put(se1, Collections.singleton(sd1));
        //BLUE
        ConnectPoint sd2 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000d"),
                PortNumber.portNumber(2));
        ConnectPoint se2 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000e"),
                PortNumber.portNumber(2));
        ConnectPoint sf1 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000f"),
                PortNumber.portNumber(1));
        Set<ConnectPoint> sa = new HashSet<ConnectPoint>();
        sa.add(se2);
        sa.add(sf1);
        allowedPorts.put(sd2, sa);
        allowedPorts.put(sd2, Collections.singleton(se2));
        allowedPorts.put(sf1, Collections.singleton(sd2));

        //GREEN
        ConnectPoint sf3 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000f"),
                PortNumber.portNumber(3));
        ConnectPoint se3 = new ConnectPoint(
                DeviceId.deviceId("of:000000000000000e"),
                PortNumber.portNumber(3));
        allowedPorts.put(sf3, Collections.singleton(se3));
        allowedPorts.put(se3, Collections.singleton(sf3));
        log.info("allowedPorts {}", allowedPorts);

    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterCompiler(AciIntent.class);
        allowedPorts.clear();
    }

    @Override
    public List<Intent> compile(AciOpticalIntent intent, List<Intent> installable) {

        List<Link> links = new ArrayList<>();
        Path path = getPath(intent, intent.ingressPoint().deviceId(), intent.egressPoint().deviceId());

        links.add(createEdgeLink(intent.ingressPoint(), true));
        links.addAll(path.links());
        links.add(createEdgeLink(intent.egressPoint(), false));
        return createAciPathIntent(new DefaultPath(PID, links, path.cost(),
                                                   path.annotations()), intent);
    }

    protected Path getPath(ConnectivityIntent intent,
                           ElementId one, ElementId two) {
        //TODO: Intent constraint are ignored!
        Set<Path> paths = pathService.getPaths(one, two);
        final List<Constraint> constraints = intent.constraints();

        ImmutableList<Path> filtered = FluentIterable.from(paths)
                //.filter(path -> checkPath(path, constraints))
                .filter(path -> portCheck(path))
                .toList();
        if (filtered.isEmpty()) {
            throw new IntentException("Path not found: + " + one + " " + two);
        }
        // TODO: let's be more intelligent about this eventually
        return filtered.iterator().next();
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

    // Creates a path intent from the specified path and original connectivity intent.
    private List<Intent> createAciPathIntent(Path path, AciOpticalIntent intent) {

        TrafficSelector selector = DefaultTrafficSelector.builder(intent.selector()).build();
        return Collections.singletonList(AciPathIntent.builder()
                                                 .appId(intent.appId())
                                                 .selector(selector)
                                                 .treatment(intent.treatment())
                                                 .path(path)
                                                 .constraints(intent.constraints())
                                                 .priority(intent.priority())
                                                 .build());
    }


}
