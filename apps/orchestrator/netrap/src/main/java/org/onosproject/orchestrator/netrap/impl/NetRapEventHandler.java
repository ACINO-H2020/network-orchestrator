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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.event.Event;
import org.onosproject.net.Link;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentListener;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.topology.TopologyEvent;
import org.onosproject.net.topology.TopologyListener;
import org.onosproject.net.topology.TopologyService;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;
import org.onosproject.orchestrator.netrap.api.NetRapTopoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.groupedThreads;


@Component(immediate = true)
public class NetRapEventHandler {
    private static final Logger log = LoggerFactory.getLogger(NetRapEventHandler.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetRapIntentService netRapIntentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetRapTopoService netRapTopoService;

    private final TopologyListener topologyListener = new InternalTopologyListener();
    private final IntentListener intentListener = new InternalIntentListener();

    private static final int DEFAULT_TOPOLOGY_UPDATE_DELAY = 2000;

    private Timer timerTopo = null;
    private TriggerTopologyUpdate nextTopoTask = null;
    private ExecutorService eventExecutor;


    @Activate
    protected void activate() {
        log.info("Starting NetRapEventhandler..");
        eventExecutor = newSingleThreadScheduledExecutor(
                groupedThreads("netrap/eventhander", "n2pevents-%d", log));
        topologyService.addListener(topologyListener);
        intentService.addListener(intentListener);
        timerTopo = new Timer();
        log.info("NetRapEventhandler started!");

    }

    @Deactivate
    protected void deactivate() {
        timerTopo.cancel();
        eventExecutor.shutdownNow();
        eventExecutor = null;
        intentService.removeListener(intentListener);
        topologyService.removeListener(topologyListener);

        log.info("NetRapEventhandler stopped!");
    }

    protected void scheduleTopologyUpdate() {
        if (nextTopoTask == null) {
            log.info("Scheduling topology update..");
            nextTopoTask = new TriggerTopologyUpdate(this);
            eventExecutor.submit(nextTopoTask, DEFAULT_TOPOLOGY_UPDATE_DELAY);
        }
    }

    /**
     * Listens to topology events and processes the topology changes.
     */
    private class InternalTopologyListener implements TopologyListener {

        @Override
        public void event(TopologyEvent event) {
            if (event == null) {
                return;
            }

            if (event.reasons() != null && !event.reasons().isEmpty()) {
                for (Event reason : event.reasons()) {
                    if (reason instanceof LinkEvent) {
                        LinkEvent linkEvent = (LinkEvent) reason;
                        if (linkEvent.subject().type() != Link.Type.INDIRECT) {
                            scheduleTopologyUpdate();
                        }
                    }
                }
            }
        }
    }

    /**
     * Listens to ACI-PP-Intent events
     */
    private class InternalIntentListener implements IntentListener {

        @Override
        public void event(IntentEvent intentEvent) {
            if (intentEvent == null) {
                return;
            }

            if (!(intentEvent.subject() instanceof ACIPPIntent)) {
                return;
            }

            if (netRapIntentService == null) {
                log.error("netRapIntentService == null!");
                netRapIntentService = DefaultServiceDirectory.getService(NetRapIntentService.class);
            }
            // Intent events contain the affected event, no need to update all
            switch (intentEvent.type()) {
                case FAILED:
                    log.info("FAILED intent: " + intentEvent.subject().key());
                    netRapIntentService.updateIntents(intentEvent.subject());
                    break;
                case WITHDRAW_REQ:
                    netRapIntentService.deleteIntent(intentEvent.subject());
                    break;
                case INSTALLED:
                    log.info("INSTALLED intent: " + intentEvent.subject().key() + " " + System.currentTimeMillis());
                    netRapIntentService.updateIntents(intentEvent.subject());
                    break;
                case CORRUPT:
                    //Avoiding the optoQueue block in case of intent corrupted
                    netRapIntentService.updateIntents(intentEvent.subject());
                    break;
                default:
                    break;
            }
        }
    }

    private class TriggerTopologyUpdate extends TimerTask {
        NetRapEventHandler parent;

        protected TriggerTopologyUpdate(NetRapEventHandler parent) {
            this.parent = parent;
            log.info("TriggerTopologyUpdate created");
        }

        @Override
        public void run() {
            log.info("Triggering topology update..");
            // trigger topology update
            parent.netRapTopoService.updateTopology();
            // remove task
            parent.nextTopoTask = null;
        }
    }

}
