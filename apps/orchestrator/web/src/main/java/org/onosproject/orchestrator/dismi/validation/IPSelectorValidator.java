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

import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IPAddress;
import org.onosproject.orchestrator.dismi.primitives.extended.IPSelectorExtended;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class IPSelectorValidator extends FieldValidator {
    String className = "IPSelector";
    /*
        The IP Selector has the following member variables:

          private String ipSrcAddr = null;b "a.b.c.d/e.f.g.h" or "a.b.c.d/x"
          private String ipDestAddr = null;
          private IpProtocolEnum ipProtocol = IpProtocolEnum.ALL;
          private String ipTos = null;
          private String ipDscp = null;
          private String selector = "IPSelector";


     */
    private final Logger log = getLogger(getClass());
    private static final String DSCP_KEY = "IP_DSCP";
    private static final String SEPARATOR = ",";

    @Override
    public Object validateAndResolve(Object field, Tracker tracker) {
        IPSelector selector = null;
        log.info("Validating and resolving IPSelector !");
        if (null == field) {
            log.error("Instance of IPSelector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "field");
            return null;
        }
        if (!(field instanceof IPSelector)) {
            log.error("Invalid instance of IPSelector !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.OBJECTISNOTANIPSELECTOR,
                             "field");
            return null;
        }

        selector = (IPSelector) field;

        if (null == tracker) {
            log.error("Tracker of IPSelector is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "tracker");
            return null;
        }

        Object object = validateAndResolve(selector, tracker);
        log.error("IPSelector selector validated and resolved !");
        return object;
    }

    private IPSelector validateAndResolve(IPSelector selector, Tracker tracker) {
        // IPSelectorExtended ipSelectorExtended = new IPSelectorExtended();
        IPSelectorExtended ipSelectorExtended = new IPSelectorExtended(selector);
        boolean bResult;

        /****************************************************
         *
         *  The parameters below are all OPTIONAL, as at least one,
         *  the ipProtocol, is specified by construction (it is an enum)
         *
         */

        // The IP source is OPTIONAL
        String ipsrc = selector.getIpSrcAddr();
        if (null != ipsrc) {
            IPAddress sourceAddress = new IPAddress();
            bResult = sourceAddress.setAddressAndMask(ipsrc);
            if (bResult) {
                ipSelectorExtended.setSourceAddressExt(sourceAddress);
            } else {
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "Invalid source ip \"" + ipsrc + "\" !");
            }
        } else {
            log.error("Invalid source ip \"" + ipsrc + "\" !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "Invalid source ip \"" + ipsrc + "\" !");
        }

        // The IP destination is OPTIONAL
        String ipdest = selector.getIpDestAddr();
        if (null != ipdest) {
            IPAddress destinationAddress = new IPAddress();
            bResult = destinationAddress.setAddressAndMask(ipdest);
            if (bResult) {
                ipSelectorExtended.setDestinationAddressExt(destinationAddress);

            } else {
                log.error("Invalid destination ip \"" + ipdest + "\" !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.INVALIDVALUE,
                                 "Invalid destination ip \"" + ipdest + "\" !");
            }
        } else {
            log.error("*Invalid destination ip \"" + ipdest + "\" !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "Invalid destination ip \"" + ipdest + "\" !");
        }
        // The IP TOS is OPTIONAL, and we haven't implemented a validation & resolve for it yet :-(
        String tos = selector.getIpTos();
        if (null != tos) {
            log.warn("IP-TOS specified but not supported yet [\"" + tos + "\"].");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.NOT_SUPPORTED,
                             "IP-TOS specified but not supported yet [\"" + tos + "\"].");
        } else {
            log.warn("Not Supported: IP-TOS = [\"" + tos + "\"].");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.INVALIDVALUE,
                             "IP-TOS = [\"" + tos + "\"].");
        }
        // The IP DSCP is OPTIONAL
        String dscp = selector.getIpDscp();

        if (null != dscp) {
            List<String> dscpList = getDscpList(dscp, tracker);
            if (dscpList.size() > 0) {
                ipSelectorExtended.setIPDscpListExt(dscpList);
            } else {
                log.warn("IP-DSCP list if empty !");
            }
        } else {
            log.warn("IP-DSCP is NULL !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.WARNING,
                             Issue.ErrorTypeEnum.ERRORUNDEFINED,
                             "IP-DSCP is NULL ");
        }
        return ipSelectorExtended;
    }

    List<String> getDscpList(String str, Tracker tracker) {
        List<String> dscpList = new ArrayList<>();
        String[] list = str.split(SEPARATOR);
        String dscpWord;
        Integer dscpValue;
        int i;

        SelectorVocabulary vocabulary = new SelectorVocabulary();
        log.info("Resolving IP-DSCP value !");
        for (i = 0; i < list.length; i++) {
            dscpWord = list[i];
            // with DSCP_KEY it was throwing exception
            dscpValue = vocabulary.get(SelectorVocabulary.IPDSCP).get(dscpWord);
            if (null != dscpValue) {
                dscpList.add(dscpWord);
            } else {
                log.warn("IP-DSCP value \"" + dscpWord + "\" not recognized !");
                tracker.addIssue(className,
                                 Issue.SeverityEnum.ERROR,
                                 Issue.ErrorTypeEnum.ERRORUNDEFINED,
                                 "IP-DSCP value \"" + dscpWord + "\" not recognized !");
            }
        }
        return dscpList;
    }
}

