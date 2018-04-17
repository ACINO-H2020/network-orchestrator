/*
 * Copyright 2017-present Open Networking Foundation
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

package org.onosproject.pipelines.fabric.pipeliner;

import com.google.common.collect.ImmutableSet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.VlanId;
import org.onlab.util.ImmutableByteSequence;
import org.onosproject.net.DeviceId;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.criteria.EthCriterion;
import org.onosproject.net.flow.criteria.VlanIdCriterion;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.pipelines.fabric.FabricConstants;
import org.slf4j.Logger;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Handling forwarding objective for fabric pipeliner.
 */
public class FabricForwardingPipeliner {
    private static final Logger log = getLogger(FabricForwardingPipeliner.class);

    protected DeviceId deviceId;

    public FabricForwardingPipeliner(DeviceId deviceId) {
        this.deviceId = deviceId;
    }

    public PipelinerTranslationResult forward(ForwardingObjective forwardObjective) {
        PipelinerTranslationResult.Builder resultBuilder = PipelinerTranslationResult.builder();
        if (forwardObjective.flag() == ForwardingObjective.Flag.VERSATILE) {
            processVersatileFwd(forwardObjective, resultBuilder);
        } else {
            processSpecificFwd(forwardObjective, resultBuilder);
        }
        return resultBuilder.build();
    }

    private void processVersatileFwd(ForwardingObjective fwd,
                                     PipelinerTranslationResult.Builder resultBuilder) {
        // program ACL table only
        FlowRule flowRule = DefaultFlowRule.builder()
                .withSelector(fwd.selector())
                .withTreatment(fwd.treatment())
                .forTable(FabricConstants.TBL_ACL_ID)
                .withPriority(fwd.priority())
                .forDevice(deviceId)
                .makePermanent()
                .fromApp(fwd.appId())
                .build();
        resultBuilder.addFlowRule(flowRule);
    }

    private void processSpecificFwd(ForwardingObjective fwd,
                                    PipelinerTranslationResult.Builder resultBuilder) {
        TrafficSelector selector = fwd.selector();
        TrafficSelector meta = fwd.meta();

        ImmutableSet.Builder<Criterion> criterionSetBuilder = ImmutableSet.builder();
        criterionSetBuilder.addAll(selector.criteria());

        if (meta != null) {
            criterionSetBuilder.addAll(meta.criteria());
        }

        Set<Criterion> criteria = criterionSetBuilder.build();

        VlanIdCriterion vlanIdCriterion = null;
        EthCriterion ethDstCriterion = null;

        for (Criterion criterion : criteria) {
            switch (criterion.type()) {
                case ETH_DST:
                    ethDstCriterion = (EthCriterion) criterion;
                    break;
                case VLAN_VID:
                    vlanIdCriterion = (VlanIdCriterion) criterion;
                    break;
                default:
                    log.warn("Unsupported criterion {}", criterion);
                    break;
            }
        }

        ForwardingFunctionType forwardingFunctionType =
                ForwardingFunctionType.getForwardingFunctionType(fwd);
        switch (forwardingFunctionType) {
            case L2_UNICAST:
                processL2UnicastRule(vlanIdCriterion, ethDstCriterion, fwd, resultBuilder);
                break;
            case L2_BROADCAST:
                processL2BroadcastRule(vlanIdCriterion, fwd, resultBuilder);
                break;
            case IPV4_UNICAST:
            case IPV4_MULTICAST:
            case IPV6_UNICAST:
            case IPV6_MULTICAST:
            case MPLS:
            default:
                log.warn("Unsupported forwarding function type {}", criteria);
                resultBuilder.setError(ObjectiveError.UNSUPPORTED);
                break;
        }
    }

    // L2 Unicast: learnt mac address + vlan
    private void processL2UnicastRule(VlanIdCriterion vlanIdCriterion,
                                      EthCriterion ethDstCriterion,
                                      ForwardingObjective fwd,
                                      PipelinerTranslationResult.Builder resultBuilder) {
        checkNotNull(vlanIdCriterion, "VlanId criterion should not be null");
        checkNotNull(ethDstCriterion, "EthDst criterion should not be null");

        if (fwd.nextId() == null) {
            log.warn("Forwarding objective for L2 unicast should contains next id");
            resultBuilder.setError(ObjectiveError.BADPARAMS);
            return;
        }

        VlanId vlanId = vlanIdCriterion.vlanId();
        MacAddress ethDst = ethDstCriterion.mac();

        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchVlanId(vlanId)
                .matchEthDst(ethDst)
                .build();
        TrafficTreatment treatment = buildSetNextIdTreatment(fwd.nextId());
        FlowRule flowRule = DefaultFlowRule.builder()
                .withSelector(selector)
                .withTreatment(treatment)
                .fromApp(fwd.appId())
                .withPriority(fwd.priority())
                .makePermanent()
                .forDevice(deviceId)
                .forTable(FabricConstants.TBL_BRIDGING_ID)
                .build();

        resultBuilder.addFlowRule(flowRule);
    }

    private void processL2BroadcastRule(VlanIdCriterion vlanIdCriterion,
                                        ForwardingObjective fwd,
                                        PipelinerTranslationResult.Builder resultBuilder) {
        checkNotNull(vlanIdCriterion, "VlanId criterion should not be null");
        if (fwd.nextId() == null) {
            log.warn("Forwarding objective for L2 broadcast should contains next id");
            resultBuilder.setError(ObjectiveError.BADPARAMS);
            return;
        }

        VlanId vlanId = vlanIdCriterion.vlanId();

        TrafficSelector selector = DefaultTrafficSelector.builder()
                .matchVlanId(vlanId)
                .build();
        TrafficTreatment treatment = buildSetNextIdTreatment(fwd.nextId());
        FlowRule flowRule = DefaultFlowRule.builder()
                .withSelector(selector)
                .withTreatment(treatment)
                .fromApp(fwd.appId())
                .withPriority(fwd.priority())
                .makePermanent()
                .forDevice(deviceId)
                .forTable(FabricConstants.TBL_BRIDGING_ID)
                .build();

        resultBuilder.addFlowRule(flowRule);
    }

    private static TrafficTreatment buildSetNextIdTreatment(Integer nextId) {
        PiActionParam nextIdParam = new PiActionParam(FabricConstants.ACT_PRM_NEXT_ID_ID,
                                                      ImmutableByteSequence.copyFrom(nextId.byteValue()));
        PiAction nextIdAction = PiAction.builder()
                .withId(FabricConstants.ACT_SET_NEXT_ID_ID)
                .withParameter(nextIdParam)
                .build();

        return DefaultTrafficTreatment.builder()
                .piTableAction(nextIdAction)
                .build();
    }

}
