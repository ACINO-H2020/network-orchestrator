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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wpl.xrapc.Constants;
import com.wpl.xrapc.XrapErrorReply;
import com.wpl.xrapc.XrapGetReply;
import com.wpl.xrapc.XrapGetRequest;
import com.wpl.xrapc.XrapMessage;
import com.wpl.xrapc.XrapPostReply;
import com.wpl.xrapc.XrapPostRequest;
import com.wpl.xrapc.XrapReply;
import com.wpl.xrapc.XrapResource;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;
import org.onosproject.orchestrator.netrap.api.NetRapRegistryService;
import org.onosproject.orchestrator.netrap.api.NetRapService;
import org.onosproject.orchestrator.netrap.api.NetRapTopoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;

@Component
@Service
public class NetRapRegistry extends XrapResource implements NetRapRegistryService {
    private static Pattern GET_PATTERN = Pattern.compile("/test/(.+)");
    private final String route = "/register/";
    private Logger log;
    private final ExecutorService greeter =
            newSingleThreadExecutor(groupedThreads("onos/orchestrator", "netrap-handler", log));

    private HashMap<String, RegEntry> registry = null;
    private NetRapIntentService netRapIntentService = null;
    private NetRapTopoService netRapTopoService = null;
    private NetRapService NetRapService;

    private final int greetingDelay = 10;

    private TriggerGreeting nextGreeterTask = null;

    public NetRapRegistry() {
        registry = new HashMap<>();
        log = LoggerFactory.getLogger(NetRapRegistry.class);
        setRoute(route);
        //    timerGreeting = new Timer();

    }

    public NetRapRegistry(NetRapTopoService topoService, NetRapIntentService intentService) {
        log = LoggerFactory.getLogger(NetRapRegistry.class);
        log.info("Starting NetRapRegistry..");
        registry = new HashMap<>();

        setRoute(route);
        netRapIntentService = intentService;
        netRapTopoService = topoService;
        log.info("NetRapRegistry started!");
    }

    public void setNetRapIntentService(NetRapIntentService netRapIntentService) {
        this.netRapIntentService = netRapIntentService;
    }

    public void setNetRapTopoService(NetRapTopoService netRapTopoService) {
        this.netRapTopoService = netRapTopoService;
    }

    private void greetNewcomer(XrapMessage newComer) {
        log.info("Sending topology to new N2P instance");
        boolean retval = netRapTopoService.sendTopology(newComer.getRouteid());
        if (retval) {
            log.info("Successful!");
        } else {
            log.info("Failed!");
        }

        /* TODO: trying to fix duplicate intents in N2P // Pontus oct 25
    log.info("Sending intents to new N2P instance");
        retval = netRapIntentService.sendIntents(newComer.getRouteid());
        if (retval) {
            log.info("Successful!");
        } else {
            log.info("Failed!");
        }
        */
    }

    public List<RegEntry> getRegistered() {

        log.info("I have " + registry.size() + " registered n2ps");
        for (String reg : registry.keySet()) {
            log.info("Mapping " + reg + " to " + registry.get(reg));
        }

        Collection<RegEntry> val = registry.values();

        List<RegEntry> list = new ArrayList<RegEntry>(registry.values());
        return list;
    }


    public XrapReply POST(XrapPostRequest request) {
        log.warn("NetRapRegistry:POST called!");
        XrapPostReply rep = null;
        XrapErrorReply error = null;
        boolean valid = true;
        int length;
        if (request.getContentType() != null) {
            valid = request.getContentType().equals("application/json");
        } else {
            valid = false;
        }

        if (!valid) {
            log.info("Bad contentType!");
            error = new XrapErrorReply();
            error.setStatusCode(Constants.BadRequest_400);
            error.setRouteid(request.getRouteid());
            error.setRequestId(request.getRequestId());
            error.setErrorText("Only ContentType: application/json supported");
            return error;
        }

        String json = new String(request.getContentBody());
        Gson gson = new GsonBuilder().create();
        RegEntry l = gson.fromJson(json, RegEntry.class);
        l.setRouteId(request.getRouteid().array());
        log.info("Putting " + l + " into registry");

        registry.put(l.getName(), l);
        log.info("Registry now contains: " + registry.size() + " entries");
        rep = new XrapPostReply();
        rep.setEtag(l.getName());
        rep.setDateModified(new Date().getTime());
        rep.setStatusCode(Constants.Created_201);
        rep.setLocation(route + l.getName());
        rep.setRequestId(request.getRequestId());
        rep.setRouteid(request.getRouteid());

        if (nextGreeterTask == null) {
            log.info("Scheduling a greeting");
            nextGreeterTask = new TriggerGreeting(this, request.getRouteid());
            Timer timer = new Timer();
            timer.schedule(nextGreeterTask, greetingDelay);
        }
        return rep;
    }


    @Override
    public XrapReply GET(XrapGetRequest request) {
        String retval = "10";
        String index = null;
        Matcher m;
        RegEntry res = null;
        boolean useXml = false;
        String replyBody = "";
        Gson gson = new GsonBuilder().create();

        // support subresources by parameter "id"
        // or /test/{id}
        String location = request.getResource();
        m = GET_PATTERN.matcher(location);
        if (m.matches()) {
            index = m.group(1);
            log.info("Matched name \"" + m.group(1) + "\"");
            res = registry.get(m.group(1));
            if (res == null) {
                XrapErrorReply rep = new XrapErrorReply();
                rep.setStatusCode(Constants.NotFound_404);
                rep.setRouteid(request.getRouteid());
                rep.setRequestId(request.getRequestId());
                rep.setErrorText("Could not find resource");
                return rep;
            }
        }

        for (XrapGetRequest.Parameter p : request.getParameters()) {
            if (p.getName().equals("id")) {
                // prefer the parameter in the URN
                if (index == null) {
                    index = p.getStringValue();
                    res = registry.get(index);
                }
            } else {
                // unknown parameter passed, lets disagree with that
                log.info("Unknown parameter " + p.getName());
                XrapErrorReply rep = new XrapErrorReply();
                rep.setStatusCode(Constants.BadRequest_400);
                rep.setRouteid(request.getRouteid());
                rep.setRequestId(request.getRequestId());
                rep.setErrorText("Unknown parameter \"" + p.getName() + "\"");
                return rep;
            }
        }

        if (request.getContentType() != null) {
            if (request.getContentType().equals("application/xml")) {
                useXml = true;
            }
        }
        if (res == null) {
            replyBody = gson.toJson(registry, HashMap.class);
        } else {
            replyBody = gson.toJson(res, RegEntry.class);
        }

        if (replyBody != null) {
            XrapGetReply rep = new XrapGetReply();
            if (useXml) {
                rep.setContentType("application/test+xml");
            } else {
                rep.setContentType("application/test+json");
            }
            rep.setBody(replyBody.getBytes());

            if (index != null) {
                rep.setEtag(index.toString());
            } else {
                rep.setEtag("*");
            }
            rep.setDateModified(new Date().getTime());
            rep.setStatusCode(Constants.OK_200);
            rep.setRequestId(request.getRequestId());
            rep.setRouteid(request.getRouteid());
            return rep;
        } else {
            log.info("Replying with 404");
            XrapErrorReply rep = new XrapErrorReply();
            rep.setStatusCode(Constants.NotFound_404);
            rep.setRouteid(request.getRouteid());
            rep.setRequestId(request.getRequestId());
            rep.setErrorText("Could not find resource");
            return rep;
        }
    }

    private class TriggerGreeting extends TimerTask {
        NetRapRegistry parent;
        ByteBuffer address;

        protected TriggerGreeting(NetRapRegistry parent, ByteBuffer address) {
            this.parent = parent;
            this.address = address;
            parent.log.info("TriggerGreeting created");
        }

        @Override
        public void run() {
            parent.log.info("Sending topology to new N2P instance");
            boolean retval = parent.netRapTopoService.sendTopology(address);
            if (retval) {
                parent.log.info("Successful!");
            } else {
                parent.log.info("Failed!");
            }
            parent.nextGreeterTask = null;
        }
    }
}

