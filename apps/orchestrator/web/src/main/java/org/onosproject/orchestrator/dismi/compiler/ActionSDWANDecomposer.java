package org.onosproject.orchestrator.dismi.compiler;

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.core.ApplicationId;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.Constraint;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.Key;
import org.onosproject.net.intent.SDWANProviderOneIntent;
import org.onosproject.net.intent.SDWANProviderTwoIntent;
import org.onosproject.net.intent.ServiceProviderIntent;
import org.onosproject.net.intent.constraint.NegotiableConstraint;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.aciIntents.SelectConstraint;
import org.onosproject.orchestrator.dismi.primitives.Priority;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.Selector;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

@Component(immediate = true)
@Service
public class ActionSDWANDecomposer implements IDismiServiceDecomposer<SDWAN> {


    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentDecomposer dismiDecomposer;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected AciStoreIface aciStoreIface;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;


    //TODO: probably this is not needed, since accessing the specific intent builders is too difficult.
    //Probably it better to create a specific abstract class for {Service}{Provider}Intents in ONOS.
    private final Set<Class<? extends Intent>> intentsToCreate = new HashSet<>();
    private final Logger log = getLogger(getClass());

    @Activate
    protected void activate() {
        dismiDecomposer.registerServiceDecomposer(SDWAN.class, this);
    }

    @Deactivate
    protected void deactivate() {
        dismiDecomposer.unregisterServiceDecomposer(SDWAN.class);
    }

    @Override
    public <V extends ServiceProviderIntent> void registerIntent(Class<V> cls) {
        intentsToCreate.add(cls);
    }

    @Override
    public <V extends ServiceProviderIntent> void unregisterIntent(Class<V> cls) {
        intentsToCreate.remove(cls);
    }

    @Override
    public Set<ServiceProviderIntent> getServiceProviderIntents(IntentExtended intentExtended, ApplicationId applicationId,
                                                 DismiIntentId dismiIntentId, AbstractionLink link) {

        //-----------------------------------------------------------------------------
        SDWAN sdwanAction = (SDWAN) intentExtended.getAction();
        log.info("Compiling subjects !");
        SubjectCompiler srcSubjectCompiler = new SubjectCompiler(sdwanAction.getSource());
        SubjectCompiler dstSubjectCompiler = new SubjectCompiler(sdwanAction.getDestination());

        //-------------Compile Intent Level Selectors and also include into subject level selectors
        log.info("Building traffic selectors !");
        // ToDO: What is criteria in TrafficSelector and how to map it and What we do with selector associated with Source and destination
        List<TrafficSelector> intentLevelTrafficSelectors = buildIntentLevelTrafficSelector(intentExtended);
        TrafficSelector selector = DefaultTrafficSelector.emptySelector();
        if (intentLevelTrafficSelectors.size() > 0) {
            selector = intentLevelTrafficSelectors.get(0);
        }

        log.info("Building constraints !");
        List<Constraint> srcConstraints = srcSubjectCompiler.toAciConstraint();
        List<Constraint> dstConstraints = dstSubjectCompiler.toAciConstraint();
        ConstraintCompiler constraintCompiler = new ConstraintCompiler(intentExtended.getConstraints());
        List<Constraint> intentConstraints = constraintCompiler.toAciConstraints();
        // Merge constraints and resolve duplication.
        List<Constraint> constraints = buildConstraints(srcConstraints, dstConstraints, intentConstraints);
        if (intentExtended.getIsNegotiatable()) {
            constraints.add(NegotiableConstraint.negotiable());
        }

        // Used Empty TrafficTreatment
        TrafficTreatment treatment = buildTrafficTreatment();

        SubjectCompiler subjectCompiler = new SubjectCompiler();
        HostId srdHostId = subjectCompiler.toAciHostId(hostService, link.getSrc());
        HostId dstHostId = subjectCompiler.toAciHostId(hostService, link.getDst());

        if (srdHostId == null) {
            log.error("No source host id found !");
            return null;
        }
        if (dstHostId == null) {
            log.error("No destination host id found !");
            return null;
        }

        log.info("Submitting AciIntent for : srdHostId=" + srdHostId.toString()
                         + "  dstHostId=" + dstHostId.toString());

        int intentNumber = 0;

        Key key = DismiIntentId
                .createDismiIntentKey(applicationId, DismiIntentId.getId(intentExtended.getIntentId()), ++intentNumber);

        Set<ServiceProviderIntent> intentSet = Sets.newHashSet();

        SDWANProviderOneIntent intent = SDWANProviderOneIntent.builder()
                .appId(applicationId)
                .key(key)//createNewKey(appId))
                .one(srdHostId)
                .two(dstHostId)
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                //.priority(priority)//priority())
                .build();

        intentSet.add(intent);

        aciStoreIface.addKeyIntent(key, intent);
        aciStoreIface.updateAbstractLinkList(DismiIntentId.getId(intentExtended.getIntentId()), key, null);

        key = DismiIntentId
                .createDismiIntentKey(applicationId, DismiIntentId.getId(intentExtended.getIntentId()), ++intentNumber);

        SDWANProviderTwoIntent intent2 = SDWANProviderTwoIntent.builder()
                .appId(applicationId)
                .key(key)//createNewKey(appId))
                .one(srdHostId)
                .two(dstHostId)
                .selector(selector)
                .treatment(treatment)
                .constraints(constraints)
                //.priority(priority)//priority())
                .build();

        intentSet.add(intent2);

        aciStoreIface.addKeyIntent(key, intent2);
        aciStoreIface.updateAbstractLinkList(DismiIntentId.getId(intentExtended.getIntentId()), key, null);

        return intentSet;
    }

    @Override
    public ServiceProviderIntent getSelectedIntent(IntentExtended intentExtended, ApplicationId applicationId,
                                    DismiIntentId dismiIntentId) {

        Key intentCalculatedKey = DismiIntentId
                .calculateKeyFromString(intentExtended.getIntentServiceProviderKey(), applicationId);

        Set<AciIntentKeyStatus> keySet = aciStoreIface.getKeys(dismiIntentId);
        Set<AciIntentKeyStatus> updatedKeys = Sets.newHashSet();
        Set<Intent> intentsToRemove = Sets.newHashSet();

        for (AciIntentKeyStatus keyStatus : keySet) {
            if (!keyStatus.getIntentKey().equals(intentCalculatedKey)) {
                Intent intentFound = aciStoreIface.removeIntentKey(keyStatus.getIntentKey());
                intentsToRemove.add(intentFound);
            } else {
                updatedKeys.add(keyStatus);
            }
        }
        aciStoreIface.put(dismiIntentId, updatedKeys);

        if (!intentsToRemove.isEmpty()) {
            for (Intent intentToRemove : intentsToRemove) {
                intentService.withdraw(intentToRemove);
            }
        }

        SDWAN sdwanAction = (SDWAN) intentExtended.getAction();
        SubjectCompiler srcSubjectCompiler = new SubjectCompiler(sdwanAction.getSource());
        SubjectCompiler dstSubjectCompiler = new SubjectCompiler(sdwanAction.getDestination());
        List<Constraint> srcConstraints = srcSubjectCompiler.toAciConstraint();
        List<Constraint> dstConstraints = dstSubjectCompiler.toAciConstraint();
        ConstraintCompiler constraintCompiler = new ConstraintCompiler(intentExtended.getConstraints());
        List<Constraint> intentConstraints = constraintCompiler.toAciConstraints();
        List<Constraint> constraints = buildConstraints(srcConstraints, dstConstraints, intentConstraints);

        Intent intentToSubmit = aciStoreIface.removeIntentKey(intentCalculatedKey);

        if (intentToSubmit instanceof SDWANProviderOneIntent) {

            SDWANProviderOneIntent sdwanIntent = (SDWANProviderOneIntent) intentToSubmit;

            return SDWANProviderOneIntent.builder()
                    .appId(applicationId)
                    .key(sdwanIntent.key())//createNewKey(appId))
                    .one(sdwanIntent.one())
                    .two(sdwanIntent.two())
                    .selector(sdwanIntent.selector())
                    .treatment(sdwanIntent.treatment())
                    .constraints(constraints)
                    //.priority(priority)//priority())
                    .build();
        }

        if (intentToSubmit instanceof SDWANProviderTwoIntent) {

            SDWANProviderTwoIntent sdwanIntent = (SDWANProviderTwoIntent) intentToSubmit;

            return SDWANProviderTwoIntent.builder()
                    .appId(applicationId)
                    .key(sdwanIntent.key())//createNewKey(appId))
                    .one(sdwanIntent.one())
                    .two(sdwanIntent.two())
                    .selector(sdwanIntent.selector())
                    .treatment(sdwanIntent.treatment())
                    .constraints(constraints)
                    //.priority(priority)//priority())
                    .build();
        }

        return null;
    }

    /**
     * Calls selector compiler(s) and converts DISMI selectors into ACI selector (TrafficSelector). Currently it
     * supports IPSelector. Return: List of Aci TrafficSelector,
     */
    List<TrafficSelector> buildIntentLevelTrafficSelector(IntentExtended pathIntent) {
        List<TrafficSelector> trafficSelectorList = new ArrayList<TrafficSelector>();
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
