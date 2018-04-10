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

package org.onosproject.orchestrator.dismi.negotiation;

import org.onlab.osgi.DefaultServiceDirectory;
import org.onlab.util.Bandwidth;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.ServiceProviderIntent;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.EncryptionConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.ServerInfo;
import org.onosproject.orchestrator.dismi.primitives.Service;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by aghafoor on 2017-06-01.
 */
public class AciToDismiComposer /*extends AbstractWebResource*/ {

    public void generateAlternativeSolutionsIntent(ACIPPIntent ACIPPIntent) {
    }

    /**
     * Description: It accepts aciIntent which is received during negotiation event listening block. We assumed that
     * the alternative solutions already ctraeted and palced in the Bucket by the negotiation plugin.
     *
     * @param dismiIntentId
     * @param aciIntents
     * @return
     */
    public Set<Intent> generateAlternativeSolutionsIntent(DismiIntentId dismiIntentId, List<org.onosproject.net
            .intent.Intent> aciIntents) {
        NegoUtils negoUtils = new NegoUtils();
        // Find dismi intent id which is used to find associated keys
        if (null == dismiIntentId) {
            return null;
        }
        // Store newly created alternative solutions (dismi-intent) into composedDismiIntent
        Set<Intent> composedDismiIntent = new HashSet<>();
        // Convert (AciIntent) alternative solutions into DismiIntent
        int no = 1;
        for (org.onosproject.net.intent.Intent aciIntent_ : aciIntents) {

            if (aciIntent_ instanceof AciIntent) {
                AciIntent aciIntentAS = (AciIntent) aciIntent_;
                Intent temp = createDismiIntent(no, dismiIntentId, aciIntentAS);
                composedDismiIntent.add(temp);
            } else if (aciIntent_ instanceof ServiceProviderIntent) {
                ServiceProviderIntent aciIntentAS = (ServiceProviderIntent) aciIntent_;
                Intent temp = createDismiIntent(no, dismiIntentId, aciIntentAS);
                composedDismiIntent.add(temp);
            }
            no++;

        }
        return composedDismiIntent;
    }

    /**
     * @param no            // since all intents have same dismi-intent-id so no is used to uniquely identify them
     * @param dismiIntentId
     * @param aciIntent
     * @return
     */
    private Intent createDismiIntent(int no, DismiIntentId dismiIntentId, ConnectivityIntent aciIntent) {
        NegoUtils negoUtils = new NegoUtils(); // Utils file to provide utility functions for negotiation
        Intent intent = new Intent();
        String serviceId = negoUtils.extractServiceId(dismiIntentId.id());
        // Get Original Intents from storage
        DismiStoreIface dismiStoreIface = DefaultServiceDirectory.getService(DismiStoreIface.class);
        Service service = dismiStoreIface.getResolvedService(serviceId);
        List<Intent> intents = service.getIntents();
        Intent originalIntent = null;
        for (Intent temp : intents) {
            if (temp.getIntentId().equals(dismiIntentId.id())) {
                originalIntent = temp;
                break;
            }
        }
        if (null == originalIntent) {
            return null;
        }
        // Following items will remain same so just coppied from original
        ServerInfo serverInfo = new ServerInfo();
        intent.setServerInfo(serverInfo);
        if (aciIntent instanceof ServiceProviderIntent) {
            intent.setIntentId(negoUtils.toNewDismiId(no, aciIntent.key().toString()));
            intent.setIntentServiceProviderKey(aciIntent.key().toString());
            intent.setProviderName(aciIntent.getClass().getSimpleName());
        } else {
            intent.setIntentId(negoUtils.toNewDismiId(no, originalIntent.getIntentId()));
        }
        intent.setDisplayName(originalIntent.getDisplayName());
        intent.setCalendaring(originalIntent.getCalendaring());
        intent.setPriorities(originalIntent.getPriorities());
        intent.setIsNegotiatable(false);
        intent = negoUtils.copyEndpoints(originalIntent, intent);
        List<org.onosproject.orchestrator.dismi.primitives.Constraint> constraintList = constraintResolver(aciIntent
                                                                                                                   .constraints());
        intent.setConstraints(constraintList);
        return intent;
    }

    /**
     * @param constraints
     * @return
     */
    private List<org.onosproject.orchestrator.dismi.primitives.Constraint> constraintResolver(List<Constraint> constraints) {
        List<org.onosproject.orchestrator.dismi.primitives.Constraint> dismiConstraints = new ArrayList<org.onosproject.orchestrator.dismi.primitives.Constraint>();
        boolean isHighAvailabilityConstraintExists = false;

        // Create separate storage for each constraints for further processing
        // Add constraints associated with source
        for (Constraint constraint : constraints) {
            if (constraint instanceof BandwidthConstraint) {
                BandwidthConstraint bandwidthConstraint = (BandwidthConstraint) constraint;
                Bandwidth bandwidth = bandwidthConstraint.bandwidth();
                org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint dismiBwConstraint = new
                        org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint();
                String formatted = new BigDecimal(Double.valueOf(bandwidth.bps())).toString();
                dismiBwConstraint.setBitrate(formatted + " bps");
                dismiConstraints.add(dismiBwConstraint);
            } else if (constraint instanceof LatencyConstraint) {
                LatencyConstraint latency = (LatencyConstraint) constraint;
                org.onosproject.orchestrator.dismi.primitives.DelayConstraint delayConstraint = new org.onosproject.orchestrator.dismi.primitives.DelayConstraint();
                //Duration.of(5, ChronoUnit.MILLIS).toMillis()
                delayConstraint.setLatency(latency.latency().toMillis() + "ms");
                dismiConstraints.add(delayConstraint);
            } else if (constraint instanceof EncryptionConstraint) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setEncryption(new Boolean(true));
                dismiConstraints.add(securityConstraint);
            } else if (constraint instanceof org.onosproject.net.intent.constraint.HighAvailabilityConstraint) {
                HighAvailabilityConstraint highAvailabilityConstraint = new HighAvailabilityConstraint();
                highAvailabilityConstraint.setAvailability(true);
                if (isHighAvailabilityConstraintExists == false) {
                    dismiConstraints.add(highAvailabilityConstraint);
                }
                isHighAvailabilityConstraintExists = true;
            }
        }
        if (isHighAvailabilityConstraintExists = false) {
            HighAvailabilityConstraint highAvailabilityConstraint = new HighAvailabilityConstraint();
            highAvailabilityConstraint.setAvailability(false);
            dismiConstraints.add(highAvailabilityConstraint);
        }
        return dismiConstraints;
    }

    /**
     * @param selectors
     * @return
     */
    private List<org.onosproject.orchestrator.dismi.primitives.Selector> selectorResolver(TrafficSelector
                                                                                                  selectors) {
        List<org.onosproject.orchestrator.dismi.primitives.Selector> dismiSelectors = new ArrayList<Selector>();
        //ToDo: Resolve here
        return dismiSelectors;
    }
}
