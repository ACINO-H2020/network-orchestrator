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

import org.onlab.packet.IpAddress;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.host.HostService;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-12-22.
 */
public class SubjectCompiler extends AbstractWebResource {
    private final Logger log = getLogger(getClass());
    private Subject subject = null;

    /**
     * This is default constructor and can be used only if we want to resolve Endpoint (source and destination).
     */
    public SubjectCompiler() {

    }

    /**
     * This constructor is callsed if we want to resolve all Endpoints associated with a subject. It is used when user
     * first submits Intents and Dismi compiles
     */
    public SubjectCompiler(Subject subject) {
        this.subject = subject;
    }

    /**
     * @return It resolves Endpoints and returns List of associated ConnectPoints. Normally used for ACINO new Model
     */
    public List<ConnectPoint> toAciConnectPoint() {
        List<ConnectPoint> connectPoints = new ArrayList<ConnectPoint>();
        DismiStoreIface store = get(DismiStoreIface.class);
        EndPointList endPointsAsList = store.getEndPointsAsList(this.subject.getConnectionPoint());
        for (EndPoint endPoint : endPointsAsList) {
            if (endPoint instanceof IPEndPoint) {
                IPEndPoint ipEndPoint = (IPEndPoint) endPoint;
                String rounterId = ipEndPoint.getRouterId();
                Integer portNo = ipEndPoint.getPortId();
                ConnectPoint connectPoint = ConnectPoint.deviceConnectPoint(rounterId + "/" + portNo);
                connectPoints.add(connectPoint);
            } else {
                log.warn("ToDo : DISMI --> ACI only supports IP end points but we found " + endPoint.toString());
            }
        }
        return connectPoints;
    }

    /**
     * @return It resolves Endpoints and returns List of associated HostId. It is used for Negotiation and ACINO flat-
     * model
     */
    public List<HostId> toAciHostId(HostService hostService) {
        List<HostId> hostIds = new ArrayList<HostId>();
        DismiStoreIface store = get(DismiStoreIface.class);
        EndPointList endPointsAsList = store.getEndPointsAsList(this.subject.getConnectionPoint());
        for (EndPoint endPoint : endPointsAsList) {
            if (endPoint instanceof IPEndPoint) {
                IPEndPoint ipEndPoint = (IPEndPoint) endPoint;
                Iterator<Host> hosts = hostService.getHosts().iterator();
                while (hosts.hasNext()) {
                    Host host = hosts.next();
                    Iterator<IpAddress> listOfHosts = host.ipAddresses().iterator();
                    while (listOfHosts.hasNext()) {
                        IpAddress ipAddress = listOfHosts.next();
                        if (ipAddress.toString().compareTo(getIpAddressFromSubnetAddress(ipEndPoint.getInAddr())) == 0) {
                            HostId hostId = HostId.hostId(host.mac());
                            // ToDo: For Demo we are considering only first host found in the list
                            hostIds.add(hostId);
                            //return hostIds;
                        }
                    }
                }
                //String rounterId = ipEndPoint.getRouterId();
                //Integer portNo = ipEndPoint.getPortId();
                //HostId hostId = new HostId();
                //ConnectPoint connectPoint = ConnectPoint.deviceConnectPoint(rounterId+"/"+portNo);
                //connectPoints.add(connectPoint);
            } else {
                log.warn("ToDo : DISMI --> ACI only supports IP end points but we found " + endPoint.toString());
            }
        }
        return hostIds;
    }

    /**
     * @param subnetAdd Provide subnet address and
     * @return return IP address as String
     */
    private String getIpAddressFromSubnetAddress(String subnetAdd) {

        if (subnetAdd.contains("/")) {
            return subnetAdd.substring(0, subnetAdd.indexOf("/")).trim();
        } else {
            return subnetAdd.trim();
        }
    }

    /**
     * @return Resolves Selectors and then returns ONOS compatible list of Selectors
     */
    public List<TrafficSelector> toAciSelector() {
        List<Selector> selectors = subject.getSelectors();
        List<TrafficSelector> trafficSelectors = new ArrayList<TrafficSelector>();
        for (Selector selector : selectors) {
            SelectorCompiler selectorCompiler = new SelectorCompiler(selector);
            TrafficSelector tempTrafficSelector = selectorCompiler.toTrafficSelector();
            if (tempTrafficSelector != null) {
                trafficSelectors.add(tempTrafficSelector);
            } else {
                log.warn("ToDo : What should be returned if Selector compilation retunrs null ");
            }
        }
        return trafficSelectors;
    }

    /**
     * @return Compiles constraints and returns Lost of ONOS compatible constraints
     */
    public List<org.onosproject.net.intent.Constraint> toAciConstraint() {
        ConstraintCompiler constraintCompiler = new ConstraintCompiler(subject.getConstraints());
        return constraintCompiler.toAciConstraints();
    }

    public ConnectPoint toConnectPoint(EndPoint endPoint) {
        if (endPoint instanceof IPEndPoint) {
            IPEndPoint ipEndPoint = (IPEndPoint) endPoint;
            String rounterId = ipEndPoint.getRouterId();
            Integer portNo = ipEndPoint.getPortId();
            ConnectPoint connectPoint = ConnectPoint.deviceConnectPoint(rounterId + "/" + portNo);
            return connectPoint;
        } else {
            log.warn("ToDo : DISMI --> ACI only supports IP end points but we found " + endPoint.toString());
            return null;
        }
    }

    public HostId toAciHostId(HostService hostService, EndPoint endPoint) {
        if (endPoint instanceof IPEndPoint) {
            IPEndPoint ipEndPoint = (IPEndPoint) endPoint;
            Iterator<Host> hosts = hostService.getHosts().iterator();
            while (hosts.hasNext()) {
                Host host = hosts.next();
                Iterator<IpAddress> listOfHosts = host.ipAddresses().iterator();
                while (listOfHosts.hasNext()) {
                    IpAddress ipAddress = listOfHosts.next();
                    if (ipAddress.toString().compareTo(getIpAddressFromSubnetAddress(ipEndPoint.getInAddr())) == 0) {
                        HostId hostId = HostId.hostId(host.mac());
                        return hostId;
                    }
                }
            }
            return null;
        } else {
            log.warn("ToDo : DISMI --> ACI only supports IP end points but we found " + endPoint.toString());
            return null;
        }
    }
}
