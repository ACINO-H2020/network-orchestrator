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

package org.onosproject.orchestrator.dismi.compiler;

import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.orchestrator.dismi.primitives.EthSelector;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-01-18.
 */
public class SelectorCompiler {
    private final Logger log = getLogger(getClass());
    private Selector selector;

    public SelectorCompiler(Selector selector) {
        this.selector = selector;
    }

    public TrafficSelector toTrafficSelector() {

        if (selector instanceof IPSelector) {
            log.info("Selector is IPSelector !");
            return buildTrafficSelectorFromIPSelector((IPSelector) selector);
        }
        if (selector instanceof EthSelector) {
            log.info("Selector is EthSelector !");
            return buildTrafficSelectorFromEthSelector((EthSelector) selector);
        }
        return null;
    }

    private TrafficSelector buildTrafficSelectorFromIPSelector(IPSelector ipSelector) {
        IPSelectorCompiler ipSelectorCompiler = new IPSelectorCompiler(ipSelector);
        return ipSelectorCompiler.toTrafficSelector();
    }

    // ToDo: This will be implemented for building TrraficSelector from Ethernet Selector
    private TrafficSelector buildTrafficSelectorFromEthSelector(EthSelector selector) {
        return null;
    }
}
