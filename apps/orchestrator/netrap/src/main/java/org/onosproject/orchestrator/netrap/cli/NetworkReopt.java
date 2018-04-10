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
package org.onosproject.orchestrator.netrap.cli;


import org.apache.karaf.shell.commands.Command;
import org.onosproject.cli.net.ConnectivityIntentCommand;
import org.onosproject.orchestrator.netrap.api.NetRapIntentService;

/**
 * Calls N2P for network reoptimization.
 */
@Command(scope = "acino", name = "network-reopt",
        description = "Triggers network re-optimization")
public class NetworkReopt extends ConnectivityIntentCommand {

    @Override
    protected void execute() {
        NetRapIntentService service = get(NetRapIntentService.class);
        if (service == null) {
            error("Could not find NetRapIntentService!");
        } else {
            print("Calling for network re-optimization..");
            service.networkReopt();
        }
    }

}
