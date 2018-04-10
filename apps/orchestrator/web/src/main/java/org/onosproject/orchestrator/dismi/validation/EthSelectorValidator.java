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

package org.onosproject.orchestrator.dismi.validation;

import org.onosproject.orchestrator.dismi.primitives.EthSelector;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class EthSelectorValidator extends FieldValidator {
    public static final int VLAN_ID_MIN = 0;
    public static final int VLAN_ID_MAX = 4094;
    public static final int VLAN_PRIO_MIN = 0;
    public static final int VLAN_PRIO_MAX = 7;
    private String className = "ETHSelector";
    private final Logger log = getLogger(getClass());

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        EthSelector selector = null;
        log.info("Validating and resolving ethernet selector !");
        if (null == field) {
            log.error("EthSelector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "field");
            return null;
        }
        if (!(field instanceof EthSelector)) {
            log.error("Invalid EthSelector instance, Cannot cast to EthSelector!");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANETHSELECTOR,
                             "field");
            return null;
        }

        selector = (EthSelector) field;
        if (null == tracker) {
            log.error("Invalid tracker in EthSelector !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }

        EthSelector objEthSelector = validateAndResolve(selector, tracker);
        return objEthSelector;
    }

    private EthSelector validateAndResolve(EthSelector selector, Tracker tracker) {
        InputAssertion inputAssertion = new InputAssertion();
        log.info("Validating and resolving MAC addresses !");
        String ethSrcMacAddr = selector.getEthSrcMacAddr();
        if (null != ethSrcMacAddr) {
            if (!inputAssertion.isValidateMac(ethSrcMacAddr)) {
                log.error("Invalid source MAC address '" + ethSrcMacAddr + "' !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "Source MAC address \"" + ethSrcMacAddr + "\" " +
                                         "is not valid !");
            }
        } else {
            log.error("Source MAC address is null !");
        }

        String ethDestAddr = selector.getEthDestAddr();
        if (null != ethDestAddr) {
            if (!inputAssertion.isValidateMac(ethDestAddr)) {
                log.error("Invalid destination MAC address '" + ethDestAddr + "' !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "Destination MAC address \"" + ethDestAddr + "\" " +
                                         "is not valid !");
            }
        } else {
            log.error("Destination MAC address is null !");
        }
        Integer vlanId = selector.getVlanId();
        if (null != vlanId) {
            if (!(vlanId >= VLAN_ID_MIN && vlanId <= VLAN_ID_MAX)) {
                log.error("Value of VLAN id is out of range '" + vlanId + "' [" + VLAN_ID_MIN + "< vlanId <= " +
                                  "" + VLAN_ID_MAX + "] !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "VLAN ID \"" + vlanId + "\" is out of range <" +
                                         VLAN_ID_MIN + "," + VLAN_ID_MAX + "> !");
            }
        }

        Integer vlanPrio = selector.getVlanPrio();
        if (null != vlanPrio) {
            if (!(vlanPrio >= 0 && vlanPrio <= 8)) {
                log.error("Value of VLAN Priority is out of range '" + vlanId + "' [" + 0 + "< vlanId <= " +
                                  "" + 8 + "] !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "VLAN Prio \"" + vlanPrio + "\" is out of range <" +
                                         VLAN_PRIO_MIN + "," + VLAN_PRIO_MAX + "> !");
            }
        }
        log.info("EthSelector successfully validated and resolved !");
        return selector;
    }
}

