package org.onosproject.provider.nil.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.*;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.BasicDeviceConfig;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.link.DefaultLinkDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.provider.nil.CustomTopologySimulator;
import org.onosproject.provider.nil.NullProviders;
import org.onosproject.provider.nil.TopologySimulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

/**
 * Adds a simulated device to the custom topology simulation.
 * Models a Directionless ROADM
 * TODO: Based on slides from Achim, add Colorless Directionless ROADM model
 */
@Command(scope = "onos", name = "null-create-roadm",
        description = "Adds a simulated roadm to the custom topology simulation")
public class CreateNullRoadm extends AbstractShellCommand {

    @Argument(index = 0, name = "name", description = "Device name",
            required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "linePorts", description = "Number of line ports",
            required = true, multiValued = false)
    Integer linePorts = null;

    @Argument(index = 2, name = "clientPorts", description = "Number of client ports",
            required = true, multiValued = false)
    Integer clientPorts = null;


    @Argument(index = 3, name = "latitude", description = "Geo latitude",
            required = true, multiValued = false)
    Double latitude = null;

    @Argument(index = 4, name = "longitude", description = "Geo longitude",
            required = true, multiValued = false)
    Double longitude = null;

    @Override
    protected void execute() {
        NullProviders service = get(NullProviders.class);
        NetworkConfigService cfgService = get(NetworkConfigService.class);
        DeviceService deviceService = get(DeviceService.class);
        TopologySimulator simulator = service.currentSimulator();
        if (!(simulator instanceof CustomTopologySimulator)) {
            error("Custom topology simulator is not active.");
            return;
        }

        CustomTopologySimulator sim = (CustomTopologySimulator) simulator;

        // Create bottom roadm node
        DeviceId bottomDeviceId = sim.nextDeviceId();
        BasicDeviceConfig cfg = cfgService.addConfig(bottomDeviceId, BasicDeviceConfig.class);
        cfg.name(name + "_bottom");
        if (latitude != 0 && longitude != 0) {
            cfg.latitude(latitude);
            cfg.longitude(longitude);
        }
        cfg.apply();
        sim.createRoadmDevice(bottomDeviceId, name + "_bottom", Device.Type.ROADM, linePorts + 1);
        //log.debug("Created bottom ROADM : " + deviceService.getDevice(bottomDeviceId));


        // Create top roadm
        DeviceId topDeviceId = sim.nextDeviceId();
        cfg = cfgService.addConfig(topDeviceId, BasicDeviceConfig.class);
        cfg.name(name + "_top");
        if (latitude != 0 && longitude != 0) {
            cfg.latitude(latitude);
            cfg.longitude(longitude);
        }
        cfg.apply();
        sim.createRoadmDevice(topDeviceId, name + "_top", Device.Type.ROADM, clientPorts + 1);
        //log.debug("Created top ROADM : " + deviceService.getDevice(topDeviceId));

        // Create transponders
        DeviceId transponderDeviceIds[] = new DeviceId[clientPorts];
        for (int i = 0; i < clientPorts; i++) {
            transponderDeviceIds[i] = sim.nextDeviceId();
            cfg = cfgService.addConfig(transponderDeviceIds[i], BasicDeviceConfig.class);
            cfg.name(name + "_tr" + "_" + i);
            if (latitude != 0 && longitude != 0) {
                cfg.latitude(latitude);
                cfg.longitude(longitude);
            }

            cfg.apply();
            if(i==0)
                sim.createOTNDevice(transponderDeviceIds[i], name + "_tr" + "_" + i, Device.Type.OTN, 2, 10);
            else
                sim.createOTNDevice(transponderDeviceIds[i], name + "_tr" + "_" + i, Device.Type.OTN, 2, 1);
//            log.debug("Created transponder : " + deviceService.getDevice(transponderDeviceIds[i]));
        }

        // TODO: seems to be a race condition here
        // Where, probably, findAvailablePort returns null
        // Link the two ROADM blocks
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DefaultAnnotations annotations = DefaultAnnotations.builder().set("originalNode", "true").build();

        ConnectPoint one = findAvailablePort(bottomDeviceId, null);
  //      log.debug("Found connect point one: " + one);
        ConnectPoint two = findAvailablePort(topDeviceId, one);
  //      log.debug("Found connect point two: " + two);
        sim.createLinkAnnotated(one, two, Link.Type.OPTICAL, true, annotations);
   //     log.debug("Created link between " + one + " and " + two);

        // Link transponders with the leftover ports on top roadm block
        for (int i = 0; i < clientPorts; i++) {
            one = findAvailablePort(topDeviceId, null);
            two = findAvailableOchPort(transponderDeviceIds[i], one);
            if(one == null || two == null){
                error("Could not find ports!");
                break;
            }
            sim.createLinkAnnotated(one, two, Link.Type.OPTICAL, true,annotations);
     //       log.debug("Created link between " + one + " and " + two);
        }
    }

    private ConnectPoint findAvailablePort(DeviceId deviceId, ConnectPoint otherPoint) {
        EdgePortService eps = get(EdgePortService.class);
        HostService hs = get(HostService.class);
        Iterator<ConnectPoint> points = eps.getEdgePoints(deviceId).iterator();

        while (points.hasNext()) {
            ConnectPoint point = points.next();
            if (!Objects.equals(point, otherPoint) && hs.getConnectedHosts(point).isEmpty()) {
                return point;
            }
        }
        return null;
    }

    private ConnectPoint findAvailableOchPort(DeviceId deviceId, ConnectPoint otherPoint) {
        EdgePortService eps = get(EdgePortService.class);
        HostService hs = get(HostService.class);
        DeviceService deviceService = get(DeviceService.class);
        Iterator<ConnectPoint> points = eps.getEdgePoints(deviceId).iterator();

        while (points.hasNext()) {
            ConnectPoint point = points.next();

            if (!Objects.equals(point, otherPoint) && hs.getConnectedHosts(point).isEmpty()) {
                Port port = deviceService.getPort(deviceId, point.port());
                log.debug("Found port: " + port);
                if (port.type() == Port.Type.OCH) {
                    log.debug("Returing port: " + port);
                    return point;
                }
            }
        }
        return null;
    }
}
