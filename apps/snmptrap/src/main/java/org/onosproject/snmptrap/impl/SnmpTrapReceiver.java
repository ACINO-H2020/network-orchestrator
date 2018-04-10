package org.onosproject.snmptrap.impl;


import org.onlab.packet.Ip4Address;
import org.slf4j.Logger;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.MessageException;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TcpAddress;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.AbstractTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.onlab.util.Tools.groupedThreads;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Manage the recieving of the SNMP Trap.
 */
public class SnmpTrapReceiver implements CommandResponder {
    private final Logger log = getLogger(getClass());

    private static final OID IF_ADMIN_STATUS = new OID("1.3.6.1.2.1.2.2.1.7");
    private static final OID IF_OPER_STATUS = new OID("1.3.6.1.2.1.2.2.1.8");
    private static final OID IF_NAME = new OID("1.3.6.1.2.1.31.1.1.1.1");

    private Address address = new UdpAddress("0.0.0.0/12345");

    private final ExecutorService executor =
            newSingleThreadExecutor(groupedThreads("onos/snmptrap", "receive-%d"));

    private SnmpTrapManager snmpTrapManager;
    private ThreadPool threadPool;
    private AbstractTransportMapping transport;

    public SnmpTrapReceiver(SnmpTrapManager snmpTrapManager) {
        executor.submit(() -> {
            try {
                this.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        this.snmpTrapManager = snmpTrapManager;
    }


    public void shutdown() {
        threadPool.interrupt();
        try {
            transport.close();
        } catch (IOException e) {
        }
        executor.shutdown();
    }

    /**
     * This method will listen for traps and response pdu's from SNMP agent.
     */

    public synchronized void listen() throws IOException {
        if (address instanceof TcpAddress) {
            transport = new DefaultTcpTransportMapping((TcpAddress) address);
        } else {
            transport = new DefaultUdpTransportMapping((UdpAddress) address);
        }

        threadPool = ThreadPool.create("DispatcherPool", 10);
        MessageDispatcher mtDispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());

        // add message processing models
        mtDispatcher.addMessageProcessingModel(new MPv1());
        mtDispatcher.addMessageProcessingModel(new MPv2c());

        // add all security protocols
        SecurityProtocols.getInstance().addDefaultProtocols();
        SecurityProtocols.getInstance().addPrivacyProtocol(new Priv3DES());

        //Create Target
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));

        Snmp snmp = new Snmp(mtDispatcher, transport);
        snmp.addCommandResponder(this);

        transport.listen();
        log.info("Listening on " + address);

        try {
            this.wait();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.error("SNMP Trap thread interrupted {}", ex);
        }
    }


    /**
     * This method will be called whenever a pdu is received on the given port
     * specified in the listen() method.
     */

    public synchronized void processPdu(CommandResponderEvent cmdRespEvent) {
        log.debug("Received PDU from {}", cmdRespEvent.getPeerAddress());
        Ip4Address deviceIp = Ip4Address.valueOf(cmdRespEvent.getPeerAddress().toString().split("/")[0]);
        PDU pdu = cmdRespEvent.getPDU();
        log.debug("pdu {}", pdu);
        if (pdu != null) {
            switch (pdu.getType()) {
                case PDU.TRAP:
                    processTrap(deviceIp, pdu);
                    break;
                case PDU.V1TRAP:
                    log.info("V1TRAP are not supported");
                    break;
                case PDU.REPORT:
                    //nothing to do
                    break;
                default:
                    break;
            }
        }
        try {
            cmdRespEvent.getMessageDispatcher().returnResponsePdu(
                    cmdRespEvent.getMessageProcessingModel(),
                    cmdRespEvent.getSecurityModel(), cmdRespEvent.getSecurityName(),
                    cmdRespEvent.getSecurityLevel(), pdu,
                    cmdRespEvent.getMaxSizeResponsePDU(),
                    cmdRespEvent.getStateReference(), new StatusInformation());
        } catch (MessageException ex) {
            log.error("Error while sending response: " + ex.getMessage());
        }
    }

    private void processTrap(Ip4Address deviceIp, PDU pdu) {
        int pduType = pdu.getType();
        Iterator iter = pdu.getVariableBindings().iterator();
        Optional<Boolean> oprStatus = Optional.empty();
        Optional<Boolean> adminStatus = Optional.empty();
        Optional<String> ifName = Optional.empty();
        while (iter.hasNext()) {
            VariableBinding var = (VariableBinding) iter.next();

            if (var.getOid().leftMostCompare(IF_OPER_STATUS.getBERLength()-3, IF_OPER_STATUS) == 0) {
                oprStatus = Optional.of(parseStatus(var.getVariable()));
            }
            if (var.getOid().leftMostCompare(IF_ADMIN_STATUS.getBERLength()-3, IF_OPER_STATUS) == 0) {
                adminStatus = Optional.of(parseStatus(var.getVariable()));
            }
            if (var.getOid().leftMostCompare(IF_NAME.getBERLength()-3, IF_NAME) == 0) {
                ifName = Optional.ofNullable(var.getVariable().toString());
            }
        }
        if (ifName.isPresent()) {
            snmpTrapManager.manageNotification(deviceIp,
                    ifName.get(),
                    oprStatus, adminStatus);
        }
    }


    private boolean parseStatus(Variable status) {
        switch (status.toInt()) {
            case 1:
                return true;
            case 2:
                return false;
            default:
                return false;
        }
    }

}
