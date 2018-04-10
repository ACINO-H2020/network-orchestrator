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

package org.onosproject.orchestrator.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.slf4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
public class OrchestratorManager {

    private static final int CORE_POOL_SIZE = 2;
    private final Logger log = getLogger(getClass());
    protected ScheduledExecutorService ipLinksExecutor;

    @Activate
    protected void activate() {

        ipLinksExecutor = Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {

        log.info("Stopped");
    }

}
