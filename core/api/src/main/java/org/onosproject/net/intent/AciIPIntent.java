/*
 * Copyright 2016 Open Networking Laboratory
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

package org.onosproject.net.intent;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.Path;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import java.util.List;

/**
 * Abstraction of explicitly path specified connectivity intent.
 */
@Beta
public class AciIPIntent extends ConnectivityIntent {

    private final Path path;

    /**
     * Creates a new point-to-point intent with the supplied ingress/egress
     * ports and using the specified explicit path.
     *
     * @param appId       application identifier
     * @param key         intent key
     * @param selector    traffic selector
     * @param treatment   treatment
     * @param path        traversed links
     * @param constraints optional list of constraints
     * @param priority    priority to use for the generated flows
     * @throws NullPointerException {@code path} is null
     */
    protected AciIPIntent(ApplicationId appId,
                          Key key,
                          TrafficSelector selector,
                          TrafficTreatment treatment,
                          Path path,
                          List<Constraint> constraints,
                          int priority) {
        super(appId, key, resources(path.links()), selector, treatment, constraints,
              priority);
        this.path = path;
    }

    /**
     * Constructor for serializer.
     */
    protected AciIPIntent() {
        super();
        this.path = null;
    }

    /**
     * Returns a new host to host intent builder.
     *
     * @return host to host intent builder
     */
    public static Builder builder() {
        return new Builder();
    }


    // NOTE: This methods takes linear time with the number of links.

    /**
     * Returns the links which the traffic goes along.
     *
     * @return traversed links
     */
    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id())
                .add("key", key())
                .add("appId", appId())
                .add("priority", priority())
                .add("resources", resources())
                .add("selector", selector())
                .add("treatment", treatment())
                .add("constraints", constraints())
                .add("path", path)
                .toString();
    }

    /**
     * Builder of a host to host intent.
     */
    public static class Builder extends ConnectivityIntent.Builder {
        Path path;

        protected Builder() {
            // Hide default constructor
        }

        @Override
        public Builder appId(ApplicationId appId) {
            return (Builder) super.appId(appId);
        }

        @Override
        public Builder key(Key key) {
            return (Builder) super.key(key);
        }

        @Override
        public Builder selector(TrafficSelector selector) {
            return (Builder) super.selector(selector);
        }

        @Override
        public Builder treatment(TrafficTreatment treatment) {
            return (Builder) super.treatment(treatment);
        }

        @Override
        public Builder constraints(List<Constraint> constraints) {
            return (Builder) super.constraints(constraints);
        }

        @Override
        public Builder priority(int priority) {
            return (Builder) super.priority(priority);
        }

        /**
         * Sets the path of the intent that will be built.
         *
         * @param path path for the intent
         * @return this builder
         */
        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        /**
         * Builds a path intent from the accumulated parameters.
         *
         * @return point to point intent
         */
        public AciIPIntent build() {

            return new AciIPIntent(
                    appId,
                    key,
                    selector,
                    treatment,
                    path,
                    constraints,
                    priority
            );
        }
    }

}
