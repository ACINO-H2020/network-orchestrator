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

package org.onosproject.orchestrator.dismi.store;

import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.aciIntents.ModelType;
import org.onosproject.orchestrator.dismi.compiler.ServiceCompilationUtils;
import org.onosproject.orchestrator.dismi.negotiation.AciToDismiComposer;
import org.onosproject.orchestrator.dismi.negotiation.AlternativeSolutionIntentIface;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

//import org.onosproject.orchestrator.dismi.ServiceApiService;

/**
 * Created by aghafoor on 2017-03-13.
 * This class handles all states of the ACI Intents and then sets the startes of Dismi intents as per 4.1.3.3 of
 * document D. 4.2
 */
public class DismiStateHandler {

    private DismiStoreIface dismiStore;
    private AciStoreIface aciStoreIface;
    private IntentService intentService;
    private HostService hostService;

    private final Logger log = getLogger(getClass());

    public DismiStateHandler(DismiStoreIface dismiStore, AciStoreIface aciStoreIface,
                             IntentService intentService, HostService hostService) {
        this.dismiStore = dismiStore;
        this.aciStoreIface = aciStoreIface;
        this.intentService = intentService;
        this.hostService = hostService;
    }

    /**
     * @param key : Aci intent key provided to to find the Dismi intent for managing install request
     */
    public void manageStates(org.onosproject.net.intent.Intent intent, Key key, IntentEvent.Type status) {
        log.info("Managing DISMI intent states !");
        DismiIntentId dismiIntentId = findDismiIntentId(key);
        if (dismiIntentId == null) {
            log.warn("Dismi ID not found, may be this intent is not installed through DIsmi !");
            return;
        }
        String serviceId = extractServiceId(dismiIntentId.id());
        ServiceExtended serviceExtended = (ServiceExtended) dismiStore.getResolvedService(serviceId);
        setNewStatus(intent, serviceExtended, dismiIntentId.id(), key,
                     status);
        log.info("Managing DISMI intent state process completed !");
    }

    /**
     * @param key : accepts Aci key and the finds DismiIntentId,
     * @return if founds DismiIntentId corresponding to key, returns otherwise it returns null;
     */
    public DismiIntentId findDismiIntentId(Key key) {

        Set<DismiIntentId> dismiIntentIds = null;
        synchronized (this) {
            dismiIntentIds = aciStoreIface.listDismiIntentId();
        }
        for (DismiIntentId dismiIntentId : dismiIntentIds) {
            Set<AciIntentKeyStatus> keySet = aciStoreIface.getKeys(dismiIntentId);
            for (AciIntentKeyStatus intentKeyStatus : keySet) {
                if (intentKeyStatus.getIntentKey().equals(key)) {
                    return dismiIntentId;
                }
            }
        }
        return null;
    }

    private void traceMe(String s) {
        log.info(s);
    }

    /**
     * @param dismiIntentId
     * @return serviceid
     */
    private String extractServiceId(String dismiIntentId) {
        String serviceId = null;
        if (null != dismiIntentId && dismiIntentId.lastIndexOf("-") >= 0) {
            serviceId = dismiIntentId.substring(0, dismiIntentId.lastIndexOf("-"));
        }
        return serviceId;
    }

    /**
     * @param intent
     * @param serviceExtended
     * @param stringIntentId
     * @param key
     * @param newStatus
     */
    public void setNewStatus(org.onosproject.net.intent.Intent intent, ServiceExtended serviceExtended, String
            stringIntentId, Key key, IntentEvent.Type newStatus) {
        log.info("Received status changed even at ACI Intent Level: " + newStatus);
        List<IntentExtended> intentExtendeds = serviceExtended.getIntentsExtended();
        if (intentExtendeds.isEmpty()) {
            return;
        }
        AciIntentKeyStatus aciIntentKeyStatusUpdated = new AciIntentKeyStatus();
        IntentFsmEvent dismiIntentstatus = null;
        DismiIntentId dismiIntentId = DismiIntentId.getId(stringIntentId);
        boolean changeDismiStatus = false;
        for (IntentExtended intentExtended : intentExtendeds) {
            if (intentExtended.getIntentId().equalsIgnoreCase(stringIntentId)) {
                switch (newStatus) {
                    case INSTALL_REQ:
                        synchronized (this) {
                            // If any of Aci intent state is not INSTALL_REQ, Failed, WithDraw or Withdrawing or then set
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.INSTALL_REQ);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }
                        break;

                    case NEGOTIATION_REQ:

                        List<org.onosproject.net.intent.Intent> aciIntents = intentService.getAlternativeSolutions(key);
                        AciToDismiComposer aciToDismiComposer = new AciToDismiComposer();
                        Set<Intent> intentSet = aciToDismiComposer.generateAlternativeSolutionsIntent
                                (DismiIntentId.getId(stringIntentId), aciIntents);
                        synchronized (this) {
                            AlternativeSolutionIntentIface aci2dismiStoreIface = DefaultServiceDirectory.getService
                                    (AlternativeSolutionIntentIface.class);
                            try {
                                aci2dismiStoreIface.put(dismiIntentId, intentSet);

                            } catch (Exception exp) {

                                exp.printStackTrace();
                                log.info(exp.toString());

                            }
                        }
                        aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.NEGOTIATION_REQ);
                        // Set the current state of intent against key
                        changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                               aciIntentKeyStatusUpdated);
                        // find status based on the policy defined in D4.2
                        dismiIntentstatus =
                                findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                        if (dismiIntentstatus == null) {
                            log.error("findDismiIntentNextStatus returned null!");
                            return;
                        }
                        log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                        changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        break;

                    case INSTALLED:

                        // If any of Aci intent state is not INSTALL_REQ, Failed, WithDraw or Withdrawing or then set
                        synchronized (this) {
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.INSTALLED);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // Following code is only used for Negotiation feature
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }

                        log.info("Store composed intent generated from AciIntent !");
                        break;

                    case FAILED:
                        synchronized (this) {
                            // Here check if it failed then use Abstract Endpoints
                            // If all endpoints tried then moved to the setting status Failed

                            // If any aci Intent failed change the status of that acin failed and also dismi status
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.FAILED);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }
                        break;

                    case WITHDRAW_REQ:
                        // If any one of the Aci intent state is Withdrawn then set the status of only this aci
                        // intent WITHDRAW_REQ
                        synchronized (this) {
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.WITHDRAW_REQ);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }
                        break;

                    case WITHDRAWN:
                        synchronized (this) {
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.WITHDRAWN);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }
                        break;

                    case CORRUPT:
                        // If any aci Intent failed change the status of that acin failed and also dismi status
                        synchronized (this) {
                            aciIntentKeyStatusUpdated.setStatus(IntentEvent.Type.FAILED);
                            // Set the current state of intent against key
                            changeDismiStatus = changeAciKeyStatus(intent, dismiIntentId, key,
                                                                   aciIntentKeyStatusUpdated);
                            // find status based on the policy defined in D4.2
                            dismiIntentstatus =
                                    findDismiIntentNextStatus(dismiIntentId, key, aciIntentKeyStatusUpdated, changeDismiStatus);
                            if (dismiIntentstatus == null) {
                                log.error("findDismiIntentNextStatus returned null!");
                                return;
                            }
                            log.info("findDismiIntentNextStatus returned " + dismiIntentstatus);
                            changeDismiIntentStatus(dismiIntentId, dismiIntentstatus);
                        }
                        break;

                    default:
                        break;

                }
            }
        }
    }

    /**
     * @param dismiIntentId      : to extract AciIntent keys and then chanche the status of Aci intent stored with key
     * @param key                : Key to find the actual intent from aci-key-store
     * @param aciIntentKeyStatus : The new status of Aci intent
     */
    private boolean changeAciKeyStatus(org.onosproject.net.intent.Intent intent, DismiIntentId dismiIntentId, Key key,
                                       AciIntentKeyStatus aciIntentKeyStatus) {
        log.info("Updating ACI key and its status to '" + aciIntentKeyStatus.getStatus() + "'!");
        boolean changeDismiStatus = true;
        synchronized (this) {

            Set<AciIntentKeyStatus> aciIntentKeyStatuses = aciStoreIface.getKeys(dismiIntentId);
            Set<AciIntentKeyStatus> updatedaciIntentKeyStatuses = new HashSet<AciIntentKeyStatus>();
            for (AciIntentKeyStatus intentKeyStatus : aciIntentKeyStatuses) {
                // This part is for netrap so it will be executed if it is not negotiable. By default isCalculated
                // is set false, so if netrap is not in action then following statement will not be executed

                // Check the key is same so we have to update the right intetns
                if (intentKeyStatus.getIntentKey().equals(key)) {


                    ServiceCompilationUtils serviceCompilationUtils = new ServiceCompilationUtils(intentService, hostService);
                    AbstractionLinkList abstractionLinkList = intentKeyStatus.getAbstractionLinks();
                    // Check configured to use new model then we have to check that netrap already set true status
                    // otherwise we assume that this Failed is for netrap
                    if (ModelType.isNewModel()) {
                        ACIPPIntent aCIPPIntent = (ACIPPIntent) intent;
                        if (intentKeyStatus.isCalculated()) {
                            // Do we have more endpoints, execute abstraction

                            if (intentKeyStatus.getAbstractionLinks() != null) {
                                if (aciIntentKeyStatus.getStatus() == IntentEvent.Type.FAILED && intentKeyStatus.getAbstractionLinks()
                                        .size() > 0) {
                                    log.info("Intent failed so try next endpoint [Using Abstraction fature]");
                                    int remainingEps = abstractionLinkList.size();
                                    log.info("Remaining Endpoints :" + remainingEps);
                                    // Change status
                                    changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.InstallationFailure);
                                    changeDismiStatus = false;
                                    // Try all endpoints one-by-one
                                    for (; remainingEps > 0; ) {
                                        // Fetch first endpoint
                                        AbstractionLink abstractionLink = abstractionLinkList.get(0);
                                        // Resubmit
                                        boolean resubmitStatus = serviceCompilationUtils.resubmitIntent(intent, abstractionLink
                                                .getSrc(), abstractionLink.getDst());
                                        // Remove consumed endpoint
                                        abstractionLinkList.remove(0);

                                        if (resubmitStatus) {
                                            // If successful then change status installation because we resubmitted
                                            // intent
                                            changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.SubmitForInstallation);
                                            changeDismiStatus = false;
                                            break;
                                        }
                                    }
                                }
                            } else if (aciIntentKeyStatus.getStatus() == IntentEvent.Type.FAILED && intentKeyStatus.getAbstractionLinks()
                                    .size() == 0) {
                                // If not abstraction endpoint left then set status failed
                                // because we resubmitted intent
                                changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.InstallationFailure);
                                changeDismiStatus = false;
                            }
                        }
                        aciIntentKeyStatus.setCalculated(aCIPPIntent.calculated());
                    } else {
                        if (intentKeyStatus.getAbstractionLinks() != null) {
                            if (aciIntentKeyStatus.getStatus() == IntentEvent.Type.FAILED && intentKeyStatus.getAbstractionLinks()
                                    .size() > 0) {

                                log.info("Intent failed so try next endpoint [Using Abstraction fature]");
                                int remainingEps = abstractionLinkList.size();
                                log.info("Remaining Endpoints :" + remainingEps);

                                changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.InstallationFailure);
                                changeDismiStatus = false;
                                // Try all endpoints one-by-one
                                for (; remainingEps > 0; ) {
                                    // Fetch first endpoint
                                    AbstractionLink abstractionLink = abstractionLinkList.get(0);
                                    // Resubmit
                                    boolean resubmitStatus = serviceCompilationUtils.resubmitIntent(intent, abstractionLink
                                            .getSrc(), abstractionLink.getDst());
                                    // Remove consumed endpoint
                                    abstractionLinkList.remove(0);

                                    if (resubmitStatus) {
                                        // If successful then change status installation because we resubmitted intent
                                        changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.SubmitForInstallation);
                                        changeDismiStatus = false;
                                        break;
                                    }
                                }
                            }
                        } else if (aciIntentKeyStatus.getStatus() == IntentEvent.Type.FAILED && intentKeyStatus.getAbstractionLinks()
                                .size() == 0) {
                            // If not abstraction endpoint left then set status failed
                            changeDismiIntentStatus(dismiIntentId, IntentFsmEvent.InstallationFailure);
                            changeDismiStatus = false;
                        }
                    }
                    aciIntentKeyStatus.setIntentKey(intentKeyStatus.getIntentKey());
                    aciIntentKeyStatus.setAbstractionLinks(abstractionLinkList);
                    updatedaciIntentKeyStatuses.add(aciIntentKeyStatus);
                } else {
                    updatedaciIntentKeyStatuses.add(intentKeyStatus);
                }
            }
            aciStoreIface.put(dismiIntentId, updatedaciIntentKeyStatuses);
            log.info("Key status : " + dismiIntentId + "\t" + aciIntentKeyStatus.getStatus());
        }
        return changeDismiStatus;
    }
    // This function is copy of another function used in IntentDecomposer

    /**
     * @param dismiIntentId : To change the status of dismi intent id
     * @param smStatus      : Updated status of dismi intent id
     * @return : If we cannot move to next state, it will return false and will not change the status
     * ToDo: limitations, what if fails
     */
    private boolean changeDismiIntentStatus(DismiIntentId dismiIntentId, IntentFsmEvent smStatus) {
        log.info("changeDismiIntentStatus called ");
        log.info("dismiIntentId: " + dismiIntentId);
        log.info("smStatus: " + smStatus);
        String serviceId = extractServiceId(dismiIntentId.id());
        ServiceId id = ServiceId.getId(serviceId);
        DismiStoreIface dismiStore = DefaultServiceDirectory.getService(DismiStoreIface.class);
        ServiceExtended resolvedService = (ServiceExtended) dismiStore.getResolvedService(serviceId);
        List<IntentExtended> intentExtendeds = resolvedService.getIntentsExtended();
        List<IntentExtended> intentExtendedUpdatedStatus = new ArrayList<IntentExtended>();
        ServiceExtended serviceWithUpdatedStatus = new ServiceExtended();
        // Check first whether we are allowed to change the states of all the Intents
        for (IntentExtended intentExtended : intentExtendeds) {
            //log.info("intentExtended: " + intentExtended);
            if (intentExtended.getIntentId().equalsIgnoreCase(dismiIntentId.id())) {
                IntentFiniteStateMachine fsm = intentExtended.getStateMachine();
                if (!fsm.canChangeState(smStatus)) {
                    String intentId = intentExtended.getIntentId();
                    InternalIntentState state = intentExtended.getStateMachine().getState();
                    log.error("Intent " + intentId + ": Event " + smStatus + " can't transition from state "
                                      + state);

                    return false;
                } else {
                    log.info("Intent " + intentExtended.getIntentId() + " Can change state to " + smStatus);
                }
            }
        }

        // Everything looks fine, so let's change the states
        for (IntentExtended intentExtended : intentExtendeds) {
            if (intentExtended.getIntentId().equalsIgnoreCase(dismiIntentId.id())) {
                intentExtended.getStateMachine().changeState(smStatus);
            }
            intentExtendedUpdatedStatus.add(intentExtended);
            //log.info("Changed state on intent " + intentExtended);
        }

        serviceWithUpdatedStatus.setServerInfo(resolvedService.getServerInfo());
        serviceWithUpdatedStatus.setServiceStatus(resolvedService.getServiceStatus());
        serviceWithUpdatedStatus.setDisplayName(resolvedService.getDisplayName());
        serviceWithUpdatedStatus.setServiceId(resolvedService.getServiceId());
        serviceWithUpdatedStatus.setIntentsExtended(intentExtendedUpdatedStatus);
        dismiStore.setResolvedService(serviceId, serviceWithUpdatedStatus, new Tracker());
        log.info("Changed Intent Status: " + dismiIntentId + "\tto: " + smStatus);
        return true;
    }

    /**
     * @param dismiIntentId
     * @param key
     * @param aciIntentKeyStatus
     * @param changeDismiStatus
     * @return
     */
    protected IntentFsmEvent findDismiIntentNextStatus(DismiIntentId dismiIntentId, Key key, AciIntentKeyStatus
            aciIntentKeyStatus, boolean changeDismiStatus) {
        // Assign priority to the states
        int withdraw = 5; // Highest priority
        int withdraw_req = 4;
        int failed = 3;
        int install_req = 2;
        int negotiation_req = 1;
        int install = 0; // Lowest priority

        int statusVal = 0;

        Set<AciIntentKeyStatus> aciIntentKeyStatuses = aciStoreIface.getKeys(dismiIntentId);
        for (AciIntentKeyStatus intentKeyStatus : aciIntentKeyStatuses) {
            int pos = 0;
            if (intentKeyStatus.getIntentKey().equals(key)) {
                pos = getStatusBitNo(aciIntentKeyStatus.getStatus());
            } else {
                pos = getStatusBitNo(intentKeyStatus.getStatus());
            }
            statusVal = statusVal | (1 << pos);
        }

        //log.info("======>findDismiIntentNextStatus: " +statusVal);
        // All Aci intents has Withdrawn status so set dismi intent status as Witndrawn
        if (statusVal == 32) {
            log.info("findDismiIntentNextStatus: returning WithdrawalSuccess");
            return IntentFsmEvent.WithdrawalSuccess;
        }
        if (((statusVal >> withdraw_req) & 1) != 0) {
            log.info("findDismiIntentNextStatus: returning SubmitForWithdrawal");
            return IntentFsmEvent.SubmitForWithdrawal;
        } else if (((statusVal >> failed) & 1) != 0) {
            log.info("findDismiIntentNextStatus: returning InstallationFailure");
            return IntentFsmEvent.InstallationFailure;
        } else if (((statusVal >> install_req) & 1) != 0) {
            log.info("findDismiIntentNextStatus: returning SubmitForInstallation");
            return IntentFsmEvent.SubmitForInstallation;
        } else if (((statusVal >> negotiation_req) & 1) != 0) {
            log.info("findDismiIntentNextStatus: returning Negotiation");
            return IntentFsmEvent.Negotiation;
        } else if (((statusVal >> install) & 1) != 0 && changeDismiStatus) {
            log.info("findDismiIntentNextStatus: returning InstallationSuccess");
            return IntentFsmEvent.InstallationSuccess;
        }
        //return IntentFsmEvent.Negotiation;
        log.info("findDismiIntentNextStatus: returning null");
        return null;
        //
    }

    /**
     * @param status
     * @return
     */
    private int getStatusBitNo(IntentEvent.Type status) {
        int withdraw = 5; // Highest priority
        int withdraw_req = 4;
        int failed = 3;
        int install_req = 2;
        int negotiation_req = 1;
        int install = 0; // Lowest priority

        switch (status) {
            case INSTALL_REQ:
                return install_req;
            case INSTALLED:
                return install;
            case FAILED:
                return failed;
            case WITHDRAW_REQ:
                return withdraw_req;
            case WITHDRAWN:
                return withdraw;
            case CORRUPT:
                return failed;
            case PURGED:
                return withdraw;
            case NEGOTIATION_REQ:
                return negotiation_req;
        }
        return -1;
    }
}
