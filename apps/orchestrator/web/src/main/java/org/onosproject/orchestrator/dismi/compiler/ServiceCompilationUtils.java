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

import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.ACIPPIntent;
import org.onosproject.net.intent.AciIntent;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.constraint.NegotiableConstraint;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.aciIntents.ModelType;
import org.onosproject.orchestrator.dismi.aciIntents.SelectConstraint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPEndPoint;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-02-08.
 *
 * @Description this class contains utils methods which help to submit, withdraw or delete an intent
 */
public class ServiceCompilationUtils {

    private final Logger log = getLogger(getClass());
    private int priority = 100;

    private IntentService intentService;
    private HostService hostService;

    public ServiceCompilationUtils(IntentService intentService, HostService hostService) {
        this.intentService = intentService;
        this.hostService = hostService;
    }

    /**
     * @param pathIntent, decomposed and ready for compilation
     * @return true if submitted successfully
     */
    public boolean submitPathIntent(GenericDismiIntent pathIntent,
                                    ApplicationId appId, Key key, AbstractionLink abstractionLink) {
        //boolean isNew = false;
        //--------------------------------------
        log.info("ServiceCompilationUtils::submitPathIntent--> invoked !");


        //-----------------------------------------------------------------------------
        Path path = (Path) pathIntent.getAction();
        log.info("Compiling subjects !");
        SubjectCompiler srcSubjectCompiler = new SubjectCompiler(path.getSource());
        SubjectCompiler dstSubjectCompiler = new SubjectCompiler(path.getDestination());

        //-------------Compile Intent Level Selectors and also include into subject level selectors
        log.info("Building traffic selectors !");
        // ToDO: What is criteria in TrafficSelector and how to map it and What we do with selector associated with Source and destination
        List<TrafficSelector> intentLevelTrafficSelectors = buildIntentLevelTrafficSelector(pathIntent, abstractionLink);
        TrafficSelector selector = null;
        if (intentLevelTrafficSelectors.size() == 0) {
            selector = DefaultTrafficSelector.emptySelector();
        } else {
            log.debug("Considering first selector !");
            selector = intentLevelTrafficSelectors.get(0);
        }

        log.info("Building constraints !");
        List<Constraint> srcConstraints = srcSubjectCompiler.toAciConstraint();
        List<Constraint> dstConstraints = dstSubjectCompiler.toAciConstraint();
        ConstraintCompiler constraintCompiler = new ConstraintCompiler(pathIntent.getConstraints());
        List<Constraint> intentConstraints = constraintCompiler.toAciConstraints();
        // Merge constraints and resolve duplication.
        List<Constraint> constraints = buildConstraints(srcConstraints, dstConstraints, intentConstraints);
        if (pathIntent.getIsNegotiatable()) {
            constraints.add(NegotiableConstraint.negotiable());
        }
        log.info("Total Number of Constraints: " + constraints.size());

        // Used Empty TrafficTreatment
        TrafficTreatment treatment = buildTrafficTreatment();

        if (ModelType.isNewModel()) {
            log.info("Supports new model !");
            // Keep it : Following will return list of hosts if an IP is mapped with more than one ONOS hosts
            // List<ConnectPoint> srcConnectPoints = srcSubjectCompiler.toAciConnectPoint();
            // List<ConnectPoint> dstConnectPoints = dstSubjectCompiler.toAciConnectPoint();
            log.info("Resolving connectPoints !");
            SubjectCompiler subjectCompiler = new SubjectCompiler();
            ConnectPoint ingress = subjectCompiler.toConnectPoint(abstractionLink.getSrc());
            ConnectPoint egress = subjectCompiler.toConnectPoint(abstractionLink.getDst());
            // Create IPSelector from source and destination end points

            if (ingress == null) {
                log.error("No source ConnectPoint found !");
                return false;
            }

            if (egress == null) {
                log.error(" No destination ConnectPoint found !");
                return false;
            }
            log.info("Submitting ACIPPIntent for : ingress=" + ingress.deviceId().uri() + "/" + ingress.port().toString()
                             + "  " +
                             "egress=" + egress.deviceId().uri() + "/" + egress.port().toString());
            ACIPPIntent intent = ACIPPIntent.builder()
                    .dst(egress)
                    .src(ingress)
                    .path(null)
                    .appId(appId)
                    .key(key)
                    .selector(selector)
                    .treatment(treatment)
                    .constraints(constraints)
                    .priority(priority)//priority())
                    .build();
            intentService.submit(intent);
            //log.info("Submitted !");
            return true;
        } else {
            log.info("Supports old-flate model !");
            log.info("Resolving hosts !");
            // Following will return list of hosts if an IP is mapped with more than one ONOS hosts
            //List<HostId> srcHostIds = srcSubjectCompiler.toAciHostId(hostService);
            //List<HostId> dstHostIds = dstSubjectCompiler.toAciHostId(hostService);
            SubjectCompiler subjectCompiler = new SubjectCompiler();
            HostId srdHostId = subjectCompiler.toAciHostId(hostService, abstractionLink.getSrc());
            HostId dstHostId = subjectCompiler.toAciHostId(hostService, abstractionLink.getDst());

            if (srdHostId == null) {
                log.error("No source host id found !");
                return false;
            }
            if (dstHostId == null) {
                log.error("No destination host id found !");
                return false;
            }

            log.info("Submitting AciIntent for : srdHostId=" + srdHostId.toString()
                             + "  dstHostId=" + dstHostId.toString());

            AciIntent intent = AciIntent.builder()
                    .appId(appId)
                    .key(key)//createNewKey(appId))
                    .one(srdHostId)
                    .two(dstHostId)
                    .selector(selector)
                    .treatment(treatment)
                    .constraints(constraints)
                    .priority(priority)//priority())
                    .build();
            intentService.submit(intent);
            //log.info("*Submitted !");
            return true;
        }
    }

    public boolean updatePathIntent(GenericDismiIntent pathIntent, Key key, AbstractionLink abstractionLink) {
        log.info("ServiceCompilationUtils::updatePathIntent--> invoked !");

        //-----------------------------------------------------------------------------
        Path path = (Path) pathIntent.getAction();
        log.info("Compiling subjects !");
        SubjectCompiler srcSubjectCompiler = new SubjectCompiler(path.getSource());
        SubjectCompiler dstSubjectCompiler = new SubjectCompiler(path.getDestination());

        //-------------Compile Intent Level Selectors and also include into subject level selectors
        log.info("Building traffic selectors !");
        // ToDO: What is criteria in TrafficSelector and how to map it and What we do with selector associated with Source and destination
        List<TrafficSelector> intentLevelTrafficSelectors = buildIntentLevelTrafficSelector(pathIntent, abstractionLink);
        TrafficSelector selector = null;
        if (intentLevelTrafficSelectors.size() == 0) {
            selector = DefaultTrafficSelector.emptySelector();
        } else {
            log.debug("Considering first selector !");
            selector = intentLevelTrafficSelectors.get(0);
        }

        log.info("Building constraints !");
        List<Constraint> srcConstraints = srcSubjectCompiler.toAciConstraint();
        List<Constraint> dstConstraints = dstSubjectCompiler.toAciConstraint();
        ConstraintCompiler constraintCompiler = new ConstraintCompiler(pathIntent.getConstraints());
        List<Constraint> intentConstraints = constraintCompiler.toAciConstraints();
        // Merge constraints and resolve duplication.
        List<Constraint> constraints = buildConstraints(srcConstraints, dstConstraints, intentConstraints);

        // Used Empty TrafficTreatment
        TrafficTreatment treatment = buildTrafficTreatment();

        if (ModelType.isNewModel()) {
            log.info("Supports new model !");
            // Keep it : Following will return list of hosts if an IP is mapped with more than one ONOS hosts
            // List<ConnectPoint> srcConnectPoints = srcSubjectCompiler.toAciConnectPoint();
            // List<ConnectPoint> dstConnectPoints = dstSubjectCompiler.toAciConnectPoint();
            log.info("Resolving connectPoints !");
            SubjectCompiler subjectCompiler = new SubjectCompiler();
            ConnectPoint ingress = subjectCompiler.toConnectPoint(abstractionLink.getSrc());
            ConnectPoint egress = subjectCompiler.toConnectPoint(abstractionLink.getDst());
            if (ingress == null) {
                log.error("No source ConnectPoint found !");
                return false;
            }

            if (egress == null) {
                log.error(" No destination ConnectPoint found !");
                return false;
            }
            log.info("Updating ACIPPIntent for : ingress=" + ingress.deviceId().uri() + "/" + ingress.port().toString()
                             + "  egress=" + egress.deviceId().uri() + "/" + egress.port().toString());
            ACIPPIntent intent = (ACIPPIntent) intentService.getIntent(key);
            if (null == intent) {
                log.error("Fetched ACI Intent is NULL !");
                return false;
            }

            ACIPPIntent intentUpdate = ACIPPIntent.builder()
                    .dst(egress)
                    .src(ingress)
                    .path(null)
                    .appId(intent.appId())
                    .key(intent.key())
                    .selector(selector)
                    .treatment(treatment)
                    .constraints(constraints)
                    .priority(priority)//priority())
                    .build();
            intentService.submit(intentUpdate);
            log.info("Submitted-update !");
            return true;
        } else {
            log.info("Supports old-flate model !");
            log.info("Resolving hosts !");
            // Following will return list of hosts if an IP is mapped with more than one ONOS hosts
            //List<HostId> srcHostIds = srcSubjectCompiler.toAciHostId(hostService);
            //List<HostId> dstHostIds = dstSubjectCompiler.toAciHostId(hostService);
            SubjectCompiler subjectCompiler = new SubjectCompiler();
            HostId srdHostId = subjectCompiler.toAciHostId(hostService, abstractionLink.getSrc());
            HostId dstHostId = subjectCompiler.toAciHostId(hostService, abstractionLink.getDst());

            if (srdHostId == null) {
                log.error("No source host id found !");
                return false;
            }
            if (dstHostId == null) {
                log.error("No destination host id found !");
                return false;
            }

            log.info("Updating AciIntent for : srdHostId=" + srdHostId.toString()
                             + "  dstHostId=" + dstHostId.toString());

            AciIntent intent = (AciIntent) intentService.getIntent(key);
            if (null == intent) {
                log.error("*Fetched ACI Intent is NULL !");
                return false;
            }
            AciIntent intentUpdate = AciIntent.builder()
                    .appId(intent.appId())
                    .key(intent.key())
                    .one(srdHostId)
                    .two(dstHostId)
                    .selector(selector)
                    .treatment(treatment)
                    .constraints(constraints)
                    .priority(intent.priority())//priority())
                    .build();
            intentService.submit(intentUpdate);
            log.info("*Submitted-update !");
            return true;
        }
    }

    public boolean deletePathIntent(Key key) {

        //--------------------------------------
        log.info("ServiceCompilationUtils::deletePathIntent--> invoked !");

        IntentService service = DefaultServiceDirectory.getService(IntentService.class);
        if (ModelType.isNewModel()) {
            ACIPPIntent intent = (ACIPPIntent) service.getIntent(key);
            if (null == intent) {
                log.error("ServiceCompilationUtils::deletePathIntent::-->Fetched ACI Intent is NULL !");
                return false;
            }
            service.withdraw(intent);
            //service.purge(intent);
            log.info("withdraw!");
        } else {
            // Get Key from local store and then use in the following command
            AciIntent intent = (AciIntent) service.getIntent(key);
            if (null == intent) {
                return false;
            }
            service.withdraw(intent);
            //service.purge(intent);
            log.info("*withdraw !");
        }
        return true;
    }

    /**
     * @param intentFailed : Received from event, if previous submitted intent fails.
     * @param src          New source point .
     * @param dst          new destination endpoint.
     * @return if successfully submitted then returns true otherwise returns fail.
     */
    public boolean resubmitIntent(Intent intentFailed, EndPoint src, EndPoint dst) {

        //--------------------------------------
        log.info("ServiceCompilationUtils::resubmitIntent--> invoked !");
        // This compiler is used for both soruce and destination endpoint compilation
        SubjectCompiler subjectCompiler = new SubjectCompiler();
        //-----------------------------------
        if (intentFailed instanceof ACIPPIntent) {
            log.info("ServiceCompilationUtils::resubmitIntent::--> Supports new model !");
            ConnectPoint ingress = subjectCompiler.toConnectPoint(src);
            ConnectPoint egress = subjectCompiler.toConnectPoint(dst);
            if (ingress == null) {
                log.error("Source connection point not available or it may be not an IPEndPoint !");
                return false;
            }

            if (egress == null) {
                log.error("Destination connection point not available or it may be not an IPEndPoint !");
                return false;
            }
            // Since new endpoints are being tried so we have to create a new traffic selector
            log.info("Processing IP endpoints for creating IPSelector for New Model !");
            if (!(src instanceof IPEndPoint)) {
                log.error("Source endpoint is not an IP endpoint !");
            }
            if (!(dst instanceof IPEndPoint)) {
                log.error("Destination endpoint is not an IP endpoint !");
            }
            IPEndPoint ipEndPointSrc = (IPEndPoint) src;
            IPEndPoint ipEndPointDst = (IPEndPoint) dst;
            IPSelector ipSelector = new IPSelector();
            ipSelector.setIpSrcAddr(ipEndPointSrc.getInAddr());
            ipSelector.setIpDestAddr(ipEndPointDst.getInAddr());
            SelectorCompiler selectorCompiler = new SelectorCompiler(ipSelector);

            log.info("Created IPSelector for New Model !");


            ACIPPIntent ACIPPIntentFailed = (ACIPPIntent) intentFailed;
            if (null == ACIPPIntentFailed) {
                log.error("ServiceCompilationUtils::resubmitIntent::--> Fetched ACI Intent is NULL for Abtract " +
                                  "points!");
                return false;
            }
            ACIPPIntent intentUpdate = ACIPPIntent.builder()
                    .dst(egress)
                    .src(ingress)
                    .path(null)
                    .appId(ACIPPIntentFailed.appId())
                    .key(ACIPPIntentFailed.key())
                    .selector(selectorCompiler.toTrafficSelector()) //ACIPPIntentFailed.selector()
                    .treatment(ACIPPIntentFailed.treatment())
                    .constraints(ACIPPIntentFailed.constraints())
                    .priority(priority)//priority())
                    .build();
            intentService.submit(intentUpdate);
            log.info("Submitted-update !");
        } else {
            log.info("ServiceCompilationUtils::resubmitIntent::--> Supports old-flate model !");
            log.info("ServiceCompilationUtils::resubmitIntent::--> Resolving hosts !");
            HostId srdHostId = subjectCompiler.toAciHostId(hostService, src);
            HostId dstHostId = subjectCompiler.toAciHostId(hostService, dst);

            if (null == srdHostId) {
                log.error("ServiceCompilationUtils::resubmitIntent::--> No source host id found !");
                return false;
            }


            if (null == dstHostId) {
                log.error("ServiceCompilationUtils::resubmitIntent::--> No destination host id found !");
                return false;
            }

            AciIntent aciIntentFailed = (AciIntent) intentFailed;
            if (null == aciIntentFailed) {
                log.error("ServiceCompilationUtils::updatePathIntent::-->*Fetched ACI Intent is NULL !");
                return false;
            }
            AciIntent intentUpdate = AciIntent.builder()
                    .appId(aciIntentFailed.appId())
                    .key(aciIntentFailed.key())
                    .one(srdHostId)
                    .two(dstHostId)
                    .selector(aciIntentFailed.selector())
                    .treatment(aciIntentFailed.treatment())
                    .constraints(aciIntentFailed.constraints())
                    .priority(aciIntentFailed.priority())//priority())
                    .build();
            intentService.submit(intentUpdate);
            log.info("*Submitted-update for Abtract Point!");
        }
        return true;
    }


    /**
     * Calls selector compiler(s) and converts DISMI selectors into ACI selector (TrafficSelector). Currently it
     * supports IPSelector. Return: List of Aci TrafficSelector,
     */
    List<TrafficSelector> buildIntentLevelTrafficSelector(GenericDismiIntent pathIntent, AbstractionLink abstractionLink) {
        List<TrafficSelector> trafficSelectorList = new ArrayList<TrafficSelector>();
        // Manage Selector if it is a new model (modelType.isNewModel())
        if (ModelType.isNewModel()) {
            log.info("Processing IP endpoints for creating IPSelector for New Model !");
            EndPoint endPointSrc = abstractionLink.getSrc();
            EndPoint endPointDst = abstractionLink.getDst();
            if (!(endPointSrc instanceof IPEndPoint)) {
                log.error("Source endpoint is not an IP endpoint !");
            }
            if (!(endPointDst instanceof IPEndPoint)) {
                log.error("Destination endpoint is not an IP endpoint !");
            }
            IPEndPoint ipEndPointSrc = (IPEndPoint) endPointSrc;
            IPEndPoint ipEndPointDst = (IPEndPoint) endPointDst;
            IPSelector ipSelector = new IPSelector();
            ipSelector.setIpSrcAddr(ipEndPointSrc.getInAddr());
            ipSelector.setIpDestAddr(ipEndPointDst.getInAddr());
            SelectorCompiler selectorCompiler = new SelectorCompiler(ipSelector);
            trafficSelectorList.add(selectorCompiler.toTrafficSelector());
            log.info("Added IPSelector for New Model !");
        }


        for (Selector selector : pathIntent.getSelectors()) {
            SelectorCompiler selectorCompiler = new SelectorCompiler(selector);
            TrafficSelector trafficSelector = selectorCompiler.toTrafficSelector();
            if (null == trafficSelector) {
                return trafficSelectorList;
            } else {
                trafficSelectorList.add(trafficSelector);
            }
        }
        return trafficSelectorList;
    }

    /**
     * Calls constraints compiler(s) and converts DISMI constraints into ACI constraints.. Currently it supports
     * Bandwidth and latency constraints. Return: List of Aci constraitns
     */
    List<Constraint> buildConstraints(List<Constraint> srcConstraints, List<Constraint> dstConstraints,
                                      List<Constraint> intentConstraints) {
        SelectConstraint selectConstraint = new SelectConstraint(srcConstraints, dstConstraints, intentConstraints);
        // ToDo: Extract level from existing storage
        int bwLevel = 0;
        int delayLevel = 0;
        int securityLevel = 0;
        return selectConstraint.select(bwLevel, delayLevel, securityLevel);
    }

    // Return:currently default TrafficTreatment is being used.
    TrafficTreatment buildTrafficTreatment() {
        return DefaultTrafficTreatment.emptyTreatment();
    }

    // Handle priority
    public void buildPriority(GenericDismiIntent pathIntent) {
        List<Priority> priorities = pathIntent.getPriorities();
        priorities.get(0).getPriority();
    }
}
