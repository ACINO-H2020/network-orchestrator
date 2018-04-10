/*
 * Copyright 2014-present Open Networking Laboratory
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
package org.onosproject.net.intent.constraint;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.ResourceContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static org.onosproject.net.AnnotationKeys.LATENCY;
import static org.onosproject.net.AnnotationKeys.getAnnotatedValue;

/**
 * Constraint that evaluates the latency through a path.
 */
@Beta
public class AvailabilityConstraint implements Constraint {

    private final Double availabilityInPercentage;

    /**
     * Creates a new constraint to keep over the specified availability through a path.
     * @param availabilityInPercentage latency to be kept
     */
    public AvailabilityConstraint(Double availabilityInPercentage) {
        this.availabilityInPercentage = availabilityInPercentage;
    }

    // Constructor for serialization
    private AvailabilityConstraint() {
        this.availabilityInPercentage = 0.0;
    }

    public Double availability() {
        return availabilityInPercentage;
    }

    @Override
    public double cost(Link link, ResourceContext context) {
        return 0;
    }

    @Override
    public boolean validate(Path path, ResourceContext context) {
        // explicitly call a method not depending on LinkResourceService
        return true;
    }

    @Override
    public int hashCode() {
        return availabilityInPercentage.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AvailabilityConstraint)) {
            return false;
        }

        final AvailabilityConstraint that = (AvailabilityConstraint) obj;
        return Objects.equals(this.availabilityInPercentage, that.availabilityInPercentage);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("availability", availabilityInPercentage)
                .toString();
    }
}
