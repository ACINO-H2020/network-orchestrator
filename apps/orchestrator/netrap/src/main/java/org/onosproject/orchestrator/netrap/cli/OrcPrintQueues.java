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
import org.onosproject.cli.AbstractShellCommand;
import org.onosproject.orchestrator.netrap.api.NetRapTransactionService;

/**
 * Created by ponsko on 2017-03-08.
 */

@Command(scope = "acino", name = "orc-print-queues",
        description = "Print the current intent queues")
public class OrcPrintQueues extends AbstractShellCommand {
    @Override
    protected void execute() {
        NetRapTransactionService service = get(NetRapTransactionService.class);
        String res = service.getStatus();
        print(res);
    }

}
