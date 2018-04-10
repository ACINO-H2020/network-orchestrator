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

import com.wpl.xrapc.XrapException;
import com.wpl.xrapc.XrapPeer;
import com.wpl.xrapc.XrapReply;
import com.wpl.xrapc.XrapRequest;
import com.wpl.xrapc.XrapResource;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;
import org.onosproject.orchestrator.netrap.api.NetRapService;
import org.onosproject.orchestrator.netrap.api.NetRapTopoService;
import org.slf4j.Logger;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;
/* Implements the NetRapService */

@Component(immediate = true)
@Service
public class NetRapServiceImpl implements NetRapService {

    private static final int CORE_POOL_SIZE = 2;
    private final Logger log = getLogger(NetRapServiceImpl.class);
    /* Definera dependencies till andra services */
    protected ScheduledExecutorService ipLinksExectuor;
    private XrapPeer client = null;
    private NetRapRegistry reg;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetRapTopoService netRapTopoService;
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetRapIntentService netRapIntentService;
    private Thread xrapClient;

    @Activate
    protected void activate() {
        log.info("Starting NetRapService...");
        client = new XrapPeer("0.0.0.0", 7777, true);
        xrapClient = new Thread(client);
        xrapClient.start();
        if (netRapTopoService == null) {
            netRapTopoService = DefaultServiceDirectory.getService(NetRapTopoService.class);
        }

        if (netRapIntentService == null) {
            netRapIntentService = DefaultServiceDirectory.getService(NetRapIntentService.class);
        }


        client.addHandler(netRapTopoService);
        client.addHandler(netRapIntentService);
        reg = new NetRapRegistry(netRapTopoService, netRapIntentService);
        client.addHandler(reg);
        log.info("NetRapService started!");
    }

    @Deactivate
    protected void deactivate() {

        log.info("Stopping NetRapService...");
        client.terminate();
        log.info("Terminated sockets and waiting for cleanup..");
        try {
            xrapClient.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public XrapReply sendOne(ByteBuffer address, XrapRequest message) {
        try {
            //  log.info("calling client.send(" + address + ", " + message + ")");
            return client.send(address, message);
        } catch (XrapException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public XrapReply sendAny(XrapRequest message) {

        List<RegEntry> registered = reg.getRegistered();
        if (registered.isEmpty()) {
            return null;
        }
        RegEntry peer = registered.get(registered.size() - 1);
        ByteBuffer addr = ByteBuffer.wrap(peer.getRouteId());
        return sendOne(addr, message);
    }

    @Override
    public void addHandler(XrapResource resource) {
        client.addHandler(resource);
    }

    @Override
    public void delHandler(XrapResource resource) {
        client.delHandler(resource);
    }
}
