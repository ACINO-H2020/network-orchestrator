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

import org.onlab.util.Bandwidth;
import org.onosproject.net.intent.constraint.EncryptionConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;
import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.extended.AvailabilityConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.BandwidthConstraintExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.DelayConstraintExtended;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class ConstraintCompiler {
    private final Logger log = getLogger(getClass());
    private List<Constraint> dismiConstraints = null;

    public ConstraintCompiler(List<Constraint> dismiConstraints) {
        this.dismiConstraints = dismiConstraints;
    }

    public List<org.onosproject.net.intent.Constraint> toAciConstraints() {
        //log.info("Converting DISMI constraints to ACI constraints !");
        final List<org.onosproject.net.intent.Constraint> constraints = new LinkedList<>();
        for (Constraint dismiConstraint : dismiConstraints) {
            if (dismiConstraint instanceof BandwidthConstraintExtended) {
                log.info("Converting DISMI BandwidthConstraint to ACI Bandwidth constraint !");
                BandwidthConstraintExtended bandwidthConstraintExtended = (BandwidthConstraintExtended) dismiConstraint;
                // Check for a bandwidth specification
                Bandwidth bandwidth = Bandwidth.bps(bandwidthConstraintExtended.getBitrateExt());
                constraints.add(new org.onosproject.net.intent.constraint.BandwidthConstraint(bandwidth));
            }
            // Check for a latency specification
            else if (dismiConstraint instanceof DelayConstraintExtended) {
                log.info("Converting DISMI DelayConstraint to ACI LatencyConstraint constraint !");
                LatencyConstraint latency;
                DelayConstraintExtended delayConstraintExtended = (DelayConstraintExtended) dismiConstraint;
                // delayConstraintExtended.getLatencyExt returns double value which represents duration in seconds
                // since LatencyConstraint accepts duration in millis therefore we multiplied LatencyExt with 1000
                // for conversion.
                long inMillis = Math.round(delayConstraintExtended.getLatencyExt().doubleValue() * 1000);
                latency = new LatencyConstraint(Duration.ofMillis(inMillis));
                constraints.add(latency);
            } else if (dismiConstraint instanceof SecurityConstraint) {
                log.info("Converting DISMI SecurityConstraint to ACI EncryptionConstraint constraint !");
                SecurityConstraint securityConstraint = (SecurityConstraint) dismiConstraint;
                if (securityConstraint.getEncryption()) {
                    constraints.add(new EncryptionConstraint());
                }
            } else if (dismiConstraint instanceof HighAvailabilityConstraint) {
                log.info("Converting DISMI HighAvailabilityConstraint to ACI HighAvailabilityConstraint constraint !");
                HighAvailabilityConstraint highAvailabilityConstraint = (HighAvailabilityConstraint) dismiConstraint;
                if (highAvailabilityConstraint.getAvailability()) {
                    org.onosproject.net.intent.constraint.HighAvailabilityConstraint highAvailabilityConstraintAci = new
                            org
                                    .onosproject.net.intent.constraint.HighAvailabilityConstraint();
                    constraints.add(highAvailabilityConstraintAci);
                }
            } else if (dismiConstraint instanceof AvailabilityConstraint) {
                log.info("Converting DISMI AvailabilityConstraint to ACI AvailabilityConstraint constraint !");
                AvailabilityConstraintExtended availabilityConstraintExtended = (AvailabilityConstraintExtended)
                        dismiConstraint;
                double availabilityExt = availabilityConstraintExtended.getAvailabilityExt();
                if (availabilityExt == 99.9999) {
                    org.onosproject.net.intent.constraint.HighAvailabilityConstraint highAvailabilityConstraintAci = new
                            org.onosproject.net.intent.constraint.HighAvailabilityConstraint();
                    constraints.add(highAvailabilityConstraintAci);
                } else {
                    org.onosproject.net.intent.constraint.AvailabilityConstraint availabilityConstraint = new org
                            .onosproject.net.intent.constraint.AvailabilityConstraint(availabilityExt);
                    constraints.add(availabilityConstraint);
                }
            }
        }
        return constraints;
    }
}
