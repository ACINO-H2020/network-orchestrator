package org.onosproject.provider.nil.cli;

/**
 * Created by ponsko on 2016-12-12.
 */
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DefaultAnnotations;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.edge.EdgePortService;
import org.onosproject.net.host.HostService;
import org.onosproject.provider.nil.CustomTopologySimulator;
import org.onosproject.provider.nil.NullProviders;
import org.onosproject.provider.nil.TopologySimulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Adds a simulated link to the custom topology simulation.
 */

@Command(scope = "acino", name = "create-annotated-link",
        description = "Adds a simulated link to the custom topology simulation")
public class  CreateAnnotatedLink extends AbstractShellCommand {

    @Argument(index = 0, name = "type", description = "Link type, e.g. direct, indirect, optical",
            required = true, multiValued = false)
    String type = null;

    @Argument(index = 1, name = "src", description = "Source device name",
            required = true, multiValued = false)
    String src = null;

    @Argument(index = 2, name = "dst", description = "Destination device name",
            required = true, multiValued = false)
    String dst = null;

    @Option(name = "-u", aliases = "--unidirectional", description = "Unidirectional link only",
            required = false, multiValued = false)
    private boolean unidirectional = false;
    @Option(name = "-a", aliases = "--annotations", description = "Annotations ",
            required = true, multiValued = true)
    private List<String> annots;



    @Override
    protected void execute() {
        DefaultAnnotations annotations ;
        String key = null;
        String value = null;
        DefaultAnnotations.Builder builder = DefaultAnnotations.builder();
        HashMap<String, String> map = new HashMap<>();
        for (String s : annots) {
            if (key == null && value == null) {
                key = s;
            } else if (key != null && value == null) {
                value = s;
                print("Added annotation " + key + " = " + value);
                builder.set(key, value);
                key = null;
                value = null;
            }


        }
        annotations = builder.build();
        print("setting annotations " + annotations.toString());

        NullProviders service = get(NullProviders.class);
        TopologySimulator simulator = service.currentSimulator();
        if (!(simulator instanceof CustomTopologySimulator)) {
            error("Custom topology simulator is not active.");
            return;
        }

        CustomTopologySimulator sim = (CustomTopologySimulator) simulator;
        ConnectPoint one = findAvailablePort(sim.deviceId(src), null);
        ConnectPoint two = findAvailablePort(sim.deviceId(dst), one);
        sim.createLinkAnnotated(one, two, Link.Type.valueOf(type.toUpperCase()), !unidirectional,annotations);
    }

    // Finds an available connect point among edge ports of the specified device
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

}
