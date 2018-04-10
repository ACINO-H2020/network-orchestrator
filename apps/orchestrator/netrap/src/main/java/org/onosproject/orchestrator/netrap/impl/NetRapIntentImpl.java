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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.wpl.xrapc.Constants;
import com.wpl.xrapc.XrapDeleteReply;
import com.wpl.xrapc.XrapDeleteRequest;
import com.wpl.xrapc.XrapErrorReply;
import com.wpl.xrapc.XrapGetReply;
import com.wpl.xrapc.XrapGetRequest;
import com.wpl.xrapc.XrapPostReply;
import com.wpl.xrapc.XrapPostRequest;
import com.wpl.xrapc.XrapReply;
import com.wpl.xrapc.XrapResource;
import org.apache.commons.codec.binary.Hex;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.util.Bandwidth;
import org.onlab.util.KryoNamespace;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DefaultLink;
import org.onosproject.net.DefaultPath;
import org.onosproject.net.Device;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.PointToPointIntent;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.HighAvailabilityConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;
import org.onosproject.orchestrator.netrap.api.NetRapService;
import org.onosproject.orchestrator.netrap.api.NetRapTransactionService;
import org.onosproject.orchestrator.netrap.model.NetRapAction;
import org.onosproject.orchestrator.netrap.model.NetRapDemand;
import org.onosproject.orchestrator.netrap.model.NetRapLink;
import org.onosproject.orchestrator.netrap.model.NetRapNode;
import org.onosproject.orchestrator.netrap.model.NetRapPlan;
import org.onosproject.orchestrator.netrap.model.NetRapRoute;
import org.onosproject.orchestrator.netrap.model.NetRapSRG;
import org.onosproject.orchestrator.netrap.model.NetRapTopology;
import org.onosproject.orchestrator.netrap.model.PortId;
import org.onosproject.store.serializers.KryoNamespaces;
import org.onosproject.store.service.ConsistentMap;
import org.onosproject.store.service.Serializer;
import org.onosproject.store.service.StorageService;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.onosproject.net.Device.Type.OTN;
import static org.onosproject.net.intent.IntentState.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class NetRapIntentImpl extends XrapResource implements NetRapIntentService {
    private static final Serializer SERIALIZER = Serializer
            .using(new KryoNamespace.Builder().register(KryoNamespaces.API)
                           .register(Key.class)
                           .register(NetRapDemand.class)
                           .register(NetRapLink.class)
                           .register(NetRapNode.class)
                           .register(NetRapPlan.class)
                           .register(NetRapRoute.class)
                           .register(NetRapSRG.class)
                           .register(NetRapTopology.class)
                           .register(PortId.class)
                           .nextId(KryoNamespaces.BEGIN_USER_CUSTOM_ID)
                           .build("OrchestratorStore"));
    private final Logger log = getLogger(NetRapIntentImpl.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected StorageService storageService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetRapTransactionService transactionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    private Map<Key, NetRapRoute> keyNetRapRouteMap;
    private Map<Key, NetRapRoute> keyNetRapBackupRouteMap;
    private LinkedList<Key> handled = new LinkedList<>();
    private ExecutorService batchExecutor;
    // Creates a dependecy loop if resolved using @Reference
    private NetRapService netRapService = null;
    private ApplicationId appId;

    @Activate
    protected void activate() {
        log.info("Starting NetRapIntentService..");
        setRoute("/intents/");
        if (storageService == null) {
            storageService = DefaultServiceDirectory.getService(StorageService.class);
        }

        ConsistentMap<Key, NetRapRoute> keyNetRapRouteConsistentMap = storageService.<Key, NetRapRoute>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("onos-intent-netraproute")
                .withRelaxedReadConsistency()
                .build();

        ConsistentMap<Key, NetRapRoute> keyNetRapBackupRouteConsistentMap = storageService.<Key, NetRapRoute>consistentMapBuilder()
                .withSerializer(SERIALIZER)
                .withName("onos-intent-netrap-backuproute")
                .withRelaxedReadConsistency()
                .build();

        batchExecutor = newSingleThreadExecutor(groupedThreads("netrap/intent", "batch", log));

        keyNetRapRouteMap = keyNetRapRouteConsistentMap.asJavaMap();
        keyNetRapBackupRouteMap = keyNetRapBackupRouteConsistentMap.asJavaMap();
        appId = coreService.registerApplication("org.onosproject.orchestrator.netrap");
        log.info("NetRapIntentService started!");

    }

    private Set<Key> getRouteKeys() {
        return ImmutableSet.copyOf(keyNetRapRouteMap.keySet());
    }

    private Set<Key> getBackupRouteKeys() {
        return ImmutableSet.copyOf(keyNetRapBackupRouteMap.keySet());
    }

    private void setRoute(Key key, NetRapRoute route) {

        keyNetRapRouteMap.put(key, route);
    }

    private NetRapRoute getRoute(Key key) {
        return keyNetRapRouteMap.get(key);
    }

    private boolean isEqualPrimaryRoute(Key key, NetRapRoute newRoute) {

        if (keyNetRapRouteMap.containsKey(key)) {
            NetRapRoute netRapRoute = keyNetRapRouteMap.get(key);
            return netRapRoute.equals(newRoute);
        }
        return false;
    }

    private void setBackupRoute(Key key, NetRapRoute route) {

        keyNetRapBackupRouteMap.put(key, route);
    }

    private boolean isEqualBackupRoute(Key key, NetRapRoute newRoute) {

        if (keyNetRapBackupRouteMap.containsKey(key)) {
            NetRapRoute netRapRoute = keyNetRapBackupRouteMap.get(key);
            return netRapRoute.equals(newRoute);
        }
        return false;
    }

    private NetRapRoute getBackupRoute(Key key) {
        return keyNetRapBackupRouteMap.get(key);
    }


    @Deactivate
    protected void deactivate() {
        batchExecutor.shutdown();
        log.info("Stopping NetRapIntentService...");
    }

    public XrapReply GET(XrapGetRequest request) {

        try {
            List<NetRapDemand> demandList = getDemands();
            Gson gson = new Gson();
            String jsonDmdList = gson.toJson(demandList);
            XrapGetReply rep = new XrapGetReply();
            rep.setContentType("application/json");
            rep.setEtag("*");
            rep.setStatusCode(Constants.OK_200);
            rep.setRequestId(request.getRequestId());
            rep.setDateModified(new Date().getTime());
            rep.setRouteid(request.getRouteid());
            rep.setBody(jsonDmdList.getBytes());
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
    public boolean sendIntents(ByteBuffer address) {
        if (netRapService == null) {
            netRapService = DefaultServiceDirectory.getService(NetRapService.class);
            log.info("Found netrap service:" + netRapService);
        }

        if (netRapService == null) {
            log.error("No NetRapService found!");
            return false;
        }

        try {
            List<NetRapDemand> demandList = getDemands();
            Gson gson = new Gson();
            String jsonDmdList = gson.toJson(demandList);
            byte[] body = jsonDmdList.getBytes();
            XrapPostRequest req = new XrapPostRequest("/demand/list", new String(body));
            req.setRouteid(address);
            log.trace("Posting intents " + req + " to " + Arrays.toString(Hex.encodeHex(address.array())));
            XrapReply response = netRapService.sendOne(address, req);
            if (response == null) {
                log.error("No reply was received from Net2Plan regarding the list of demands.");
            } else {
                if (response instanceof XrapPostReply) {
                    handleIntentResponse(null, (XrapPostReply) response);
                } else {
                    log.error("Response is not a XrapPostReply!");
                }
            }
        } catch (RuntimeException e) {
            log.info("Error building intents: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean sendIntents() {
        return sendIntents(getDemands());
    }

    private boolean sendIntents(List<NetRapDemand> demands) {
        if (netRapService == null) {
            netRapService = DefaultServiceDirectory.getService(NetRapService.class);
            log.info("Found netrap service:" + netRapService);
        }

        if (netRapService == null) {
            log.error("No NetRapService found!");
            return false;
        }

        try {
            Gson gson = new Gson();
            String jsonDmdList = gson.toJson(demands);
            byte[] body = jsonDmdList.getBytes();
            XrapPostRequest req = new XrapPostRequest("/demand/list", new String(body));
            log.trace("Posting demand list " + req + " to ALL");
            XrapReply response = netRapService.sendAny(req);
            if (response == null) {
                log.error("No reply was received from Net2Plan regarding the list of demands.");
            } else {
                if (response instanceof XrapPostReply) {
                    handleIntentResponse(null, (XrapPostReply) response);
                } else {
                    log.error("Response is not a XrapPostReply!");
                }
            }
        } catch (RuntimeException e) {
            log.info("Error building topology: " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean networkReopt() {
        if (netRapService == null) {
            netRapService = DefaultServiceDirectory.getService(NetRapService.class);
            log.info("Found netrap service:" + netRapService);
        }

        if (netRapService == null) {
            log.error("No NetRapService found!");
            return false;
        }

        try {
            XrapPostRequest req = new XrapPostRequest("/demand/reopt", "");
            log.trace("Posting call to reopt " + req + " to ALL");
            XrapReply response = netRapService.sendAny(req);
            if (response == null) {
                log.error("No reply was received from Net2Plan regarding network reoptimization!");
            } else {
                if (response instanceof XrapPostReply) {
                    handleIntentResponse(null, (XrapPostReply) response);
                } else {
                    log.error("Response is not a XrapPostReply!");
                }
            }
        } catch (RuntimeException e) {
            log.info("Error building topology: " + e.getMessage());
            return false;
        }
        return true;
    }

    private Link resolveOptoNetRapLink(NetRapLink netRapLink) {
        String srcNode = netRapLink.getSrc();
        String dstNode = netRapLink.getDst();
        String dstPort = (String) netRapLink.getAttributes().get("dstPort");
        String srcPort = (String) netRapLink.getAttributes().get("srcPort");
        //log.info("Resolving link with src:  " + srcNode + " / " + srcPort + "  and dst " + dstNode + " / " + dstPort);

        if (linkService == null) {
            linkService = DefaultServiceDirectory.getService(LinkService.class);
        }

        if (linkService == null) {
            log.error("Could not resolve linkservice!");
            return null;
        }

  /*    RuleA
          if one normal and one weird:
          keep, but replace the weird dstPort

        RuleB
          if both weird and both have the same deviceId:
          drop the link

        RuleC
          if both weird and both DON NOT have the same deviceId:
          keep, but replace the weird src- and dstPort
*/

        /* Apply ruleA and Rule C */
        if (srcNode.contains("/")) {
            String newSrcNode = srcNode.substring(0, srcNode.indexOf("/"));
            srcPort = srcNode.substring(srcNode.indexOf("/") + 1, srcNode.length());
            srcNode = newSrcNode;

        }
        if (dstNode.contains("/")) {
            String newDstNode = dstNode.substring(0, dstNode.indexOf("/"));
            dstPort = dstNode.substring(dstNode.indexOf("/") + 1, dstNode.length());
            dstNode = newDstNode;

        }
        /* Apply rule B */
        if (dstNode.equals(srcNode)) {
            return null;
        }

        ConnectPoint srcCP = ConnectPoint.deviceConnectPoint(srcNode + "/" + srcPort);
        ConnectPoint dstCP = ConnectPoint.deviceConnectPoint(dstNode + "/" + dstPort);
        if (linkService == null) {
            linkService = DefaultServiceDirectory.getService(LinkService.class);
        }

        if (linkService == null) {
            log.error("Could not find linkService!");
            return null;
        }

        Link link = linkService.getLink(srcCP, dstCP);
        if (link == null) {
            link = linkService.getLink(dstCP, srcCP);
        }

        if (link == null) {
            log.error("Could not find link between " + srcCP.toString() + " and " + dstCP.toString());
        }

        return link;
    }

    private Link resolveIPNetRapLink(NetRapLink netRapLink) {
        String srcNode = netRapLink.getSrc();
        String srcPort = (String) netRapLink.getAttributes().get("srcPort");

        String dstNode = netRapLink.getDst();
        String dstPort = (String) netRapLink.getAttributes().get("dstPort");

        /* apply ruleA - if both nodes are weirdos, drop the link */
        if (dstPort == null && srcPort == null) {
            log.info("Found weird link, dropping it!");
            return null;
        }
        //log.info("Resolving link with src:  " + srcNode + " / " + srcPort + "  and dst " + dstNode + " / " + dstPort);
        ConnectPoint srcCP = ConnectPoint.deviceConnectPoint(srcNode + "/" + srcPort);
        ConnectPoint dstCP = ConnectPoint.deviceConnectPoint(dstNode + "/" + dstPort);
        if (linkService == null) {
            linkService = DefaultServiceDirectory.getService(LinkService.class);
        }

        if (linkService == null) {
            log.error("Could not find linkService!");
            return null;
        }

        Link link = linkService.getLink(srcCP, dstCP);
        if (link == null) {
            link = linkService.getLink(dstCP, srcCP);
        }

        if (link == null) {
            log.error("Could not find link between " + srcCP.toString() + " and " + dstCP.toString());
        }
        return link;
    }

    public void testTranslation() {
        if (intentService == null) {
            intentService = DefaultServiceDirectory.getService(IntentService.class);
        }
        Iterable<Intent> intentList = intentService.getIntents();
        Gson gson = new Gson();
        log.info("getRouteKeys() returned " + getRouteKeys());

    }


    private void handleIntentResponse(ACIPPIntent intent, XrapPostReply response) {
        Gson gson = new Gson();
        NetRapAction[] nrdArray = gson.fromJson(new String(response.getBody()), NetRapAction[].class);
        if (nrdArray != null) {
            List<NetRapAction> netRapDemandList = new ArrayList<>(Arrays.asList(nrdArray));
//            log.info("handleIntentResponse - Got List of netRapDemands back:  " + netRapDemandList);

            /* NetRapActions
            priority 1) NEW -> Newly created link, handle first
            priority 2) MOVE -> Change the path of an existing link
            priority 2) UPDATE -> - || -
            priority 3) ROUTE -> Existing intent has gotten a path
            priority 4) FAIL -> Net2Plan could not find a route
            */

            for (NetRapAction nrd : netRapDemandList) {
                if (nrd.getAction().equals("NEW")) {
                    handleNewNetRapAction(nrd, intent);
                }
            }
            for (NetRapAction nrd : netRapDemandList) {
                if (nrd.getAction().equals("MOVE")) {
                    handleMoveNetRapAction(nrd, intent);
                }
            }
            for (NetRapAction nrd : netRapDemandList) {
                if (nrd.getAction().equals("ROUTE")) {
                    handleRouteNetRapAction(nrd, intent);
                }
            }
            for (NetRapAction nrd : netRapDemandList) {
                if (nrd.getAction().equals("FAIL")) {
                    handleFailNetRapAction(nrd, intent);
                }
            }
        } else {
            log.error("Could not parse NetRapDemand from response!");
            //return;
        }

        //executeIntentTransaction();
    }

    private void addPathSubmitIntent(ACIPPIntent intent, Path path) {
        ACIPPIntent newIntent = ACIPPIntent.builder()
                .appId(intent.appId())
                .key(intent.key())
                .calculated(true)
                .constraints(intent.constraints())
                .priority(intent.priority())
                .selector(intent.selector())
                .treatment(intent.treatment())
                .src(intent.src())
                .dst(intent.dst())
                .path(path)
                .build();
        log.error("SUBMITTING INTENT key=" + newIntent.key() + " src=" + newIntent.src() +
                          " dst=" + newIntent.dst());
        intentService.submit(newIntent);
    }


    private String netRapRouteString(NetRapRoute netRapRoute) {
        StringBuilder sb = new StringBuilder();
        for (NetRapLink l : netRapRoute.getLinks()) {
            sb.append("[src=" + l.getSrc() + "/" + l.getAttributes().get("srcPort"));
            sb.append(", dst=" + l.getDst() + "/" + l.getAttributes().get("dstPort") + "],");
        }
        return sb.toString();
    }

    private void handleNewNetRapAction(NetRapAction newAction, Intent refintent) {
        NetRapDemand netRapDemand = newAction.getDemand();

        NetRapRoute route = netRapDemand.getRoute();
        int priority = 100;
        if (refintent != null) {
            priority = refintent.priority();
        }

        List<Link> linkList;
        if (route.getLayer() != 0) {
            log.error("handleNewNetRapAction got a route that is not on the WDM layer!");
        }
        linkList = resolveOptoRoute(route);
        DefaultPath newPath = new DefaultPath(ProviderId.NONE, linkList, 1.0);
        List<Constraint> constraints = new ArrayList<>();
        if (netRapDemand.getOfferedTraffic() != null) {
            BandwidthConstraint bwConst = new BandwidthConstraint(Bandwidth.gbps(netRapDemand.getOfferedTraffic()));
            constraints.add(bwConst);
        }

        // TODO: Pontus: oct 11
        // Ugly hack to set the correct source/dst port
        // TAPI expects the src/dst to point to the copper interfaces of the transponders
        // This happens to be always 1 in our case
        ConnectPoint srcCP = new ConnectPoint(newPath.src().deviceId(), PortNumber.portNumber(1));
        ConnectPoint dstCP = new ConnectPoint(newPath.dst().deviceId(), PortNumber.portNumber(1));


        ACIPPIntent newIntent = ACIPPIntent.builder()
                .appId(this.appId)
                .calculated(true)
                .priority(priority)
                .selector(DefaultTrafficSelector.emptySelector())
                .treatment(DefaultTrafficTreatment.emptyTreatment())
                .src(srcCP)
                .dst(dstCP) // Pontus: oct 11, src/dst was swapped here, intentionally?
                .path(newPath)
                .constraints(constraints)
                .build();
        setRoute(newIntent.key(), route);
        transactionService.addOpticalIntent(newIntent);
    }

    /* TODO: update this to do what should be done! */
    private void handleMoveNetRapAction(NetRapAction netRapAction, Intent refintent) {
        log.error("handleMoveNetRapAction - called, executing ROUTE action");
        HashMap<String, String> attributes = netRapAction.getDemand().getAttributes();

        String appName = attributes.get("appname");
        String appId = attributes.get("appid");
        String intentId = attributes.get("id");
        String intentKey = attributes.get("key");

        if (intentKey != null && appId != null) {
            DefaultApplicationId searchAppId = new DefaultApplicationId(Integer.parseInt(appId), appName);
            Key key;
            if (intentKey.startsWith("0x")) {
                 key = Key.of(Long.decode(intentKey), searchAppId);
            } else {
                key = Key.of(intentKey, searchAppId);
            }
            log.info("handleMoveNetRapAction, looking up intent with key: " + key);
            Intent oldIntent = intentService.getIntent(key);
            if (oldIntent != null) {
                handleRouteNetRapAction(netRapAction, refintent);
            } else {
                log.error("Failed to handle MOVE action, could not find existing intent!");
            }
        } else {
            log.error("Failed to handle MOVE action, intentKey or appId is NULL!");
        }

    }

    private void handleFailNetRapAction(NetRapAction netRapAction, Intent refintent) {

        NetRapDemand netRapDemand = netRapAction.getDemand();
        String appname = (String) netRapDemand.getAttributes().get("appname");
        String appid = (String) netRapDemand.getAttributes().get("appid");
        String id = (String) netRapDemand.getAttributes().get("id");
        String intentStringKey = (String) netRapDemand.getAttributes().get("key");
        DefaultApplicationId searchAppId = null;
        ACIPPIntent intent = null;
        if (intentStringKey != null && appid != null) {
            searchAppId = new DefaultApplicationId(Integer.parseInt(appid), appname);

            log.error("NetRap FAIL action received for intent {}", intentStringKey);

            Key intentKey;
            if (intentStringKey.startsWith("0x")) {
                intentKey = Key.of(Long.decode(intentStringKey), searchAppId);
            } else {
                intentKey = Key.of(intentStringKey, searchAppId);
            }

            handled.remove(intentKey);

            //log.info("handleIntentResponse, looking up intent with key: " + intentKey);
            Intent lookupIntent = intentService.getIntent(intentKey);
            //log.info("Found intent " + lookupIntent);

            if (lookupIntent instanceof ACIPPIntent) {
                intent = (ACIPPIntent) lookupIntent;
            } else {
                log.info("However, it is not a ACIPPIntent!");
            }

        }
        if (intent == null) {
            log.error("handleIntentResponse, could not find ACIPPIntent for key: " + intentStringKey + " appid: " + appid + " appname: " + appname + " id: " + id + " !");
            if (refintent != null) {
                return;
            }
        }
        // TODO better error handling here ..
        if (!intent.appId().name().equals(appname)) {
            log.error("Intent appname doesnt match the returned value!");
            log.error(intent.appId().name());
            log.error(appname);
        }
        if (!Short.toString(intent.appId().id()).equals(appid)) {
            log.error("Intent appid doesnt match returned value!");
            log.error(Short.toString(intent.appId().id()));
            log.error(appid);
        }
        if (!intent.id().toString().equals(id)) {
            log.error("Intent id doesnt matched returned value!");
            log.error(intent.id().toString());
            log.error(id);
        }
        if (!intent.key().toString().equals(intentStringKey)) {
            log.error("Intent key doesnt match returned value!");
            log.error(intent.key().toString());
            log.error(intentStringKey);
        }
    }

    private DefaultPath extractPath(NetRapRoute netRapRoute, List<ExpectedLink> expectedLinks) {
        ArrayList<Link> resolvedLinkList = new ArrayList<>();

        for (NetRapLink netRapLink : netRapRoute.getLinks()) {
            Link link = resolveIPNetRapLink(netRapLink);
            if (link != null) {
                resolvedLinkList.add(link);
            }
        }

        /*
         * Replace the holes created by removing any /null /null devices
         * apply ruleB - iterate through the list and
         * if a.dst devId != b.src devId:
         * replace a.dst with b.dst
         * drop link b  -> put in attributes
         */

        ArrayList<Link> mainpathlist = new ArrayList<>();

        Link srcLink = null;
        for (Link dstLink : resolvedLinkList) {
            log.debug("srcLink: " + srcLink + " dstLink: " + dstLink);
            // first iteration, srcLink is null
            // add dstLink to dstLink
            // set srcLink to dstLink
            if (srcLink == null) {
                mainpathlist.add(dstLink);
                srcLink = dstLink;
                continue;
            }
            // Normal case
            // srcLink dst == dstLink src
            // add dstLink to linkList
            // set srcLink to dstLink
            if (srcLink.dst().deviceId().equals(dstLink.src().deviceId())) {
                mainpathlist.add(dstLink);
                srcLink = dstLink;
            } else {
                // Forwarding Adjecency link case
                // srcLink.dst != dstLink.src
                // 1) look for a link between srcLink.dst and dstLink.src
                // 2) if found:
                //    add foundLink to linkList
                //    add dstLink to linklist
                //    srcLink == dstLink
                // 3) if not found:
                //    create newLink and add to linklist and expectedLinks
                //    add dstLink to linklist
                //    set srcLink to dstLink
                // This creates a link between transponders

                ConnectPoint srcConnectPoint = srcLink.src();
                ConnectPoint dstConnectPoint = dstLink.dst();

                Link foundLink = linkService.getLink(srcConnectPoint, dstConnectPoint);
                if (foundLink != null) {
                    log.debug("Found a link to replace the weird one {}", foundLink);
                    mainpathlist.remove(mainpathlist.size() - 1);
                    mainpathlist.add(foundLink);
                    srcLink = foundLink;
                    continue;
                }

                // TODO: How should this look like?
                // no link found, create one
                DefaultAnnotations annotations = DefaultAnnotations.builder().set("netRap", "ignore").build();
                ExpectedLink newLink = new ExpectedLink(srcConnectPoint, dstConnectPoint, annotations);
                expectedLinks.add(newLink);

                DefaultLink dlink = DefaultLink.builder()
                        .annotations(annotations)
                        .dst(dstConnectPoint)
                        .src(srcConnectPoint)
                        .state(Link.State.INACTIVE)
                        .type(Link.Type.DIRECT)
                        .providerId(ProviderId.NONE)
                        .isExpected(true)
                        .build();

                // remove the last one
                log.debug("Removing link:" + mainpathlist.get(mainpathlist.size() - 1));
                mainpathlist.remove(mainpathlist.size() - 1);
                mainpathlist.add(dlink);

                srcLink = dlink;
            }

        }
        log.debug("Expected links = " + expectedLinks);
        log.debug("Discovered path = " + mainpathlist);
        //log.info("TRANSLATED IP ROUTE  " + netRapRouteString(netRapRoute));
        //log.info("TO PATH: " + linkList.toString());
        DefaultPath newPath = new DefaultPath(ProviderId.NONE, mainpathlist, 1.0);
        return newPath;
    }


    private synchronized void handleRouteNetRapAction(NetRapAction netRapAction, Intent refintent) {
        //log.info("handleRouteNetRapAction called");
        NetRapDemand netRapDemand = netRapAction.getDemand();
        String appname = (String) netRapDemand.getAttributes().get("appname");
        String appid = (String) netRapDemand.getAttributes().get("appid");
        String id = (String) netRapDemand.getAttributes().get("id");
        String intentStringKey = (String) netRapDemand.getAttributes().get("key");
        DefaultApplicationId searchAppId;
        ACIPPIntent intent = null;
        ArrayList<ExpectedLink> expectedLinks = new ArrayList<>();
        if (intentStringKey != null && appid != null) {
            searchAppId = new DefaultApplicationId(Integer.parseInt(appid), appname);

            Key intentKey;
            Intent lookupIntent = null;
            try {

                if (intentStringKey.startsWith("0x")) {
                    intentKey = Key.of(Long.decode(intentStringKey), searchAppId);
                } else {
                    intentKey = Key.of(intentStringKey, searchAppId);
                }

                log.info("handleIntentResponse, looking up intent with key: " + intentKey);
                lookupIntent = intentService.getIntent(intentKey);

            } catch (NumberFormatException e) {
                //intent key is bigger a long
                for (Intent intentToCheck : intentService.getIntents()) {
                    Key intentKeyToCheck = intentToCheck.key();
                    if (intentKeyToCheck.toString().equals(intentStringKey)) {
                        lookupIntent = intentToCheck;
                        break;
                    }
                }
            }

            if (lookupIntent == null) {
                return;
            }


            log.debug("Found intent " + lookupIntent);
            if (lookupIntent instanceof ACIPPIntent) {
                intent = (ACIPPIntent) lookupIntent;
            } else {
                log.info("However, it is not a ACIPPIntent!");
            }

        }
        if (intent == null) {
            log.error("handleIntentResponse, could not find ACIPPIntent for key: " + intentStringKey + " appid: " + appid + " appname: " + appname + " id: " + id + " !");
            return;
        }
        // TODO better error handling here ..
        if (!intent.appId().name().equals(appname)) {
            log.error("Intent appname doesnt match the returned value!");
            log.error(intent.appId().name());
            log.error(appname);
        }
        if (!Short.toString(intent.appId().id()).equals(appid)) {
            log.error("Intent appid doesnt match returned value!");
            log.error(Short.toString(intent.appId().id()));
            log.error(appid);
        }
        if (!intent.id().toString().equals(id)) {
            log.error("Intent id doesnt matched returned value!");
            log.error(intent.id().toString());
            log.error(id);
        }
        if (!intent.key().toString().equals(intentStringKey)) {
            log.error("Intent key doesnt match returned value!");
            log.error(intent.key().toString());
            log.error(intentStringKey);
        }
        //log.info("This intent has Net2Plan demandId: " + netRapDemand.getIdentifier());
        //log.info("This intent has Net2Plan routeId: " + netRapDemand.getRouteId());
        if (netRapDemand.getRoute() == null) {
            log.error("Could not get routes from Action: " + netRapAction.toString());
            return;
        }

        NetRapRoute netRapRoute = netRapDemand.getRoute();
        NetRapRoute netRapBackupRoute = netRapDemand.getBackupRoute();

        if (netRapRoute.getLayer() != 1) {
            log.error("ROUTE ON UNKNOWN LAYER!");
            return;
        }

        if (netRapBackupRoute != null) {
            if (isEqualPrimaryRoute(intent.key(), netRapRoute) && isEqualBackupRoute(intent.key(), netRapBackupRoute)) {
                log.debug("Same PRIMARY and BACKUP routes received from Net2Plan for intent {}, skipping...", intent.key());
                return;
            }
        } else {
            if (isEqualPrimaryRoute(intent.key(), netRapRoute)) {
                log.debug("Same PRIMARY route received from Net2Plan for intent {}, skipping...", intent.key());
                return;
            }
        }

        setRoute(intent.key(), netRapRoute);
        DefaultPath newPath = extractPath(netRapRoute, expectedLinks);
        DefaultPath newBackupPath = null;
        if (netRapBackupRoute != null) {
            log.trace("Extracting backup path!");
            newBackupPath = extractPath(netRapBackupRoute, expectedLinks);
            setBackupRoute(intent.key(), netRapBackupRoute);
        } else {
            log.trace("NO BACKUP PATH!");
        }

        //Update the intent endpoints with the real ones calculated by net2plan, required by emulatedip app
        List<Link> pathLinks = newPath.links();
        ConnectPoint src = pathLinks.get(0).src();
        ConnectPoint dst = pathLinks.get(pathLinks.size() - 1).dst();

        ACIPPIntent newIntent = ACIPPIntent.builder()
                .appId(intent.appId())
                .key(intent.key())
                .calculated(true)
                .constraints(intent.constraints())
                .priority(intent.priority())
                .selector(intent.selector())
                .treatment(intent.treatment())
                .src(src)
                .dst(dst)
                .path(newPath)
                .backupPath(newBackupPath)
                .build();

        log.info("Created new intent for an IP link");
        log.debug("Number of expected links: " + expectedLinks.size());
        transactionService.addRouteIntent(newIntent, expectedLinks);
    }

    private LinkDescription description(Link link, String key, String value) {
        DefaultAnnotations.Builder builder = DefaultAnnotations.builder();
        if (value != null) {
            builder.set(key, value);
        } else {
            builder.remove(key);
        }
        return new DefaultLinkDescription(link.src(), link.dst(), link.type(), link.isExpected(), builder.build());
    }

    private List<Link> resolveOptoRoute(NetRapRoute route) {
        ArrayList<Link> linkList = new ArrayList<>();
        for (NetRapLink netRapLink : route.getLinks()) {
            Link link = resolveOptoNetRapLink(netRapLink);
            if (link != null) {
                linkList.add(link);
            }
        }
        return linkList;
    }

    private boolean handledIntent(ACIPPIntent intent) {

        if (handled.contains(intent.key())) {
            return true;
        } else {
            handled.add(intent.key());
            return false;
        }
    }

    @Override
    public void updateIntents(Intent intent) {
        if (netRapService == null) {
            netRapService = DefaultServiceDirectory.getService(NetRapService.class);
        }


        if (intent instanceof ACIPPIntent) {

            ACIPPIntent aciIntent = (ACIPPIntent) intent;

            if (!handledIntent(aciIntent)) {
                batchExecutor.execute(new Net2PlanRequestHandler(aciIntent, false));
            }

        }
    }

    private class Net2PlanRequestHandler implements Runnable {

        ACIPPIntent intentToProcess;
        boolean removeIntent;

        public Net2PlanRequestHandler(ACIPPIntent intentToProcess, boolean removeIntent) {
            this.intentToProcess = intentToProcess;
            this.removeIntent = removeIntent;
        }

        @Override
        public void run() {

            XrapReply response = null;

            if (removeIntent) {
                String intentKey = intentToProcess.key().toString();
                //Remove the intent from the queue
                transactionService.removeRouteIntent(intentToProcess.key());

                XrapDeleteRequest req = new XrapDeleteRequest("/demand/" + intentKey);

                log.info("DELETING intent with key=" + intentKey);
                response = netRapService.sendAny(req);

            } else {
                NetRapDemand netRapDemand = aciPathIntentToDemand(intentToProcess);
                if (intentToProcess.appId().equals(appId)) {
                    transactionService.notifyInstalledOpticalIntent(intentToProcess.key());
                    log.info("Sending to N2P the optical intent {} information", intentToProcess.key());
                    NetRapDemand revnetRapDemand = aciPathIntentToRevDemand(intentToProcess);
                    List<NetRapDemand> demands = Lists.newArrayList();
                    demands.add(netRapDemand);
                    demands.add(revnetRapDemand);
                    sendIntents(demands);
                } else {
                    Gson gson = new Gson();
                    String jsonDmdList = gson.toJson(netRapDemand);
                    byte[] body = jsonDmdList.getBytes();
                    XrapPostRequest req = new XrapPostRequest("/demand", new String(body));
                    //log.info("Posting demand " + req + " to ALL");
                    log.debug("SENDING intent key " + intentToProcess.key()
                                      + " src=" + intentToProcess.src() + " dst=" + intentToProcess.dst() + " TO N2P, "
                                      + System.currentTimeMillis());
                    response = netRapService.sendAny(req);
                }
            }

            if (response == null) {
                log.error("No reply was received from Net2Plan regarding intent {}", intentToProcess.key());
                handled.remove(intentToProcess.key());
            } else {
                //log.info("On demand req,  got reply: " + response.toString());
                if (response instanceof XrapPostReply) {
                    log.debug("Handling the response for intent {}, {}", intentToProcess.key(), +System.currentTimeMillis());
                    handleIntentResponse(intentToProcess, (XrapPostReply) response);
                } else if (response instanceof XrapDeleteReply) {
                    log.info("Removed N2P demand: " + response.toString());
                    keyNetRapRouteMap.remove(intentToProcess.key());
                    keyNetRapBackupRouteMap.remove(intentToProcess.key());
                } else {
                    log.error("Response is not a XrapPostReply/Remove reply!");
                    if (!removeIntent) {
                        handled.remove(intentToProcess.key());
                    }
                }
            }
        }
    }

    @Override
    public void deleteIntent(Intent intent) {
        if (netRapService == null) {
            netRapService = DefaultServiceDirectory.getService(NetRapService.class);
        }

        if (intent instanceof ACIPPIntent) {
            batchExecutor.execute(new Net2PlanRequestHandler((ACIPPIntent) intent, true));
        }
    }

    private NetRapDemand aciPathIntentToDemand(ACIPPIntent ACIPPIntent) {
        NetRapDemand netRapDemand = new NetRapDemand();
        PortId dstPort = new PortId();
        PortId srcPort = new PortId();

        dstPort.setDevice(ACIPPIntent.dst().deviceId().toString());
        dstPort.setPort(ACIPPIntent.dst().port().toString());
        srcPort.setDevice(ACIPPIntent.src().deviceId().toString());
        srcPort.setPort(ACIPPIntent.src().port().toString());
        netRapDemand.setEgressNode(dstPort);
        netRapDemand.setIngressNode(srcPort);
        netRapDemand.setOfferedTraffic(0.0001);

        NetRapRoute route = getRoute(ACIPPIntent.key());
        if (route != null) {
            netRapDemand.setRoute(route);
        } else {
            netRapDemand.setRoute(null);
        }

        NetRapRoute backupRoute = getBackupRoute(ACIPPIntent.key());
        if (route != null) {
            netRapDemand.setBackupRoute(backupRoute);
        } else {
            netRapDemand.setBackupRoute(null);
        }

        HashMap<String, String> attributes = new HashMap<>();
        // Set a default maximum latency, 10 seconds
        attributes.put("maxLatencyInMs", "10000");
        // Set a default minimum availability, anything goes
        attributes.put("minAvailability", "0.001");
        // Set a default exclusivity, share links
        attributes.put("wdmClass", "0");
        // To be able to identify the intent in the other direction
        attributes.put("appname", ACIPPIntent.appId().name());
        attributes.put("appid", Short.toString(ACIPPIntent.appId().id()));
        attributes.put("key", ACIPPIntent.key().toString());
        attributes.put("id", ACIPPIntent.id().toString());

        for (Constraint c : ACIPPIntent.constraints()) {
            if (c instanceof BandwidthConstraint) {
                // Convert to gigabits per second
                netRapDemand.setOfferedTraffic(((BandwidthConstraint) c).bandwidth().bps() / 1e9);
            } else if (c instanceof LatencyConstraint) {
                attributes.replace("maxLatencyInMs", Long.toString(((LatencyConstraint) c).latency().toMillis()) + "");
            } else if (c instanceof HighAvailabilityConstraint) {
                attributes.put("Protection", "true");
            }

        }
        netRapDemand.setAttributes(attributes);
        return netRapDemand;
    }

    private NetRapRoute reverseNetRapRoute(NetRapRoute route) {
        Integer layer = route.getLayer();
        List<NetRapLink> links = com.google.common.collect.Lists.reverse(route.getLinks());
        List<NetRapNode> nodes = com.google.common.collect.Lists.reverse(route.getNodes());
        HashMap attributes = route.getAttributes();
        String demandId = route.getDemandId();
        Double occupidesCapacity = route.getOccupiedCapacity();
        NetRapRoute netRapRoute = new NetRapRoute();

        netRapRoute.setLayer(layer);
        netRapRoute.setLinks(links);
        netRapRoute.setNodes(nodes);
        netRapRoute.setAttributes(attributes);
        netRapRoute.setDemandId(demandId);
        netRapRoute.setOccupiedCapacity(occupidesCapacity);
        return netRapRoute;
    }

    private NetRapDemand aciPathIntentToRevDemand(ACIPPIntent ACIPPIntent) {
        NetRapDemand netRapDemand = new NetRapDemand();
        PortId dstPort = new PortId();
        PortId srcPort = new PortId();

        dstPort.setDevice(ACIPPIntent.dst().deviceId().toString());
        dstPort.setPort(ACIPPIntent.dst().port().toString());
        srcPort.setDevice(ACIPPIntent.src().deviceId().toString());
        srcPort.setPort(ACIPPIntent.src().port().toString());
        netRapDemand.setEgressNode(srcPort);
        netRapDemand.setIngressNode(dstPort);
        netRapDemand.setOfferedTraffic(0.0001);

        NetRapRoute route = getRoute(ACIPPIntent.key());
        if (route != null) {
            NetRapRoute revRoute = reverseNetRapRoute(route);
            netRapDemand.setRoute(revRoute);
        } else {
            netRapDemand.setRoute(null);
        }

        NetRapRoute backupRoute = getBackupRoute(ACIPPIntent.key());
        if (backupRoute != null) {
            NetRapRoute revBackupRoute = reverseNetRapRoute(backupRoute);
            netRapDemand.setBackupRoute(revBackupRoute);
        } else {
            netRapDemand.setBackupRoute(null);
        }


        HashMap<String, String> attributes = new HashMap<>();
        // Set a default maximum latency, 10 seconds
        attributes.put("maxLatencyInMs", "10000");
        // Set a default minimum availability, anything goes
        attributes.put("minAvailability", "0.001");
        // Set a default exclusivity, share links
        attributes.put("wdmClass", "0");
        // To be able to identify the intent in the other direction
        attributes.put("appname", ACIPPIntent.appId().name());
        attributes.put("appid", Short.toString(ACIPPIntent.appId().id()));
        attributes.put("key", ACIPPIntent.key().toString());
        attributes.put("id", ACIPPIntent.id().toString());

        for (Constraint c : ACIPPIntent.constraints()) {
            if (c instanceof BandwidthConstraint) {
                // Convert to gigabits per second
                netRapDemand.setOfferedTraffic(((BandwidthConstraint) c).bandwidth().bps() / 1e9);
            } else if (c instanceof LatencyConstraint) {
                attributes.replace("maxLatencyInMs", Long.toString(((LatencyConstraint) c).latency().toMillis()) + "");
            } else if (c instanceof HighAvailabilityConstraint) {
                attributes.put("Protection", "true");
            }
        }
        netRapDemand.setAttributes(attributes);

        return netRapDemand;
    }

    NetRapDemand ptpIntentToDemand(PointToPointIntent p2pintent) {

        NetRapDemand netRapDemand = new NetRapDemand();
        PortId dstPort = new PortId();
        PortId srcPort = new PortId();
        dstPort.setDevice(p2pintent.egressPoint().deviceId().toString());
        dstPort.setPort(p2pintent.egressPoint().port().toString());
        srcPort.setDevice(p2pintent.ingressPoint().deviceId().toString());
        srcPort.setPort(p2pintent.ingressPoint().port().toString());

        netRapDemand.setEgressNode(dstPort);
        netRapDemand.setIngressNode(srcPort);
        netRapDemand.setOfferedTraffic(0.0001);
        HashMap<String, String> attributes = new HashMap<>();
        // Set a default maximum latency, 10 seconds
        attributes.put("maxLatencyInMs", "10000");
        // Set a default minimum availability, anything goes
        attributes.put("minAvailability", "0.001");
        // Set a default exclusivity, share links
        attributes.put("wdmClass", "0");
        // To be able to identify the intent in the other direction
        attributes.put("appname", p2pintent.appId().name());
        attributes.put("appid", Short.toString(p2pintent.appId().id()));
        attributes.put("key", p2pintent.key().toString());
        attributes.put("id", p2pintent.id().toString());

        for (Constraint c : p2pintent.constraints()) {
            log.info("Found intent contstraint: " + c.getClass());
            if (c instanceof BandwidthConstraint) {
                netRapDemand.setOfferedTraffic(((BandwidthConstraint) c).bandwidth().bps());
            } else if (c instanceof LatencyConstraint) {
                attributes.replace("maxLatencyInMs", Long.toString(((LatencyConstraint) c).latency().toMillis()) + "");
            }
        }
        log.debug("Demand: " + netRapDemand.toString());
        netRapDemand.setAttributes(attributes);
        return netRapDemand;
    }

    @Override
    public List<NetRapDemand> getDemands() throws RuntimeException {
        String errormsg = null;

        buildIntents:
        {
            List<NetRapDemand> demands = new ArrayList<>();
            Iterable<Intent> intents = intentService.getIntents();

            if (intents == null) {
                errormsg = "unable to find intents";
                break buildIntents;
            }

            for (Intent intent : intents) {
                if (intent instanceof ACIPPIntent) {
                    IntentState state = intentService.getIntentState(intent.key());
                    // If the intent is being withdrawn or similar, don't send it
                    if (state == WITHDRAW_REQ ||
                            state == WITHDRAWN ||
                            state == WITHDRAWING ||
                            state == CORRUPT ||
                            state == PURGE_REQ) {
                        continue;
                    }

                    NetRapDemand netRapDemand;
                    ACIPPIntent acippIntent = (ACIPPIntent) intent;

                    if (state == FAILED && acippIntent.calculated()) {

                        log.debug("Adding FAILED intent {} to N2P", acippIntent.key());

                        keyNetRapRouteMap.remove(acippIntent.key());
                        keyNetRapBackupRouteMap.remove(acippIntent.key());

                        ACIPPIntent newIntent = ACIPPIntent.builder()
                                .appId(acippIntent.appId())
                                .key(acippIntent.key())
                                .calculated(false)
                                .constraints(acippIntent.constraints())
                                .priority(intent.priority())
                                .selector(acippIntent.selector())
                                .treatment(acippIntent.treatment())
                                .src(acippIntent.src())
                                .dst(acippIntent.dst())
                                .path(null)
                                .backupPath(null)
                                .build();
                        netRapDemand = aciPathIntentToDemand(newIntent);
                        demands.add(netRapDemand);
                    } else {

                        //if(!isFailedClientPort(failedConnectPoints, acippIntent)) {
                        log.debug("Adding intent {} to N2P", acippIntent.key());
                        netRapDemand = aciPathIntentToDemand(acippIntent);
                        demands.add(netRapDemand);
                        // If intent was created by the netrap app, it's gonna be an optical intent
                        // therefore, make a reverse copy of it and send to net2plan
                        if (intent.appId().equals(this.appId)) {
                            log.debug("Optical ACIPPIntent discovered, duplicating and reversing!");
                            NetRapDemand revnetRapDemand = aciPathIntentToRevDemand((ACIPPIntent) intent);
                            demands.add(revnetRapDemand);
                        }
                    }
                    /*} else {
                        intentService.withdraw(acippIntent);
                    }*/
                }
            }
            return demands;
        }
        throw new RuntimeException(errormsg);
    }

    /**
     * Check if the optical intent should be sent back to Net2Plan
     *
     * @param connectPoints a list of failed IP connection points
     * @param intent        the intent to check
     * @return a boolean
     */
    private boolean isFailedClientPort(List<ConnectPoint> connectPoints, ACIPPIntent intent) {

        //TODO: Check if this works with the real testbed failures
        Device srcDev = deviceService.getDevice(intent.src().deviceId());
        Device dstDev = deviceService.getDevice(intent.dst().deviceId());

        if (srcDev.type() == OTN && dstDev.type() == OTN) {

            return (linkService.getDeviceEgressLinks(intent.src().deviceId()).stream()
                    .anyMatch(dstlink -> connectPoints.contains(dstlink.dst())) &&
                    linkService.getDeviceEgressLinks(intent.dst().deviceId()).stream()
                            .anyMatch(dstlink -> connectPoints.contains(dstlink.dst())));
        } else {
            return false;
        }

    }
}

