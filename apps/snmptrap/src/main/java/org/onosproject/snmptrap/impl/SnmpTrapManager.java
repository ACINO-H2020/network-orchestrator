package org.onosproject.snmptrap.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ip4Address;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.Device;
import org.onosproject.net.Port;
import org.onosproject.net.device.DefaultPortDescription;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.netconf.NetConfNotification;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Component that manage the SNMP Trap.
 */
@Component(immediate = true)
public class SnmpTrapManager {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected NetConfNotification netConfNotification;

    private ApplicationId appId;
    private final DeviceListener topologyListener = new InternalDeviceListener();
    private SnmpTrapReceiver snmpTrapReceiver;
    private Map<Ip4Address, Device> relevantDevices = new HashMap<>();

    static final String AK_OPER_STATUS = "operStatus";

    static final String AK_ADMIN_STATUS = "adminStatus";

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.snmptrap");
        loadDevices();
        deviceService.addListener(topologyListener);
        snmpTrapReceiver = new SnmpTrapReceiver(this);
        log.info("Started {}", appId.name());
    }

    @Deactivate
    public void deactivate() {
        relevantDevices.clear();
        snmpTrapReceiver.shutdown();
        snmpTrapReceiver = null;
        deviceService.removeListener(topologyListener);
    }

    @Modified
    public void modified() {
        deactivate();
        activate();
    }


    public void manageNotification(Ip4Address deviceIp, String interf,
                                   Optional<Boolean> newOprStatus, Optional<Boolean> newAdminStatus) {

        log.info("Notification for {} interface {}, operStatus {}, adminStatus {}", deviceIp, interf,
                newOprStatus.isPresent() ? newOprStatus.get().toString() : "not reported",
                newAdminStatus.isPresent() ? newAdminStatus.get().toString() : "not reported");
        Device dev = relevantDevices.get(deviceIp);
        if (dev != null) {
            Collection<Port> ports = deviceService.getPorts(dev.id());
            Optional<Port> port = ports.stream().filter(x -> x.annotations().value(AnnotationKeys.PORT_NAME)
                    .equals(interf.toString())).findAny();
            DefaultAnnotations.Builder annBuilder = DefaultAnnotations.builder();
            if (port.isPresent()) {
                boolean enable;

                newOprStatus.ifPresent(x -> annBuilder.set(AK_OPER_STATUS, x.toString()));
                newAdminStatus.ifPresent(x -> annBuilder.set(AK_ADMIN_STATUS, x.toString()));

                enable = newOprStatus.isPresent() ? newOprStatus.get() : port.get().isEnabled();

                if(newAdminStatus.isPresent()) {
                    if(newAdminStatus.get() && !port.get().isEnabled()) {
                        //The port is administratively up, so let's enable it
                        enable = true;
                    } else if (!newAdminStatus.get()) {
                        //The port is disable
                        enable = false;
                    }
                }

                PortDescription portDescription = new DefaultPortDescription(
                        port.get().number(),
                        enable,
                        port.get().type(),
                        port.get().portSpeed(),
                        annBuilder.build());
                netConfNotification.portChanged(dev.id(), portDescription);
            }
        }
    }

    private void loadDevices() {
        deviceService.getAvailableDevices().forEach(this::storeRelevantDevice);
    }

    private void storeRelevantDevice(Device device) {
        if (device.providerId().scheme().equals("netconf")) {
            relevantDevices.put(Ip4Address.valueOf(device.annotations().value("ipaddress")), device);
        }
        log.info("relevantDevices {}", relevantDevices);
    }


    private class InternalDeviceListener implements DeviceListener {

        @Override
        public void event(DeviceEvent event) {
            switch (event.type()) {
                case DEVICE_ADDED:
                    storeRelevantDevice(event.subject());
                    break;
                default:
                    break;
            }
        }
    }
}
