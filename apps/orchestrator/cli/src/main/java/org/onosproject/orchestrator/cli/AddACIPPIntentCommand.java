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
package org.onosproject.orchestrator.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.IntentService;

import java.util.List;

/**
 * Installs ACI intent.
 */
@Command(scope = "acino", name = "add-aci-pp-intent",
        description = "Installs aci intent between src and dst")
public class AddACIPPIntentCommand extends ConnectivityIntentCommand {

    @Argument(index = 0, name = "ingressDevice",
            description = "Ingress Device/Port Description",
            required = true, multiValued = false)
    String ingressDeviceString = null;

    @Argument(index = 1, name = "egressDevice",
            description = "Egress Device/Port Description",
            required = true, multiValued = false)
    String egressDeviceString = null;


    @Override
    protected void execute() {
        IntentService service = get(IntentService.class);

        ConnectPoint ingress = ConnectPoint.deviceConnectPoint(ingressDeviceString);
        ConnectPoint egress = ConnectPoint.deviceConnectPoint(egressDeviceString);

        TrafficSelector selector = buildTrafficSelector();
        TrafficTreatment treatment = buildTrafficTreatment();
        List<Constraint> constraints = buildConstraints();

        ACIPPIntent intent = ACIPPIntent.builder()
                .dst(egress)
                .src(ingress)
                .path(null) // Path has links and costs
                .appId(appId())
                .key(key())
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                .priority(priority())
                .build();

        service.submit(intent);
        print("New Aci intent submitted:\n%s", intent.toString());
    }

}
