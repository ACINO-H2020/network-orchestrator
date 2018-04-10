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
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.net.Link;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.link.LinkEvent;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkService;
import org.onosproject.orchestrator.netrap.api.NetRapTransactionService;
import org.onosproject.provider.nil.CustomTopologySimulator;
import org.onosproject.provider.nil.NullProviders;
import org.onosproject.provider.nil.TopologySimulator;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.onlab.util.Tools.get;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;

enum ActionType {
    NEW, ROUTE, MOVE
}

/**
 * Created by ponsko on 2017-03-06.
 */

@Component(immediate = true)
@Service
public class NetRapTransactionImpl implements NetRapTransactionService {
    private final Logger log = getLogger(NetRapTransactionImpl.class);
    private final LinkListener linkListener = new TransactionListener(this);
    private ArrayDeque<ActionItem> optoQueue;
    private ArrayDeque<ActionItem> ipQueue;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ComponentConfigService configService;

    protected ExecutorService eventExecutor;

    @Property(label = "Enable when using the nullProvider",
            name = "nullProvider",
            boolValue = false)
    private boolean nullProvider = false;


    @Activate
    protected void activate(ComponentContext context) {
        log.info("Starting NetRapTransactionService");
//        eventExecutor = newSingleThreadScheduledExecutor(
//                groupedThreads("netrap/transactions", "netraptrans-%d", log));
        configService.registerProperties(getClass());
        modified(context);
        eventExecutor = newSingleThreadScheduledExecutor(groupedThreads("netrap/linkevent", "events-%d", log));

        optoQueue = new ArrayDeque<>();
        ipQueue = new ArrayDeque<>();
        linkService.addListener(linkListener);

    }

    @Deactivate
    protected void deactivate() {
        log.info("Stopping NetRapTransactionService");
        configService.unregisterProperties(getClass(), false);
        linkService.removeListener(linkListener);
        eventExecutor.shutdownNow();
        eventExecutor = null;
    }

    @Modified
    public void modified(ComponentContext context) {
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new java.util.Properties();
        boolean newEnabled;
        try {
            String s = get(properties, "nullProvider");
            newEnabled = isNullOrEmpty(s) ? nullProvider : Boolean.parseBoolean(s.trim());
        } catch (NumberFormatException e) {
            log.warn(e.getMessage());
            newEnabled = false;
        }
        nullProvider = newEnabled;
        log.info("Using nullProvider: " + nullProvider);
    }


    private synchronized boolean removeExpectedLinks(Link modifiedLink) {
        int tot = 0;
        for (ActionItem ai : ipQueue) {
            tot += ai.removeWaitingLink(modifiedLink);
        }
        if (tot > 0) {
            log.info("removed " + tot + " expected links");
            return true;
        }
        return false;
    }

    private void printIPQueue() {
        if (ipQueue.size() == 0) {
            return;
        }
        log.info("IP QUEUE: " + ipQueue.size());
        for (ActionItem ai : ipQueue) {
            log.info("  ActionItem: " + ai);
        }
    }

    @Override
    public void addOpticalIntent(ACIPPIntent intent) {
        if (optoQueue.isEmpty()) {
            intentService.submit(intent);
        }
        optoQueue.add(new ActionItem(ActionType.NEW, intent, null));
    }

    @Override
    public void notifyInstalledOpticalIntent(Key intentKey) {
        ArrayDeque<ActionItem> waitingElements = new ArrayDeque<>();
        while (!optoQueue.isEmpty()) {
            ActionItem ai = optoQueue.peek();
            if (ai.getIntent().key().equals(intentKey)) {
                optoQueue.pop();
                if (!optoQueue.isEmpty()) {
                    ai = optoQueue.pop();
                    waitingElements.add(ai);
                    intentService.submit(ai.getIntent());
                }
            } else {
                waitingElements.add(ai);
                optoQueue.pop();
            }
        }
        optoQueue = waitingElements;
    }

    private synchronized void evaluateIPQueue() {
        if (ipQueue.isEmpty()) {
            return;
        }

        ActionItem ai = ipQueue.peek();
        ArrayDeque<ActionItem> waitingElements = new ArrayDeque<>();

        while (!ipQueue.isEmpty()) {
            if (ai == null) {
                return;
            }
            if (ai.isWaiting()) {
                log.info("IP Queue has a waiting action item, continue...");
                waitingElements.add(ai);
                ipQueue.pop();
            } else {
                // It's not waiting, pop it of the queue and install it!
                ai = ipQueue.pop();
                log.info("####################################");
                log.info("evaluateIPQueue installing IP intent with key: " + ai.getIntent().key());
                log.info("PATH: " + ai.getIntent().path());
                log.info("####################################");
                intentService.submit(ai.getIntent());
                log.info("   Submitted IP intent " + ai.getIntent().key());
            }
            // check the next one
            ai = ipQueue.peek();
        }

        ipQueue = waitingElements;
    }

    private synchronized void removeIntentFromQueue(Key intentKey) {
        if (ipQueue.isEmpty()) {
            return;
        }

        ActionItem ai = ipQueue.peek();
        ArrayDeque<ActionItem> waitingElements = new ArrayDeque<>(ipQueue.size());

        while (!ipQueue.isEmpty()) {
            if (ai == null) {
                return;
            }
            if (ai.getIntent().key().equals(intentKey)) {
                ipQueue.pop();
            } else {
                waitingElements.add(ai);
            }
            // check the next one
            ai = ipQueue.peek();
        }

        ipQueue = waitingElements;
    }

    @Override
    public synchronized void addRouteIntent(ACIPPIntent intent, List<ExpectedLink> expectedLinks) {
        // Make a copy to avoid Concurrent Modification exception
        ExpectedLink[] expectedLinksArray = expectedLinks.toArray(new ExpectedLink[expectedLinks.size()]);

        ActionItem ai = new ActionItem(ActionType.ROUTE, intent, expectedLinks);
        ipQueue.add(ai);
        log.info("ActionItem created: " + ai);

        for (Link linkToCheck : linkService.getLinks()) {
            removeExpectedLinks(linkToCheck);
        }

        if (!ai.isWaiting()) {
            evaluateIPQueue();
        } /*else {
            if (timerTransTask == null) {
                timerTransTask = new SubmitRoutes(this);
                timerTrans.schedule(timerTransTask, DEFAULT_DELAY_SECONDS);
            }
        }*/
        // only for the nullprovider!
        if (nullProvider) {
            if (expectedLinks.size() == 0) {
                return;
            }

            for (ExpectedLink el : expectedLinksArray) {
                log.info("addRouteIntent installing expected Link");
                installExpectedLinkNonBlock(el);
            }
        }
    }

    @Override
    public void removeRouteIntent(Key intentKey) {
        removeIntentFromQueue(intentKey);
    }

    @Override
    public void addMoveIntent(ACIPPIntent intent) {
        ipQueue.add(new ActionItem(ActionType.MOVE, intent, null));
        evaluateIPQueue();
    }

    @Override
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("OptoQueue: \n");
        for (ActionItem ai : optoQueue) {
            sb.append(ai.toString()).append("\n");
        }
        sb.append("IPQueue: \n");
        for (ActionItem ai : ipQueue) {
            sb.append(ai.toString()).append("\n");
        }

        return sb.toString();
    }

    private boolean installExpectedLinkNonBlock(ExpectedLink link) {
        log.info("Installing expected Link " + link);

        NullProviders service = DefaultServiceDirectory.getService(NullProviders.class);
        TopologySimulator simulator = service.currentSimulator();
        if (!(simulator instanceof CustomTopologySimulator)) {
            log.error("CANNOT FIND CustomerTopologySimulator!");
            return false;
        }
        CustomTopologySimulator sim = (CustomTopologySimulator) simulator;
        sim.createLinkAnnotated(link.src(), link.dst(), Link.Type.DIRECT, true, link.annotations());
        return true;
    }

    private class TransactionListener implements LinkListener {
        NetRapTransactionImpl parent;
        //private final Logger log = getLogger(NetRapTransactionImpl.class);

        TransactionListener(NetRapTransactionImpl parent) {
            this.parent = parent;
        }

        @Override
        public void event(LinkEvent event) {
            if (event == null) {
                log.error("Topology event is null.");
                return;
            }

            if (event.type() == LinkEvent.Type.LINK_ADDED || event.type() == LinkEvent.Type.LINK_UPDATED) {
                log.info("Checking if there's any links to remove.. ");

                if (removeExpectedLinks(event.subject())) {
                    //we need an executor to handle all the links that will come up, otherwise we can skip sth
                    eventExecutor.submit(new SubmitRoutes(this.parent));
                }
            }
        }
    }

    private class SubmitRoutes extends TimerTask {
        NetRapTransactionImpl parent;

        SubmitRoutes(NetRapTransactionImpl parent) {
            this.parent = parent;
        }

        @Override
        public void run() {

            // trigger installation topology update
            if (!parent.ipQueue.isEmpty()) {
                log.info("SubmitRoutes running, evaluating IP queue");
                parent.evaluateIPQueue();
            }
        }
    }
}

class ActionItem {
    private ActionType actionType;
    private List<ExpectedLink> waiting;
    private ACIPPIntent intent;
    private final Logger log = getLogger(ActionItem.class);

    ActionItem(ActionType act, ACIPPIntent intent1, List<ExpectedLink> linklist) {
        this.actionType = act;
        this.intent = intent1;
        this.waiting = linklist;

    }

    ActionType getActionType() {
        return actionType;
    }

    void setActionType(ActionType act) {
        this.actionType = act;
    }

    boolean isWaiting() {
        return waiting != null && waiting.size() != 0;

    }

    List<ExpectedLink> getWaiting() {
        return waiting;
    }

    void addWaitingLink(ExpectedLink expectedLink) {
        if (waiting == null) {
            this.waiting = new ArrayList<>();
        }

        waiting.add(expectedLink);
    }

    int removeWaitingLink(Link newlink) {
        if (this.waiting == null) {
            return 0;
        }

        for (ExpectedLink expectedLink : this.waiting) {

            if (expectedLink.src().equals(newlink.src()) && expectedLink.dst().equals(newlink.dst())) {
                this.waiting.remove(expectedLink);
                return 1;
            } /*else if (expectedLink.src().equals(newlink.dst()) && expectedLink.dst().equals(newlink.src())) {
                log.info("  Matching! ");
                this.waiting.remove(expectedLink);
                return 1;
            }*/
        }
        return 0;
    }

    ACIPPIntent getIntent() {
        return intent;
    }

    public void setIntent(ACIPPIntent intent) {
        this.intent = intent;
    }

    public String toString() {
        return toStringHelper(this)
                .add("action", actionType)
                .add("waiting", waiting)
                .add("intent", intent)
                .toString();
    }
}
