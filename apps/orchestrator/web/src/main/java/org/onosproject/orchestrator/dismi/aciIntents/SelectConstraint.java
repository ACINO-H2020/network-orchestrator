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

package org.onosproject.orchestrator.dismi.aciIntents;

import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.constraint.AvailabilityConstraint;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.EncryptionConstraint;
import org.onosproject.net.intent.constraint.HighAvailabilityConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by aghafoor on 2017-05-03.
 */
public class SelectConstraint {
    private List<Constraint> srcConstraints;
    private List<Constraint> dstConstraints;
    private List<Constraint> intentConstraints;

    public SelectConstraint(List<Constraint> srcConstraints, List<Constraint> dstConstraints, List<Constraint>
            intentConstraints) {
        this.srcConstraints = srcConstraints;
        this.dstConstraints = dstConstraints;
        this.intentConstraints = intentConstraints;
    }

    public List<Constraint> select(int bwLevel, int delayLevel, int securityLevel) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        List<BandwidthConstraint> bw = new ArrayList<BandwidthConstraint>();
        List<LatencyConstraint> latency = new ArrayList<LatencyConstraint>();
        List<EncryptionConstraint> security = new ArrayList<EncryptionConstraint>();
        List<HighAvailabilityConstraint> highAvailabilityConstraint = new ArrayList<HighAvailabilityConstraint>();
        List<AvailabilityConstraint> vailabilityConstraint = new ArrayList<AvailabilityConstraint>();

        // Create separate storage for each constraints for further processing
        // Add constraints associated with source
        for (Constraint constraint : this.srcConstraints) {
            if (constraint instanceof BandwidthConstraint) {
                bw.add((BandwidthConstraint) constraint);
            } else if (constraint instanceof LatencyConstraint) {
                latency.add((LatencyConstraint) constraint);
            } else if (constraint instanceof EncryptionConstraint) {
                security.add((EncryptionConstraint) constraint);
            } else if (constraint instanceof HighAvailabilityConstraint) {
                highAvailabilityConstraint.add((HighAvailabilityConstraint) constraint);
            } else if (constraint instanceof AvailabilityConstraint) {
                vailabilityConstraint.add((AvailabilityConstraint) constraint);
            }

        }
        // Add constraints associated with destination
        for (Constraint constraint : this.dstConstraints) {
            if (constraint instanceof BandwidthConstraint) {
                bw.add((BandwidthConstraint) constraint);
            } else if (constraint instanceof LatencyConstraint) {
                latency.add((LatencyConstraint) constraint);
            } else if (constraint instanceof EncryptionConstraint) {
                security.add((EncryptionConstraint) constraint);
            } else if (constraint instanceof HighAvailabilityConstraint) {
                highAvailabilityConstraint.add((HighAvailabilityConstraint) constraint);
            } else if (constraint instanceof AvailabilityConstraint) {
                vailabilityConstraint.add((AvailabilityConstraint) constraint);
            }
        }
        // Add constraints associated with intents
        for (Constraint constraint : this.intentConstraints) {
            if (constraint instanceof BandwidthConstraint) {
                bw.add((BandwidthConstraint) constraint);
            } else if (constraint instanceof LatencyConstraint) {
                latency.add((LatencyConstraint) constraint);
            } else if (constraint instanceof EncryptionConstraint) {
                security.add((EncryptionConstraint) constraint);
            } else if (constraint instanceof HighAvailabilityConstraint) {
                highAvailabilityConstraint.add((HighAvailabilityConstraint) constraint);
            } else if (constraint instanceof AvailabilityConstraint) {
                vailabilityConstraint.add((AvailabilityConstraint) constraint);
            }
        }
        // Bandwidth should be in descending order.
        Collections.sort(bw, new BandwidthComparator());
        if (bw.size() > bwLevel) {
            constraints.add(bw.get(bwLevel));
        }
        // Latency should be ascending order.
        Collections.sort(latency, Collections.reverseOrder(new LatencyComparator()));
        if (latency.size() > delayLevel) {
            constraints.add(latency.get(delayLevel));
        }
        if (security.size() > 0) {
            constraints.add(security.get(0));
        }
        if (highAvailabilityConstraint.size() > 0) {
            constraints.add(highAvailabilityConstraint.get(0));
        }
        if (vailabilityConstraint.size() > 0) {
            constraints.add(vailabilityConstraint.get(0));
        }
        // Apply levels
        return constraints;
    }

    class BandwidthComparator implements Comparator<BandwidthConstraint> {
        @Override
        public int compare(BandwidthConstraint first, BandwidthConstraint second) {
            return first.bandwidth().compareTo(second.bandwidth());
        }
    }

    class LatencyComparator implements Comparator<LatencyConstraint> {
        @Override
        public int compare(LatencyConstraint first, LatencyConstraint second) {
            return first.latency().compareTo(second.latency());
        }
    }
}
