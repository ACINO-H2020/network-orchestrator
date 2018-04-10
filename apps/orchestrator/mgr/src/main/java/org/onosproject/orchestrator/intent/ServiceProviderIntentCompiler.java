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

package org.onosproject.orchestrator.intent;

import com.google.common.collect.Lists;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.util.Bandwidth;
import org.onosproject.net.AnnotationKeys;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.Path;
import org.onosproject.net.intent.ConnectivityIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.constraint.BandwidthConstraint;
import org.onosproject.net.intent.constraint.LatencyConstraint;
import org.onosproject.net.resource.ContinuousResource;
import org.onosproject.net.resource.DiscreteResource;
import org.onosproject.net.resource.Resource;
import org.onosproject.net.resource.ResourceAllocation;
import org.onosproject.net.resource.ResourceConsumer;
import org.onosproject.net.resource.ResourceId;
import org.onosproject.net.resource.ResourceService;
import org.onosproject.net.resource.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.onosproject.net.AnnotationKeys.getAnnotatedValue;

@Component(immediate = true)
public abstract class ServiceProviderIntentCompiler<T extends ConnectivityIntent>
        implements IntentCompiler<T> {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected ResourceService resourceService;

    private static final Logger log = LoggerFactory.getLogger(ServiceProviderIntentCompiler.class);

    protected boolean portCheck(Path path, List<ConnectPoint> allowedPorts) {

        return path.links().stream()
                .anyMatch(link -> (allowedPorts.contains(link.src()) || allowedPorts.contains(link.dst())));
    }

    /**
     * Validates the specified path against the given constraints.
     *
     * @param path        path to be checked
     * @param constraints path constraints
     * @return true if the path passes all constraints
     */
    protected boolean checkPath(Path path, List<Constraint> constraints) {

        for (Constraint constraint : constraints) {
            if (!constraint.validate(path, resourceService::isAvailable)) {
                return false;
            }
        }
        return true;
    }

    protected List<Constraint> supportedPathConstraints(Path path, List<Constraint> initialConstraints) {
        List<ConnectPoint> pathCPs =
                path.links().stream()
                        .flatMap(l -> Stream.of(l.src(), l.dst()))
                        .collect(Collectors.toList());

        double bandwidthLeft = 0;
        double currentBandwidth = 0;

        for (ConnectPoint point : pathCPs) {
            if (point.elementId() instanceof DeviceId) {
                currentBandwidth = leftResources(point);

                if (currentBandwidth == 0) {
                    bandwidthLeft = 0;
                    break;
                }

                if (bandwidthLeft == 0) {
                    bandwidthLeft = currentBandwidth;
                }

                if (currentBandwidth < bandwidthLeft) {
                    bandwidthLeft = currentBandwidth;
                }
            }
        }

        Bandwidth bw = Bandwidth.bps(bandwidthLeft);
        BandwidthConstraint bwConstraint = new BandwidthConstraint(bw);

        double pathLatency = path.links().stream().mapToDouble(this::cost).sum();

        LatencyConstraint latencyConstraint = new LatencyConstraint(Duration.of((long) pathLatency, ChronoUnit.NANOS));

        List<Constraint> pathConstraints = Lists.newArrayList(bwConstraint, latencyConstraint);

        return pathConstraints.stream()
                .filter(pathConstraint -> {
                    for (Constraint c : initialConstraints) {
                        if ((pathConstraint instanceof BandwidthConstraint) &&
                                (c instanceof BandwidthConstraint)) {
                            return true;
                        }
                        if ((pathConstraint instanceof LatencyConstraint) &&
                                (c instanceof LatencyConstraint)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(pathConstraint -> {
                    for (Constraint initialConst : initialConstraints) {
                        if ((pathConstraint instanceof BandwidthConstraint) &&
                                (initialConst instanceof BandwidthConstraint)) {
                            if (((BandwidthConstraint) pathConstraint).bandwidth()
                                    .isGreaterThan(((BandwidthConstraint) initialConst).bandwidth())) {
                                return initialConst;
                            }
                        }
                        if ((pathConstraint instanceof LatencyConstraint) &&
                                (initialConst instanceof LatencyConstraint)) {
                            if (((LatencyConstraint) pathConstraint).latency()
                                    .compareTo(((LatencyConstraint) initialConst).latency()) <= 0) {
                                return initialConst;
                            }
                        }
                    }
                    return pathConstraint;
                })
                .collect(Collectors.toList());
    }

    private double cost(Link link) {
        //Check only links, not EdgeLinks
        if (link.type() != Link.Type.EDGE) {
            return link.annotations().value(AnnotationKeys.LATENCY) != null
                    ? getAnnotatedValue(link, AnnotationKeys.LATENCY) : 0;
        } else {
            return 0;
        }
    }

    /**
     * Returns the amount of resources left with respect to the current allocation
     *
     * @param connectPoint requested resource
     * @return true if there is enough resource volume. Otherwise, false.
     */
    // computational complexity: O(n) where n is the number of allocations
    private double leftResources(ConnectPoint connectPoint) {

        DiscreteResource resource = Resources.discrete(connectPoint.deviceId(), connectPoint.port()).resource();

        Set<Resource> resourceValues = resourceService.getRegisteredResources(resource.id());

        double original = resourceValues.stream()
                .filter(x -> x instanceof ContinuousResource)
                .map(x -> (ContinuousResource) x)
                .mapToDouble(ContinuousResource::value)
                .sum();

        Collection<ResourceAllocation> resourceAllocations =
                resourceService.getResourceAllocations(resource.id(), Bandwidth.class);

        double allocated = resourceAllocations.stream()
                .filter(x -> x.resource() instanceof ContinuousResource)
                .map(x -> (ContinuousResource) x.resource())
                .mapToDouble(ContinuousResource::value)
                .sum();

        return original - allocated;
    }

    /**
     * Allocates the bandwidth specified as intent constraint on each link
     * composing the intent, if a bandwidth constraint is specified.
     *
     * @param intent        the intent requesting bandwidth allocation
     * @param connectPoints the connect points composing the intent path computed
     */
    protected void allocateBandwidth(ConnectivityIntent intent,
                                     List<ConnectPoint> connectPoints) {
        // Retrieve bandwidth constraint if exists
        List<Constraint> constraints = intent.constraints();

        if (constraints == null) {
            return;
        }

        Optional<Constraint> constraint =
                constraints.stream()
                        .filter(c -> c instanceof BandwidthConstraint)
                        .findAny();

        // If there is no bandwidth constraint continue
        if (!constraint.isPresent()) {
            return;
        }

        BandwidthConstraint bwConstraint = (BandwidthConstraint) constraint.get();

        double bw = bwConstraint.bandwidth().bps();

        // If a resource group is set on the intent, the resource consumer is
        // set equal to it. Otherwise it's set to the intent key
        ResourceConsumer newResourceConsumer =
                intent.resourceGroup() != null ? intent.resourceGroup() : intent.key();

        // Get the list of current resource allocations
        Collection<ResourceAllocation> resourceAllocations =
                resourceService.getResourceAllocations(newResourceConsumer);

        // Get the list of resources already allocated from resource allocations
        List<Resource> resourcesAllocated =
                resourcesFromAllocations(resourceAllocations);

        // Get the list of resource ids for resources already allocated
        List<ResourceId> idsResourcesAllocated = resourceIds(resourcesAllocated);

        // Create the list of incoming resources requested. Exclude resources
        // already allocated.
        List<Resource> incomingResources =
                resources(connectPoints, bw).stream()
                        .filter(r -> !resourcesAllocated.contains(r))
                        .collect(Collectors.toList());

        if (incomingResources.isEmpty()) {
            return;
        }

        // Create the list of resources to be added, meaning their key is not
        // present in the resources already allocated
        List<Resource> resourcesToAdd =
                incomingResources.stream()
                        .filter(r -> !idsResourcesAllocated.contains(r.id()))
                        .collect(Collectors.toList());

        // Resources to updated are all the new valid resources except the
        // resources to be added
        List<Resource> resourcesToUpdate = Lists.newArrayList(incomingResources);
        resourcesToUpdate.removeAll(resourcesToAdd);

        // If there are no resources to update skip update procedures
        if (!resourcesToUpdate.isEmpty()) {
            // Remove old resources that need to be updated
            // TODO: use transaction updates when available in the resource service
            List<ResourceAllocation> resourceAllocationsToUpdate =
                    resourceAllocations.stream()
                            .filter(rA -> resourceIds(resourcesToUpdate).contains(rA.resource().id()))
                            .collect(Collectors.toList());
            log.debug("Releasing bandwidth for intent {}: {} bps", newResourceConsumer, resourcesToUpdate);
            resourceService.release(resourceAllocationsToUpdate);

            // Update resourcesToAdd with the list of both the new resources and
            // the resources to update
            resourcesToAdd.addAll(resourcesToUpdate);
        }

        // Look also for resources allocated using the intent key and -if any-
        // remove them
        if (intent.resourceGroup() != null) {
            // Get the list of current resource allocations made by intent key
            Collection<ResourceAllocation> resourceAllocationsByKey =
                    resourceService.getResourceAllocations(intent.key());

            resourceService.release(Lists.newArrayList(resourceAllocationsByKey));
        }

        // Allocate resources
        log.debug("Allocating bandwidth for intent {}: {} bps", newResourceConsumer, resourcesToAdd);
        List<ResourceAllocation> allocations =
                resourceService.allocate(newResourceConsumer, resourcesToAdd);

        if (allocations.isEmpty()) {
            log.debug("No resources allocated for intent {}", newResourceConsumer);
        }

        log.debug("Done allocating bandwidth for intent {}", newResourceConsumer);
    }

    /**
     * Produces a list of resources from a list of resource allocations.
     *
     * @param rAs the list of resource allocations
     * @return a list of resources retrieved from the resource allocations given
     */
    private static List<Resource> resourcesFromAllocations(Collection<ResourceAllocation> rAs) {
        return rAs.stream()
                .map(ResourceAllocation::resource)
                .collect(Collectors.toList());
    }

    /**
     * Creates a list of continuous bandwidth resources given a list of connect
     * points and a bandwidth.
     *
     * @param cps the list of connect points
     * @param bw  the bandwidth expressed as a double
     * @return the list of resources
     */
    private static List<Resource> resources(List<ConnectPoint> cps, double bw) {
        return cps.stream()
                // Make sure the element id is a valid device id
                .filter(cp -> cp.elementId() instanceof DeviceId)
                // Create a continuous resource for each CP we're going through
                .map(cp -> Resources.continuous(cp.deviceId(), cp.port(),
                                                Bandwidth.class).resource(bw))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of resource ids given a list of resources.
     *
     * @param resources the list of resources
     * @return the list of resource ids retrieved from the resources given
     */
    private static List<ResourceId> resourceIds(List<Resource> resources) {
        return resources.stream()
                .map(Resource::id)
                .collect(Collectors.toList());
    }
}
