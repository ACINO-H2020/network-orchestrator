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
import org.apache.karaf.shell.commands.Option;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.net.HostId;
import org.onosproject.net.RestorationType;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.constraint.NegotiableConstraint;
import org.onosproject.net.intent.constraint.RestorationConstraint;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Installs ACI intent.
 */
@Command(scope = "onos", name = "add-aci-intent",
        description = "Installs aci intent between two hosts connectivity intent")
public class AddAciIntentCommand extends ConnectivityIntentCommand {

    @Argument(index = 0, name = "one", description = "One host ID",
            required = true, multiValued = false)
    String one = null;

    @Argument(index = 1, name = "two", description = "Another host ID",
            required = true, multiValued = false)
    String two = null;

    @Option(name = "--restoration", description = "Restoration type",
            required = false, multiValued = false)
    private String restorationString = null;

    /**
     * Option to tell that the intent is negotiable.
     */
    @Option(name = "--negotiable",
            description = "the constraints can be negotiated")
    private boolean negotiable = false;

    @Override
    protected void execute() {
        IntentService service = get(IntentService.class);

        HostId oneId = HostId.hostId(one);
        HostId twoId = HostId.hostId(two);

        TrafficSelector selector = buildTrafficSelector();
        TrafficTreatment treatment = buildTrafficTreatment();
        List<Constraint> constraints = buildConstraints();

        // Check for restoration constraint
        if (!isNullOrEmpty(restorationString)) {
            final RestorationType restorationType = RestorationType.valueOf(restorationString);
            constraints.add(new RestorationConstraint(restorationType));
        }

        if (negotiable) {
            constraints.add(NegotiableConstraint.negotiable());
        }

        AciIntent intent = AciIntent.builder()
                .appId(appId())
                .key(key())
                .one(oneId)
                .two(twoId)
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                .priority(priority())
                .build();
        service.submit(intent);
        print("Aci intent submitted:\n%s", intent.toString());
    }

}
