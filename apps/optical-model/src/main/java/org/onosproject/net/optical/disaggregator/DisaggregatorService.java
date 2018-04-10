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

package org.onosproject.net.optical.disaggregator;

import com.google.common.annotations.Beta;
import org.onosproject.net.DeviceId;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.PortDescription;
import org.onosproject.net.link.LinkDescription;
import org.onosproject.net.optical.device.RoadmArchitecture;

import java.util.List;
import java.util.Set;

/**
 * This service provides the dissagreggation of the ROADM based on the {@Link RoadmArchitecture}.
 * Currently, it supports and a disaggregation similar to
 * {@see https://wiki.onosproject.org/display/ONOS/Optical+Information+Model#OpticalInformationModel-DisaggregatedROADMmodel}
 * but with a Transponder for each network port.
 */
@Beta
public interface DisaggregatorService {

    /**
     * Helper method to disaggregate a single device represented by a device Id into:
     * <li> a Bottom ROADM </li>
     * <li> a Top ROADM </li>
     * <li> a transponder for each Network port</li>
     *
     * @param devId             device id of the original device
     * @param roadmArchitecture type of the ROADM architecture
     *                          Only directionless Add Drop architecture is supported
     * @param portDescriptions  the full port descriptions of the original device
     * @return a set of device id representing the disaggregated ROAMDs
     */
    Set<DeviceId> getDisaggregatedDevIds(DeviceId devId,
                                         RoadmArchitecture roadmArchitecture,
                                         List<PortDescription> portDescriptions);

    /**
     * Helper method to build a Device Description of a disaggregated ROADM.
     * In particular, the Device Description is extended as follows:
     * <li> Top/Bottom ROADM is type ROADM</li>
     * <li> Transponder is type OTN</li>
     * <li> an annotation to point to the original node</li>
     *
     * @param disaggregatedDevId the device id of the disaggregated ROADM
     * @param deviceDescription Device Description of the original ROADM
     * @return the Device Description of the disaggregated ROADM
     */
    DeviceDescription getDisaggregatedDevDescription(DeviceId disaggregatedDevId,
                                                     DeviceDescription deviceDescription);

    /**
     * Helper method to build a List of Port Description of a disaggregated ROADM.
     * In particular, the disaggregator exposes the following ports:
     * <li> The bottom ROADM has a line port already present in the original ROADM + a new line port</li>
     * <li> The top ROADM has a new line port for each transponder + a new line port that connects to the bottom</li>
     * <li> Each transponder has a given number of client ports associated with a network port +
     * such network port.
     * Client ports are present in the original ROADM and the disaggregator groups them based on the annotation
     * {@link org.onosproject.net.optical.device.port.ClientPortMapper.NETWORK_PORT} for the network port.
     * </li>
     *
     * @param disaggregatedDevId the device id of the disaggregated ROADM
     * @param portDescriptions the full port descriptions of the original device
     * @return a list of port descriptions of the disaggregated ROADM
     */
    List<PortDescription> getDisaggregatedPortDescriptions(DeviceId disaggregatedDevId,
                                                           List<PortDescription> portDescriptions);

    Set<LinkDescription> getDisaggregatedLinks(DeviceId disaggregatedDevId,
                                               Set<LinkDescription> originalLinkDescs);
}
