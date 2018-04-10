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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentEvent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.ServiceProviderIntent;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLinkList;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionUnitls;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.Action;
import org.onosproject.orchestrator.dismi.primitives.Aggregate;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.Constraint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.ServiceAction;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.onosproject.orchestrator.dismi.primitives.extended.ServiceExtended;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.onosproject.orchestrator.dismi.store.IntentFsmEvent;
import org.onosproject.orchestrator.dismi.store.ServiceId;
import org.onosproject.orchestrator.dismi.validation.DismiValidationServiceImpl;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-12-16.
 */
@Component(immediate = true)
@Service
public class IntentDecomposerManager implements IntentDecomposer {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AciStoreIface aciStoreIface;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DismiStoreIface dismiStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;


    private final Logger log = getLogger(getClass());
    private ApplicationId appId;
    private ServiceCompilationUtils serviceCompilationUtils;
    private DecomposeVocabulary decomposeVocabulary;
    private DecomposeServiceVocabulary decomposeServiceVocabulary = new DecomposeServiceVocabulary();

    @Activate
    protected void activate() {

        appId = coreService.registerApplication("org.onosproject.orchestrator.dismi");
        serviceCompilationUtils = new ServiceCompilationUtils(intentService, hostService);
        decomposeVocabulary = new DecomposeVocabulary();

    }

    @Deactivate
    protected void deactivate() {

    }

    @Override
    public void performAction(ServiceExtended service, Tracker tracker,
                              DismiValidationServiceImpl.ValidationTypeEnum action) {
        List<IntentExtended> list = service.getIntentsExtended();

        for (IntentExtended intent : list) {
            if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForCompilation)) {
                intent.getStateMachine().changeState(IntentFsmEvent.SubmitForCompilation);
            }

            decompose(service.getServiceId(), intent, null, action);
        }

    }

    @Override
    public <T extends ServiceAction> void registerServiceDecomposer(Class<T> cls, IDismiServiceDecomposer<T> compiler) {
        decomposeServiceVocabulary.registerDecomposer(cls, compiler);
    }

    @Override
    public <T extends ServiceAction> void unregisterServiceDecomposer(Class<T> cls) {
        decomposeServiceVocabulary.unregisterDecomposer(cls);
    }

    /**
     * @param intentOrg     Extended intent for decomposition
     * @param tracker       Indicates an error during decomposition process
     * @param operationType indicates what action should be taken
     * @return Set of PathIntent s which the output of decomposition process
     */
    public IntentExtended decompose(String serviceId, IntentExtended intentOrg, Tracker tracker,
                                    DismiValidationServiceImpl
                                            .ValidationTypeEnum operationType) {
        // Following function handles constraints and shifts intent level constratins at source or destination,
        // depends on the nature of action (Uni/Bidirectional)
        IntentExtended intent = arrangeConstraints(intentOrg);

        if (intent.getAction() instanceof ServiceAction) {
            log.info("We are an instance of ServiceAction");
            executeServiceIntents(serviceId, intent, operationType);
            log.info("Intents submitted");
            return intent;
        }

        log.debug("DISMI intent {} received {}", intent.getIntentId(), System.currentTimeMillis());
        Set<GenericDismiIntent> genericDismiIntentSet = getGenericDismiIntents(intent, tracker);
        if (null == genericDismiIntentSet) {
            // Error message is already displayed
            return null;
        }
        log.info("Total number of Generic Intents : " + genericDismiIntentSet.size());
        if (intent.getStateMachine().canChangeState(IntentFsmEvent.CompilationSuccess)) {
            intent.getStateMachine().changeState(IntentFsmEvent.CompilationSuccess);
        }
        executeGenericIntents(serviceId, intent, genericDismiIntentSet, operationType);
        log.debug("DISMI intent {} decomposed {}", intent.getIntentId(), System.currentTimeMillis());
        // ServiceApiServiceImpl.totoalGenericIntents = ServiceApiServiceImpl.totoalGenericIntents +
        //       genericDismiIntentSet.size();
        return intent;
    }

    /**
     * @param intent  : Action wihch is being decomposed
     * @param tracker : To provide error information
     * @return : Returns GenericDismiIntent
     * @Description: Decomposition is highly dependent on the configuration of ONOS (ONOSFeatures). If it supports both
     * unidirectional and bidirectional then all Actions will be decomposed in the Path action, which is represents a
     * unidirectional connection. For example a Connection will be decomposed into to two Paths. If it supports
     * Bidirectional Only then this function decomposes all unidirectional Actions into Connection primitive.
     * For example a Path primitive is decomposed into a connection.
     */
    private Set<GenericDismiIntent> getGenericDismiIntents(IntentExtended intent, Tracker tracker) {

        log.info("Successfully validated and starting compilation ");
        if (OnosFeatures.isUnidirectional() && OnosFeatures.isBidirectional()) {

            IDismiDecomposer iDismiDecomposer = decomposeVocabulary.getDecomposer(DecomposeVocabulary.ACTION, intent
                    .getAction().getClass());
            Set<Path> paths = iDismiDecomposer.decompose(intent.getAction(), tracker);
            if (null == paths || paths.size() == 0) {
                log.error("Decomposed intent list is empty !");
                return null;
            }
            log.info("SUPPORTS BOTH MODE - Total Paths: " + paths.size());
            return pathToGenericIntents(intent, paths);
        }
        if (OnosFeatures.isUnidirectional()) {

            IDismiDecomposer iDismiDecomposer = decomposeVocabulary.getDecomposer(DecomposeVocabulary.ACTION, intent
                    .getAction().getClass());
            Set<Path> paths = iDismiDecomposer.decompose(intent.getAction(), tracker);
            if (null == paths || paths.size() == 0) {
                log.error("Decomposed intent list is empty !");
                return null;
            }
            log.info("SUPPORTS UNIDIRECTIONAL - Total Paths: " + paths.size());
            return pathToGenericIntents(intent, paths);
        }
        if (OnosFeatures.isBidirectional()) {

            IDismiDecomposer iDismiDecomposer = decomposeVocabulary.getDecomposer(DecomposeVocabulary.ACTION, intent
                    .getAction().getClass());
            Set<Connection> connections = iDismiDecomposer.decomposeBidirectional(intent.getAction(), tracker);
            if (null == connections || connections.size() == 0) {
                log.error("Decomposed intent list is empty !");
                return null;
            }
            log.info("SUPPORTS BIDIRECTIONAL- Total Paths: " + connections.size());
            return connectionToGenericIntents(intent, connections);
        }
        log.error("Problems when decomposing DISMI intents into ACI intents !");
        return null;
    }

    /**
     * @param intent       : Extended intents are used to get other information related to intent like constraints, status,
     *                     selectors, etc
     * @param connections: Provide list of Connection for translating into GenericDismiIntents
     * @Description: Decomposes all connection (bidirectional) intents into GenericDismiIntents.
     * @return: Set of GenericDismiIntent
     */
    private Set<GenericDismiIntent> connectionToGenericIntents(IntentExtended intent, Set<Connection> connections) {
        Set<GenericDismiIntent> genericDismiIntentList = new HashSet<GenericDismiIntent>();
        short sIntentId = 0;
        for (Connection path : connections) {
            Path con2path = new Path();
            //Convert connection to path to consistent with unicode
            con2path.setDestination(path.getDestination());
            con2path.setSource(path.getSource());
            GenericDismiIntent genericDismiIntent = new GenericDismiIntent(++sIntentId);
            genericDismiIntent.setAction(con2path);
            //Intentionally keep intent level constraint empty, since we already distribute these at subject level
            genericDismiIntent.setConstraints(new ArrayList<Constraint>());//intent.getConstraints());
            genericDismiIntent.setIntentStatus(genericDismiIntent.getIntentStatus());
            genericDismiIntent.setServerInfo(intent.getServerInfo());
            genericDismiIntent.setIntentId(intent.getIntentId());
            genericDismiIntent.setIsNegotiatable(intent.getIsNegotiatable());
            genericDismiIntent.setCalendaring(intent.getCalendaring());
            genericDismiIntent.setDisplayName(intent.getDisplayName());
            genericDismiIntent.setSelectors(intent.getSelectors());
            genericDismiIntent.setPriorities(intent.getPriorities());
            genericDismiIntentList.add(genericDismiIntent);
        }
        return genericDismiIntentList;
    }

    /**
     * @param intent : Extended intents are used to get other information related to intent like constraints, status,
     *               selectors, etc
     * @param paths: Provide list of Paths for translating into GenericDismiIntents
     * @Description: Decomposes all Paths (unidirectional) intents into GenericDismiIntents.
     * @return: Set of GenericDismiIntent
     */
    private Set<GenericDismiIntent> pathToGenericIntents(IntentExtended intent, Set<Path> paths) {

        Set<GenericDismiIntent> genericDismiIntentList = new HashSet<GenericDismiIntent>();
        short sIntentId = 0;
        for (Path path : paths) {
            GenericDismiIntent genericDismiIntent = new GenericDismiIntent(++sIntentId);
            // Since it is Unidirectional at ONOS (ACI) level so we use only source constraints
            //path.getDestination().setConstraints(new ArrayList<Constraint>()); <-- removes all the constraints
            genericDismiIntent.setAction(path);
            //Intentionally keep intent level constraint empty, since we already distribute these at subject level
            genericDismiIntent.setConstraints(new ArrayList<Constraint>());//intent.getConstraints());
            genericDismiIntent.setIntentStatus(genericDismiIntent.getIntentStatus());
            genericDismiIntent.setServerInfo(intent.getServerInfo());
            genericDismiIntent.setIntentId(intent.getIntentId());
            genericDismiIntent.setIsNegotiatable(intent.getIsNegotiatable());
            genericDismiIntent.setCalendaring(intent.getCalendaring());
            genericDismiIntent.setDisplayName(intent.getDisplayName());
            genericDismiIntent.setSelectors(intent.getSelectors());
            genericDismiIntent.setPriorities(intent.getPriorities());
            genericDismiIntentList.add(genericDismiIntent);
        }
        return genericDismiIntentList;
    }

    private void executeGenericIntents(String serviceId, IntentExtended intent, Set<GenericDismiIntent> paths,
                                       DismiValidationServiceImpl.ValidationTypeEnum operationType) {

        // Create all possible links and then create a ConnectionPoint of first link and then pass it to this
        // GenericDismiIntent so we are trying first link. Other links will be tried once it will get failed. See
        // DismiStateHandler Failed block for further information
        Map<Short, AbstractionLinkList> abstractionLinkListMap = createAllEndpointCombinations(paths);

        DismiIntentId dismiIntentId = DismiIntentId.getId(intent.getIntentId());

        if (null == abstractionLinkListMap) {
            log.error("Generic DismiIntent List is empty !");
            return;
        }

        List<EndPoint> usedEndPoints = Lists.newArrayList();

        for (GenericDismiIntent path : paths) {
            AbstractionLinkList abstractionLinks = abstractionLinkListMap.get(path.getIntentNo());
            if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Create) {
                Key key = DismiIntentId.createDismiIntentKey(appId, dismiIntentId, path.getIntentNo());
                if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation)) {
                    // Update store
                    setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                            .SubmitForInstallation);
                }
                boolean result = false;
                log.info("Total Abstraction Links for intent : " + abstractionLinks.size());

                AbstractionLinkList copyAbstractionLinks = new AbstractionLinkList();
                copyAbstractionLinks.addAll(abstractionLinks);

                AbstractionLink abstractionLink;
                for (int index = 0; index < copyAbstractionLinks.size() && !result; index++) {
                    // Always use first abstractionLink because we consume it and remove from list
                    abstractionLink = copyAbstractionLinks.get(index);
                    if (OnosFeatures.isBidirectional()) {
                        if (!checkEndPoint(usedEndPoints, abstractionLink.getSrc()) &&
                                !checkEndPoint(usedEndPoints, abstractionLink.getDst())) {
                            // Removing abstraction link which is being consumed
                            abstractionLinks.remove(0);

                            // Update in the store
                            aciStoreIface.updateAbstractLinkList(dismiIntentId, key, abstractionLinks);

                            // Submitting GenericIntent
                            //result = path.submit(appId, key, abstractionLink);
                            result = serviceCompilationUtils.submitPathIntent(path, appId, key, abstractionLink);

                            //abstractionLinks.size > 1 is required
                            // when a Mesh Action is submitted and the connectionPoints
                            // are composed of more than one EndPoint
                            if (result && copyAbstractionLinks.size() > 1) {
                                usedEndPoints.add(abstractionLink.getSrc());
                                usedEndPoints.add(abstractionLink.getDst());
                            }
                        }
                    } else {
                        if (!checkEndPoint(usedEndPoints, abstractionLink.getDst())) {
                            // Removing abstraction link which is being consumed
                            abstractionLinks.remove(0);

                            // Update in the store
                            aciStoreIface.updateAbstractLinkList(dismiIntentId, key, abstractionLinks);

                            //result = path.submit(appId, key, abstractionLink);
                            result = serviceCompilationUtils.submitPathIntent(path, appId, key, abstractionLink);

                            if (result && copyAbstractionLinks.size() > 1) {
                                usedEndPoints.add(abstractionLink.getDst());
                            }
                        }
                    }
                }
                // If all intents are tried and still could not get host mapping or any other issue then it will set
                // status of that intent as failed.

                if (!result) {
                    //set status installation failed because we tried all abstraction link but could not successed
                    log.error("Problems when compiling endpoint(s) against hosts/connectpoint ! Could not find " +
                                      "suitable mapping. Please check Connection Points are installed at ONOS level. " +
                                      " ");
                    if (intent.getStateMachine().canChangeState(IntentFsmEvent.InstallationFailure)) {
                        // Update store
                        setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                                .InstallationFailure);
                    }
                }

                log.info("Operation " + DismiValidationServiceImpl.ValidationTypeEnum.Create + " completed. " +
                                 "Submission Status : " + result);
            } else if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Update) {
                if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation)) {
                    // Update store
                    setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                            .SubmitForInstallation);
                }
                Set<AciIntentKeyStatus> keySet = aciStoreIface.getKeys(dismiIntentId);
                Key intentKey = DismiIntentId.createDismiIntentKey(appId, DismiIntentId
                        .getId(intent.getIntentId()), path.getIntentNo());
                if (!idKeyParamExisits(keySet, intentKey)) {
                    Key key = DismiIntentId.createDismiIntentKey(appId, dismiIntentId, path.getIntentNo());
                    AbstractionLink abstractionLink = abstractionLinks.remove(0);
                    boolean result = serviceCompilationUtils.submitPathIntent(path, appId, key, abstractionLink);
                    if (!path.getIsNegotiatable()) {

                        boolean isTriedOtherLink = false;
                        // If first link could not mapped then try other endpoints
                        if (!result) {
                            log.info(path.getDisplayName() + " intent is not negotiable so we will apply " +
                                             "abstraction endpoints rule !");
                            while (abstractionLinks.size() > 0) {
                                result = serviceCompilationUtils.submitPathIntent(path, appId, key, abstractionLink);
                                if (result) {
                                    break;
                                }
                                abstractionLink = abstractionLinks.get(0);
                                abstractionLinks.remove(0);
                                isTriedOtherLink = true;
                            }
                        }
                        // If all intents are tried and still could not get host mapping then it will set status of that
                        // intent as failed.
                        if (abstractionLinks.size() == 0) {
                            //set status installation failed
                            log.error("Problems when compiling endpoint(s) against hosts/connectpoint ! Could not find " +
                                              "suitable mapping. Please check Connection Points are installed at ONOS level. " +
                                              " ");
                            if (intent.getStateMachine().canChangeState(IntentFsmEvent.InstallationFailure)) {
                                // Update store
                                setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                                        .InstallationFailure);
                            }
                        }

                        // If more than one endpoints (AbstractLink) are tried then it wil lcall following fucntion to updte list against key
                        if (isTriedOtherLink) {
                            aciStoreIface.updateAbstractLinkList(dismiIntentId, key, abstractionLinks);
                        }
                    } else {
                        //It seems that it is negotiable therefore, NEGOTIATION_REQ will be handle in state handler
                        log.error("Negotiable Intent Update Result =" + result);
                    }
                } else {
                    AbstractionLink abstractionLink = abstractionLinks.remove(0);
                    boolean result = serviceCompilationUtils.updatePathIntent(path, intentKey, abstractionLink);
                    //---------------------------------------------------------------------------
                    if (!path.getIsNegotiatable()) {
                        boolean isTriedOtherLink = false;
                        // If first link could not mapped then try other endpoints
                        if (!result) {
                            while (abstractionLinks.size() > 0) {
                                result = serviceCompilationUtils.updatePathIntent(path, intentKey, abstractionLink);
                                if (result) {
                                    break;
                                }
                                abstractionLink = abstractionLinks.get(0);
                                abstractionLinks.remove(0);
                                isTriedOtherLink = true;
                            }
                        }
                        // If all intents are tried and still could not get host mapping then it will set status of that
                        // intent as failed.
                        if (abstractionLinks.size() == 0 && result == false) {
                            //set status installatin failed
                            log.error("Problems when compiling endpoint(s) against hosts/connectpoint ! Could not find " +
                                              "suitable mapping. Please check Connection Points are installed at ONOS level. " +
                                              " ");
                            if (intent.getStateMachine().canChangeState(IntentFsmEvent.InstallationFailure)) {
                                // Update store
                                setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                                        .InstallationFailure);
                            }
                        }

                        // If more than one endpoints (AbstractLink) are tried then it wil lcall following fucntion to updte
                        // list against key
                        if (isTriedOtherLink) {
                            aciStoreIface.updateAbstractLinkList(dismiIntentId, intentKey, abstractionLinks);
                        }
                    } else {
                        //It seems that it is negotiable therefore, NEGOTIATION_REQ will be handle in state handler
                        log.error("Negotiable Intent update result =" + result);
                    }
                    //===========================================================================
                }

            } else if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Delete) {
                Set<AciIntentKeyStatus> keySet = aciStoreIface.getKeys(dismiIntentId);
                Key intentKey = DismiIntentId.createDismiIntentKey(appId, DismiIntentId
                        .getId(intent.getIntentId()), path.getIntentNo());
                if (!idKeyParamExisits(keySet, intentKey)) {
                    log.error("Could not find key (long value) for deleting intent !");
                } else {
                    
                    log.info("Deleting Intent key Value is " + intentKey.toString());
                    serviceCompilationUtils.deletePathIntent(intentKey);
                }
            }
        }
    }

    private void executeServiceIntents(String serviceId, IntentExtended intent,
                                       DismiValidationServiceImpl.ValidationTypeEnum operationType) {
        log.info("DISMI application id :" + appId.name() + ", Operation Type = " + operationType);

        DismiIntentId dismiIntentId = DismiIntentId.getId(intent.getIntentId());

        if (intent.getStateMachine().canChangeState(IntentFsmEvent.CompilationSuccess)) {
            intent.getStateMachine().changeState(IntentFsmEvent.CompilationSuccess);
        }

        IDismiServiceDecomposer decomposer = decomposeServiceVocabulary.getDecomposer((ServiceAction)intent.getAction());
        if (decomposer == null) {
            log.error("Decomposition of action {} is not supported", intent.getAction().getClass());
            return;
        }

        AbstractionLinkList abstractionLinks = createAllEndpointCombinations((ServiceAction) intent.getAction());
        if (null == abstractionLinks) {
            log.error("Generic DismiIntent List is empty !");
            return;
        }
        // Select find pari and then try
        AbstractionLink abstractionLink = abstractionLinks.get(0);
        // Remove it from the list
        abstractionLinks.remove(0);

        if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Create) {
            log.info("===============Create Service Intent ================");

            Set<ServiceProviderIntent> intentsToSubmit = decomposer
                    .getServiceProviderIntents(intent, appId, dismiIntentId, abstractionLink);

            if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation)) {
                // Update store
                setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                        .SubmitForInstallation);
            }

            for (Intent intentToSubmit : intentsToSubmit) {
                intentService.submit(intentToSubmit);
            }
        } else if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Update) {
            log.info("===============Update Service Intent ================");

            Intent selectedIntentInNegotiation = decomposer.getSelectedIntent(intent, appId, dismiIntentId);

            if (intent.getStateMachine().canChangeState(IntentFsmEvent.SubmitForInstallation)) {
                //log.info("Changing status of "+intent.getIntentId()+" into "+IntentFsmEvent.CompilationSuccess);
                setResolvedIntentStatus(serviceId, intent.getIntentId(), IntentFsmEvent
                        .SubmitForInstallation);
            }

            intentService.submit(selectedIntentInNegotiation);
            log.info("Intent updated");

        } else if (operationType == DismiValidationServiceImpl.ValidationTypeEnum.Delete) {

            log.info("===============Delete Service Intent ================");
            Set<AciIntentKeyStatus> keySet = aciStoreIface.getKeys(dismiIntentId);

            for (AciIntentKeyStatus intentKeyStatus : keySet) {
                Intent intentToWithdrawn = intentService.getIntent(intentKeyStatus.getIntentKey());
                if (intentToWithdrawn != null) {
                    intentService.withdraw(intentToWithdrawn);
                }
            }
            //Cleanup the store, hopefully it may work
            //aciStoreIface.removeDismiIntent(DismiIntentId.getId(intent.getIntentId()));
            log.info("Operation " + DismiValidationServiceImpl.ValidationTypeEnum.Delete + " completed !");

        }


    }

    private Map<Short, AbstractionLinkList> createAllEndpointCombinations(Set<GenericDismiIntent> paths) {
        if (paths.size() == 0) {
            return null;
        }

        AbstractionUnitls abstractionUnitls = new AbstractionUnitls();
        Map<Short, AbstractionLinkList> endPointMap = Maps.newHashMap();

        for (GenericDismiIntent genericDismiIntent : paths) {

            Path aPath = (Path) genericDismiIntent.getAction();
            EndPointList srcEndPointsAsList = dismiStore.getEndPointsAsList(aPath.getSource().getConnectionPoint());
            EndPointList dstEndPointsAsList = dismiStore.getEndPointsAsList(aPath.getDestination().getConnectionPoint());

            AbstractionLinkList abstractionLinkList = abstractionUnitls.possibleLinksForNewModel(srcEndPointsAsList,
                                                                                                 dstEndPointsAsList);
            endPointMap.put(genericDismiIntent.getIntentNo(), abstractionLinkList);
        }
        return endPointMap;
    }

    private AbstractionLinkList createAllEndpointCombinations(ServiceAction serviceAction) {

        // Get store interface to fetch List of endpoints associated with connection-points

        EndPointList srcEndPointsAsList = dismiStore.getEndPointsAsList(serviceAction.getSource().getConnectionPoint());
        EndPointList dstEndPointsAsList = dismiStore.getEndPointsAsList(serviceAction.getDestination().getConnectionPoint());
        AbstractionUnitls abstractionUnitls = new AbstractionUnitls();

        return abstractionUnitls.possibleLinksForNewModel(srcEndPointsAsList,
                                                          dstEndPointsAsList);
    }


    /**
     * @param keys
     * @param intentKeyToCheck
     * @return true if a Long value sexits in the key set otherwise return false.
     */
    private boolean idKeyParamExisits(Set<AciIntentKeyStatus> keys, Key intentKeyToCheck) {
        for (AciIntentKeyStatus key : keys) {
            if (key.getIntentKey().equals(intentKeyToCheck)) {
                return true;
            }
        }
        return false;
    }

    private boolean setResolvedIntentStatus(String serviceId, String intentId, IntentFsmEvent status) {
        ServiceId id = ServiceId.getId(serviceId);

        ServiceExtended resolvedService = (ServiceExtended) dismiStore.getResolvedService(serviceId);
        ServiceExtended serviceWithUpdatedStatus = new ServiceExtended();
        List<IntentExtended> intentExtendeds = resolvedService.getIntentsExtended();
        List<IntentExtended> intentExtendedUpdatedStatus = new ArrayList<IntentExtended>();
        for (IntentExtended intentExtended : intentExtendeds) {
            if (intentExtended.getIntentId().equalsIgnoreCase(intentId)) {
                intentExtended.getStateMachine().changeState(status);
            }
            intentExtendedUpdatedStatus.add(intentExtended);
        }
        serviceWithUpdatedStatus.setServerInfo(resolvedService.getServerInfo());
        serviceWithUpdatedStatus.setServiceStatus(resolvedService.getServiceStatus());
        serviceWithUpdatedStatus.setDisplayName(resolvedService.getDisplayName());
        serviceWithUpdatedStatus.setServiceId(resolvedService.getServiceId());
        serviceWithUpdatedStatus.setIntentsExtended(intentExtendedUpdatedStatus);
        dismiStore.setResolvedService(serviceId, serviceWithUpdatedStatus, new Tracker());
        return true;
    }

    public IntentExtended arrangeConstraints(IntentExtended intent) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        Action action = intent.getAction();
        if (action instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Subject> subjectListUpdated = new ArrayList<Subject>();
            List<Subject> subjectList = aggregate.getSource();
            for (Subject src : subjectList) {
                List<Constraint> srcConstraint = src.getConstraints();
                for (Constraint constraint : intentConstraint) {
                    srcConstraint.add(constraint);
                }
                src.setConstraints(srcConstraint);
                subjectListUpdated.add(src);
            }
            aggregate.setSource(subjectListUpdated);
            intent.setAction(aggregate);
        } else if (action instanceof Path) {
            // Path is unidirectional so merge intent constraint with source
            Path path = (Path) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Constraint> srcConstraint = path.getSource().getConstraints();
            for (Constraint constraint : intentConstraint) {
                srcConstraint.add(constraint);
            }
            path.getSource().setConstraints(srcConstraint);
            intent.setAction(path);

        } else if (action instanceof Connection) {
            Connection connection = (Connection) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Constraint> srcConstraint = connection.getSource().getConstraints();
            List<Constraint> dstConstraint = connection.getDestination().getConstraints();
            for (Constraint constraint : intentConstraint) {
                srcConstraint.add(constraint);
                dstConstraint.add(constraint);
            }
            connection.getSource().setConstraints(srcConstraint);
            connection.getDestination().setConstraints(dstConstraint);
            intent.setAction(connection);

        } else if (action instanceof SDWAN) {
            SDWAN connection = (SDWAN) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Constraint> srcConstraint = connection.getSource().getConstraints();
            List<Constraint> dstConstraint = connection.getDestination().getConstraints();
            for (Constraint constraint : intentConstraint) {
                srcConstraint.add(constraint);
                dstConstraint.add(constraint);
            }
            connection.getSource().setConstraints(srcConstraint);
            connection.getDestination().setConstraints(dstConstraint);
            intent.setAction(connection);

        } else if (action instanceof Mesh) {
            Mesh mesh = (Mesh) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Subject> subjectListUpdated = new ArrayList<Subject>();
            List<Subject> subjectList = mesh.getSource();
            for (Subject subject : subjectList) {
                List<Constraint> srcConstraint = subject.getConstraints();
                for (Constraint constraint : intentConstraint) {
                    srcConstraint.add(constraint);
                }
                subject.setConstraints(srcConstraint);
                subjectListUpdated.add(subject);
            }
            mesh.setSource(subjectListUpdated);
            intent.setAction(mesh);

        } else if (action instanceof Multicast) {
            Multicast multicast = (Multicast) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Constraint> srcConstraint = multicast.getSource().getConstraints();
            for (Constraint constraint : intentConstraint) {
                srcConstraint.add(constraint);
            }
            multicast.getSource().setConstraints(srcConstraint);
            List<Subject> subjectListUpdated = new ArrayList<Subject>();
            List<Subject> subjectList = multicast.getDestination();
            for (Subject subject : subjectList) {
                List<Constraint> dstConstraint = subject.getConstraints();
                for (Constraint constraint : intentConstraint) {
                    dstConstraint.add(constraint);
                }
                subject.setConstraints(dstConstraint);
                subjectListUpdated.add(subject);
            }
            multicast.setDestination(subjectListUpdated);
            intent.setAction(multicast);
        } else if (action instanceof Tree) {
            Tree tree = (Tree) action;
            List<Constraint> intentConstraint = intent.getConstraints();
            List<Subject> subjectListUpdated = new ArrayList<Subject>();
            List<Subject> subjectList = tree.getDestination();
            List<Constraint> srcConstraint = tree.getSource().getConstraints();
            for (Constraint constraint : intentConstraint) {
                srcConstraint.add(constraint);
            }
            for (Subject subject : subjectList) {
                List<Constraint> dstConstraint = subject.getConstraints();
                for (Constraint constraint : intentConstraint) {
                    dstConstraint.add(constraint);
                }
                subject.setConstraints(dstConstraint);
                subjectListUpdated.add(subject);
            }
            tree.setDestination(subjectListUpdated);
            tree.getSource().setConstraints(srcConstraint);
            intent.setAction(tree);
        }
        intent.setConstraints(new ArrayList<Constraint>());
        return intent;
    }

    private boolean checkEndPoint(List<EndPoint> endPointList, EndPoint endPoint) {
        return endPointList.stream().filter(eP -> eP instanceof IPEndPoint)
                .map(eP -> (IPEndPoint) eP).anyMatch(eP -> {
                    if (endPoint instanceof IPEndPoint) {
                        return eP.equals((IPEndPoint) endPoint);
                    } else {
                        return false;
                    }
                });
    }
}
