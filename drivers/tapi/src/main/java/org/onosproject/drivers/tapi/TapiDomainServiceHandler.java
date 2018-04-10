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

package org.onosproject.drivers.tapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.onlab.util.GuavaCollectors;
import org.onosproject.drivers.tapi.common.LayerProtocolName;
import org.onosproject.drivers.tapi.common.PortDirection;
import org.onosproject.drivers.tapi.common.PortRole;
import org.onosproject.drivers.tapi.common.UniversalId;
import org.onosproject.drivers.tapi.connectivity.ConnectivityConstraint;
import org.onosproject.drivers.tapi.connectivity.ConnectivityService;
import org.onosproject.drivers.tapi.connectivity.ConnectivityServiceEndPoint;
import org.onosproject.drivers.tapi.connectivity.TopologyConstraint;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.FilteredConnectPoint;
import org.onosproject.net.Port;
import org.onosproject.net.behaviour.DomainIntentConfigurable;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.domain.DomainIntent;
import org.onosproject.net.domain.DomainPointToPointIntent;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.protocol.http.HttpSBController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.onosproject.drivers.tapi.NodeEdgePointWrapper.UUID_TAG;

public class TapiDomainServiceHandler extends AbstractHandlerBehaviour
        implements DomainIntentConfigurable {

    private static final Logger LOG =
            LoggerFactory.getLogger(TapiDomainServiceHandler.class);
    // TAPI RPC call to create a connectivity service
    private static final String CREATE_CONNECTIVITY_REQUEST =
            "operations/tapi-connectivity%3Acreate-connectivity-service";
    // TAPI RPC call to delete a connectivity service
    private static final String DELETE_CONNECTIVITY_REQUEST =
            "operations/tapi-connectivity%3Adelete-connectivity-service";
    private static final ConnectivityConstraint EMPTY_CONNECTIVITY_CONSTRAINT =
            new ConnectivityConstraint(null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null,
                                       null);
    private static final TopologyConstraint EMPTY_TOPOLOGY_CONSTRAINT =
            new TopologyConstraint(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public synchronized DomainIntent sumbit(DomainIntent intent) {
        TapiIntentToTunnel intentToTunnel =
                handler().get(TapiIntentToTunnel.class);
        if (intentToTunnel.contains(intent)) {
            LOG.info("The intent ({}) is already installed.", intent.id());
            return intent;
        }
        Set<DomainIntent> sameKeyIntents = intentToTunnel.sameKey(intent);
        if (sameKeyIntents.size() > 0) {
            intentToTunnel.addTunnelIdentifier(intent,
                                               intentToTunnel.getTunnelIdentifier(
                                                       sameKeyIntents.iterator()
                                                               .next()));
            LOG.info("An Intent with the same key ({}) is already installed.",
                     intent.key());
            return intent;
        }
        // Currently only point to point is supported.
        Preconditions.checkArgument(intent.filteredIngressPoints().size() == 1);
        Preconditions.checkArgument(intent.filteredEgressPoints().size() == 1);
        ImmutableList.Builder<ConnectivityServiceEndPoint> builder =
                ImmutableList.builder();
        builder.add(retrieveServiceEndpoints(intent.filteredIngressPoints()).get(
                0));
        builder.add(retrieveServiceEndpoints(intent.filteredEgressPoints()).get(
                0));
        TopologyConstraint topoConstraint = EMPTY_TOPOLOGY_CONSTRAINT;
        if (intent instanceof DomainPointToPointIntent) {
            List<UniversalId> linksId = ((DomainPointToPointIntent) intent).links().stream()
                    .filter(x -> StringUtils.isNotEmpty(x.annotations().value(UUID_TAG)))
                    .map(x -> new UniversalId(x.annotations().value(UUID_TAG)))
                    .collect(Collectors.toList());
            topoConstraint = new TopologyConstraint(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    linksId,
                    null,
                    null
            );
        }
        CreateConnectivity request = new CreateConnectivity(builder.build(), EMPTY_CONNECTIVITY_CONSTRAINT, topoConstraint);
        LOG.info("Sending connectivity request for service ports ({}): {}",
                 intent.id(),
                 request.getServicePort()
                         .stream()
                         .map(port -> port.getServiceInterfacePoint()
                                 .getUniversalId())
                         .collect(Collectors.joining(",")));
        DeviceId deviceId = handler().data().deviceId();
        HttpSBController controller =
                Preconditions.checkNotNull(handler().get(HttpSBController.class));
        try {
            String jsonNode = controller.post(deviceId,
                                              CREATE_CONNECTIVITY_REQUEST,
                                              new ByteArrayInputStream(mapper.writeValueAsBytes(
                                                      request)),
                                              MediaType.APPLICATION_JSON_TYPE,
                                              String.class);
            ConnectivityService connectivityService;
            //FIXME: handle the error returned by NMS
            try {
                connectivityService =
                        mapper.readValue(jsonNode, ConnectivityService.class);
            } catch (NullPointerException e) {
                LOG.error("Impossible to install the intent {}", intent.id());
                        return null;
            }
            if (connectivityService == null) {
                return null;
            }
            LOG.info("Connectivity service installed (intent: {}): {}",
                     intent.id(),
                     connectivityService.getUuid().getUniversalId());
            intentToTunnel.addTunnelIdentifier(intent,
                                               connectivityService.getUuid()
                                                       .getUniversalId());
            return intent;
        } catch (IOException e) {
            LOG.error("Failed to convert request to json: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public DomainIntent remove(DomainIntent intent) {
        TapiIntentToTunnel intentToTunnel =
                handler().get(TapiIntentToTunnel.class);
        if (intentToTunnel.isTaggedForDeletion(intent)) {
            String tunnelId = intentToTunnel.removeIntent(intent);
            LOG.info("Tunnel {} already deleted. Removing entry.", tunnelId);
            return intent;
        }
        HttpSBController controller =
                Preconditions.checkNotNull(handler().get(HttpSBController.class));
        String serviceIdentifier =
                Preconditions.checkNotNull(intentToTunnel.removeIntent(intent),
                                           "Unknown intent identifier");
        DeleteConnectivity delete = new DeleteConnectivity(serviceIdentifier);
        try {
            DeviceId deviceId = handler().data().deviceId();
            LOG.info("Deleting service: {}", serviceIdentifier);
            int status = controller.post(deviceId,
                                         DELETE_CONNECTIVITY_REQUEST,
                                         new ByteArrayInputStream(mapper.writeValueAsBytes(
                                                 delete)),
                                         MediaType.APPLICATION_JSON_TYPE);
            LOG.info("Deletion finished with status code: {}", status);
            return intent;
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert deletion to json: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Collection<DomainIntent> getIntents() {
        return null;
    }

    /**
     * Retrieve a service endpoint for a connect point.
     *
     * @param connectPoint the connect point
     * @return the corresponding service port
     */
    private ConnectivityServiceEndPoint retrieveServiceEndpoint(
            ConnectPoint connectPoint) {
        DeviceService deviceService =
                Preconditions.checkNotNull(handler().get(DeviceService.class));
        Port port = deviceService.getPort(connectPoint);
        UniversalId uid = new UniversalId(port.annotations()
                                                  .value(NodeWrapper.SERVICE_END_POINT_TAG));
        return new ConnectivityServiceEndPoint(null,
                                               null,
                                               uid,
                                               PortRole.SYMMETRIC,
                                               PortDirection.BIDIRECTIONAL,
                                               LayerProtocolName.ODU);
    }

    /**
     * Retrieve the service endpoints for all filtered connect points.
     *
     * @param filteredConnectPoints the collection of all filtered connect
     *                              points
     * @return the list of the corresponding service endpoints
     */
    private ImmutableList<ConnectivityServiceEndPoint> retrieveServiceEndpoints(
            Collection<FilteredConnectPoint> filteredConnectPoints) {
        return filteredConnectPoints.stream()
                .map(FilteredConnectPoint::connectPoint)
                .map(this::retrieveServiceEndpoint)
                .collect(GuavaCollectors.toImmutableList());

    }

    /**
     * Simple class for creating connectivity.
     */
    public static final class CreateConnectivity {
        @JsonProperty("sep")
        private final List<ConnectivityServiceEndPoint> servicePort;

        @JsonProperty("conn-constraint")
        private final ConnectivityConstraint connConstraint;

        @JsonProperty("topo-constraint")
        private final TopologyConstraint topoConstraint;

        public CreateConnectivity(List<ConnectivityServiceEndPoint> servicePort,
                                  ConnectivityConstraint connConstraint, TopologyConstraint topoConstraint) {
            this.servicePort = servicePort;
            this.connConstraint = connConstraint;
            this.topoConstraint = topoConstraint;
        }

        public List<ConnectivityServiceEndPoint> getServicePort() {
            return servicePort;
        }

        public ConnectivityConstraint getConnConstraint() {
            return connConstraint;
        }

        public TopologyConstraint getTopoConstraint() {
            return topoConstraint;
        }
    }

    /**
     * Simple class for deleting connectivity.
     */
    public static final class DeleteConnectivity {
        @JsonProperty("service-id-or-name")
        private final String serviceIdName;

        public DeleteConnectivity(String serviceIdName) {
            this.serviceIdName = serviceIdName;
        }
    }

}
