/*
 * Copyright 2016-present Open Networking Laboratory
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.Path;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * TODO.
 */
@Beta
public class ACIPPIntent extends ConnectivityIntent {

    private final ConnectPoint src;
    private final ConnectPoint dst;
    private final Path path;
    private final Path backupPath;
    private final Boolean calculated;

    /**
     * Creates a new point-to-point intent with the supplied ingress/egress
     * ports and using the specified explicit path.
     *
     * @param appId       application identifier
     * @param selector    traffic selector
     * @param treatment   treatment
     * @param src
     * @param dst
     * @param path        traversed links
     * @param backupPath  backup traversed links
     * @param constraints optional list of constraints
     * @param priority    priority to use for the generated flows
     * @throws NullPointerException {@code src} {@code dst} are null
     */

    private ACIPPIntent(ApplicationId appId, Key key,
                        ConnectPoint src, ConnectPoint dst,
                        TrafficSelector selector,
                        TrafficTreatment treatment,
                        Path path, Path backupPath, List<Constraint> constraints,
                        int priority, Boolean calculated) {
        super(appId, key, resources(path), selector, treatment, constraints,
              priority);
        validate(path);
        this.path = path;
        this.src = checkNotNull(src);
        this.dst = checkNotNull(dst);
        this.calculated = calculated == null ? false : calculated;
        this.backupPath = backupPath;
    }

    /**
     * Constructor for serializer.
     */
    protected ACIPPIntent() {
        super();
        this.src = null;
        this.dst = null;
        this.path = null;
        this.backupPath = null;
        this.calculated = false;
    }

    /**
     * Returns a new host to host intent builder
     *
     * @return host to host intent builder
     */
    public static ACIPPIntent.Builder builder() {
        return new Builder();
    }

    /**
     * ACIPPIntent.Builder of a host to host intent.
     */
    public static class Builder extends ConnectivityIntent.Builder {
        ConnectPoint src;
        ConnectPoint dst;
        Path path;
        Path backupPath;
        Boolean calculated;

        protected Builder() {
            // Hide default constructor
        }

        // Copy existing ACIPPIntent, in order to update Path
        public Builder(ACIPPIntent old){
            this.appId = old.appId();
            this.key = old.key();
            this.resources = old.resources();
            this.priority = old.priority();
            this.selector = old.selector();
            this.treatment = old.treatment();
            this.constraints = old.constraints();
            this.src = old.src;
            this.dst = old.dst;
            this.path = old.path;
            this.backupPath = old.backupPath;
            this.calculated = old.calculated;
        }

        @Override
        public ACIPPIntent.Builder key(Key key) {
            return (ACIPPIntent.Builder) super.key(key);
        }

        @Override
        public ACIPPIntent.Builder appId(ApplicationId appId) {
            return (ACIPPIntent.Builder) super.appId(appId);
        }

        @Override
        public ACIPPIntent.Builder selector(TrafficSelector selector) {
            return (ACIPPIntent.Builder) super.selector(selector);
        }

        @Override
        public ACIPPIntent.Builder treatment(TrafficTreatment treatment) {
            return (ACIPPIntent.Builder) super.treatment(treatment);
        }

        @Override
        public ACIPPIntent.Builder constraints(List<Constraint> constraints) {
            return (ACIPPIntent.Builder) super.constraints(constraints);
        }

        @Override
        public ACIPPIntent.Builder priority(int priority) {
            return (ACIPPIntent.Builder) super.priority(priority);
        }

        public ACIPPIntent.Builder calculated (boolean calculated){
            this.calculated = calculated;
            return this;
        }
        /*
         public ACIPPIntent.Builder copyOf (boolean calculated){
            this.calculated = calculated;
            return this;
        }

        /**
         * Sets the src of the intent that will be built.
         *
         * @param src for the intent
         * @return this builder
         */
        public ACIPPIntent.Builder src(ConnectPoint src) {
            this.src = src;
            return this;
        }

        /**
         * Sets the dst of the intent that will be built.
         *
         * @param dst for the intent
         * @return this builder
         */
        public ACIPPIntent.Builder dst(ConnectPoint dst) {
            this.dst = dst;
            return this;
        }

        /**
         * Sets the path of the intent that will be built.
         *
         * @param path path for the intent
         * @return this builder
         */
        public ACIPPIntent.Builder path(Path path) {
            this.path = path;
            return this;
        }

        /**
         * Sets the path of the intent that will be built.
         *
         * @param path path for the intent
         * @return this builder
         */
        public ACIPPIntent.Builder backupPath(Path path) {
            this.backupPath = path;
            return this;
        }


        /**
         * Builds a path intent from the accumulated parameters.
         *
         * @return point to point intent
         */
        // TODO: Cannot set the IntentId here! :(
        public ACIPPIntent build() {
            return new ACIPPIntent(
                    appId,
                    key,
                    src,
                    dst,
                    selector,
                    treatment,
                    path,
                    backupPath,
                    constraints,
                    priority,
                    calculated
            );
        }
    }

    /**
     * Validates that source element ID and destination element ID of a link are
     * different for the specified all links and that destination element ID of a link and source
     * element ID of the next adjacent source element ID are same for the specified all links.
     *
     * @param path to be validated
     */
    public static void validate(Path path) {
        if (path != null) {
            List<Link> links = path.links();
            if (!links.isEmpty()) {
                checkArgument(Iterables.all(links, link -> !link.src().elementId().equals(link.dst().elementId())),
                              "element of src and dst in a link must be different: {}", links);

                boolean adjacentSame = true;
                for (int i = 0; i < links.size() - 1; i++) {
                    if (!links.get(i).dst().elementId().equals(links.get(i + 1).src().elementId())) {
                        adjacentSame = false;
                        break;
                    }
                }
                checkArgument(adjacentSame, "adjacent links must share the same element: {}", links);
            }
        }
    }

    /**
     * Returns the source connection point.
     *
     * @return source connection point
     */
    public ConnectPoint src() {
        return src;
    }

    /**
     * Returns the destination connection point.
     *
     * @return destination connection point
     */
    public ConnectPoint dst() {
        return dst;
    }

    /**
     * Returns the links which the traffic goes along.
     *
     * @return traversed links
     */
    public Path path() {
        return path;
    }

    /**
     * Returns the links which the traffic goes along.
     *
     * @return traversed links
     */
    public Path backupPath() {
        return backupPath;
    }

    /**
     * Return the calculation status
     *
     * @return if the intent has been in the PCE or not
     */
    public Boolean calculated() {
        if (calculated == null)
            return Boolean.FALSE;
        
        return calculated;
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
                .add("src", src)
                .add("dst", dst)
                .add("path", path)
                .add("backupPath", backupPath)
                .add("calculated", calculated)
                .toString();
    }

    /**
     * Produces a collection of network resources from the given a path.
     *
     * @param path a path
     * @return collection of link resources
     */
    protected static Collection<NetworkResource> resources(Path path) {
        if (path == null) {
            return ImmutableList.of();
        }
        return ImmutableSet.copyOf(path.links());
    }
}

