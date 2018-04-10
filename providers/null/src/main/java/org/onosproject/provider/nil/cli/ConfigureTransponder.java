package org.onosproject.provider.nil.cli;

import com.google.common.collect.Lists;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.net.*;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.config.basics.BasicDeviceConfig;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.provider.Provider;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.provider.nil.CustomTopologySimulator;
import org.onosproject.provider.nil.NullProviders;
import org.onosproject.provider.nil.TopologySimulator;
import org.onosproject.net.device.DeviceProvider;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;

import java.util.List;
import static org.onosproject.net.optical.device.OchPortHelper.ochPortDescription;
import static org.onosproject.net.optical.device.OduCltPortHelper.oduCltPortDescription;

@Command(scope = "onos", name = "null-config-transponder",
        description = "Set the port type of a null device")
public class ConfigureTransponder extends AbstractShellCommand {

    @Argument(index = 0, name = "name", description = "Device name",
            required = true, multiValued = false)
    String name = null;

    @Argument(index = 1, name = "speed", description = "Transponder speed",
            required = true, multiValued = false)
    Integer portSpeed = null;

    @Argument(index = 2, name = "frequency slot", description = "Transponder frequency slot",
            required = true, multiValued = false)
    Integer portSlot = null;

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
        boolean found = false;
        DeviceId foundDev  = null;
        String output;
        for (Device dev : deviceService.getDevices()){
            Annotations ann = dev.annotations();
            if(ann.keys().contains("name")){
                if(name.equals(ann.value("name"))){
                    print("Found device: " + dev + "\n");
                    found = true;
                    foundDev = dev.id();
                }
            }
        }
        if(!found){
            error("Device with name " + name + " not found!");
            return;
        }

        List<PortDescription> portDescriptions = Lists.newArrayList();

        if (portSpeed == 1) {
            portDescriptions.add(oduCltPortDescription(PortNumber.portNumber(1),
                    true, // enabled
                    CltSignalType.CLT_1GBE));
        } else if (portSpeed == 10){
            portDescriptions.add(oduCltPortDescription(PortNumber.portNumber(1),
                    true, // enabled
                    CltSignalType.CLT_10GBE));
        } else if (portSpeed == 40){
            portDescriptions.add(oduCltPortDescription(PortNumber.portNumber(1),
                    true, // enabled
                    CltSignalType.CLT_40GBE));
        } else if (portSpeed == 100){
            portDescriptions.add(oduCltPortDescription(PortNumber.portNumber(1),
                    true, // enabled
                    CltSignalType.CLT_100GBE));
        } else {
            error("Port speed has to be 1,10,40, or 100");
            return;
        }

        portDescriptions.add(ochPortDescription(PortNumber.portNumber(2),
                true, // enabled
                OduSignalType.ODU0,
                true, // tunable
                OchSignal.newDwdmSlot(ChannelSpacing.CHL_50GHZ, portSlot)));

        sim.configureTransponder(foundDev,portDescriptions);
        return;

/*
        DeviceId deviceId = sim.nextDeviceId();
        BasicDeviceConfig cfg = cfgService.addConfig(deviceId, BasicDeviceConfig.class);
        cfg.name(name);
        cfg.apply();

        sim.createDevice(deviceId, name, Device.Type.valueOf(type.toUpperCase()), portNumber);
*/
    }

}
