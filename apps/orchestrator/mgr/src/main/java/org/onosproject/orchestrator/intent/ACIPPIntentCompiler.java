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

import com.google.common.collect.ImmutableSet;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.FilteredConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentCompilationException;
import org.onosproject.net.intent.IntentCompiler;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.LinkCollectionIntent;
import org.onosproject.net.intent.constraint.DomainConstraint;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.onosproject.net.Device.Type.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by michele on 15/11/16.
 */
@Component(immediate = true)
public class ACIPPIntentCompiler
        implements IntentCompiler<ACIPPIntent> {

    private final Logger log = getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentExtensionService intentManager;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    private ApplicationId appId;

    private Integer counter = 0;

    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.net.intent");
        intentManager.registerCompiler(ACIPPIntent.class, this);
    }

    @Deactivate
    public void deactivate() {
        intentManager.unregisterCompiler(ACIPPIntent.class);
    }

    @Override
    public List<Intent> compile(ACIPPIntent intent, List<Intent> installable) {

        //Not calculated, intent is failed
        if (intent.path() == null) {
            log.debug("ACiPPIntent requested, key {}, {}", intent.key(), System.currentTimeMillis());
            throw new IntentCompilationException("Path is null");
        }

        //The store should be updated with the last stable state, so if the intent
        //was installed, now it requires a recomputation
        IntentState currentState = intentService.getIntentState(intent.key());
        if (currentState == IntentState.INSTALLED || currentState == IntentState.INSTALLING) {

            ACIPPIntent intentInStore = (ACIPPIntent) intentService.getIntent(intent.key());

            //Some inconsitencies in the db can create this issue
            if (intentInStore.path() == null) {
                return generateLinkCollection(intent);
            }

            if (intent.path() != intentInStore.path()) {
                //the intent path has been recalculated, so lets generate the new intents and don't put as failed
                Device srcDevice = deviceService.getDevice(intent.src().deviceId());
                Device dstDevice = deviceService.getDevice(intent.dst().deviceId());

                if ((srcDevice.type() == ROUTER || srcDevice.type() == SWITCH) && (dstDevice.type() == ROUTER || dstDevice.type() == SWITCH)) {
                    log.debug("IP ACiPPIntent re-calculated, key {}, {}", intent.key(), System.currentTimeMillis());
                } else if (srcDevice.type() == OTN && dstDevice.type() == OTN) {
                    log.debug("OPTICAL ACiPPIntent re-calculated, key {}, {}", intent.key(), System.currentTimeMillis());
                }
                return generateLinkCollection(intent);
            }
            //Backup path exists, so we return the list of installed intents
            if (intent.backupPath() != null) {
                return installable;
            } else {
                //Intent failed, report as failed
                throw new IntentCompilationException("Intent has failed, recomputation needed");
            }
        } else {
            Device srcDevice = deviceService.getDevice(intent.src().deviceId());
            Device dstDevice = deviceService.getDevice(intent.dst().deviceId());

            if ((srcDevice.type() == ROUTER || srcDevice.type() == SWITCH) && (dstDevice.type() == ROUTER || dstDevice.type() == SWITCH)) {
                log.debug("IP ACiPPIntent calculated, key {}, {}, {}", intent.key(), System.currentTimeMillis(), ++counter);
            } else if (srcDevice.type() == OTN && dstDevice.type() == OTN) {
                log.debug("OPTICAL ACiPPIntent calculated, key {}, {}", intent.key(), System.currentTimeMillis());
            }
            return generateLinkCollection(intent);
        }
    }

    private List<Intent> generateLinkCollection(ACIPPIntent intent) {

        Set<Link> links = intent.path().links()
                .stream()
                .filter(link -> link.src().elementId() instanceof DeviceId
                        && link.dst().elementId() instanceof DeviceId)
                .collect(toImmutableSet());

        List<Constraint> constraints = new ArrayList<>(intent.constraints());
        constraints.add(DomainConstraint.domain());
        LinkCollectionIntent linkCollectionPrimary = LinkCollectionIntent.builder()
                .key(intent.key())
                .filteredIngressPoints(ImmutableSet.of(new FilteredConnectPoint(intent.src())))
                .filteredEgressPoints(ImmutableSet.of(new FilteredConnectPoint(intent.dst())))
                .appId(intent.appId())
                .selector(intent.selector())
                .treatment(intent.treatment())
                .links(links)
                .constraints(constraints)
                .priority(intent.priority())
                .build();

        if (intent.backupPath() != null) {
            Set<Link> backupLinks = intent.backupPath().links()
                    .stream()
                    .filter(link -> link.src().elementId() instanceof DeviceId
                            && link.dst().elementId() instanceof DeviceId)
                    .collect(toImmutableSet());

            LinkCollectionIntent linkCollectionBackup =
                    LinkCollectionIntent.builder()
                            .key(intent.key())
                            .filteredIngressPoints(ImmutableSet.of(new FilteredConnectPoint(intent.src())))
                            .filteredEgressPoints(ImmutableSet.of(new FilteredConnectPoint(intent.dst())))
                            .appId(intent.appId())
                            .selector(intent.selector())
                            .treatment(intent.treatment())
                            .links(backupLinks)
                            .constraints(constraints)
                            .priority(intent.priority() - 1)
                            .build();
            return Arrays.asList(linkCollectionPrimary, linkCollectionBackup);
        }

        return Collections.singletonList(linkCollectionPrimary);

    }
}
