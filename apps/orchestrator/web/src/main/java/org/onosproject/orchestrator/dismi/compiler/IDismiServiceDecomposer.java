package org.onosproject.orchestrator.dismi.compiler;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.ServiceProviderIntent;
import org.onosproject.orchestrator.dismi.abstraction.AbstractionLink;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.ServiceAction;
import org.onosproject.orchestrator.dismi.primitives.extended.IntentExtended;

import java.util.Set;

public interface IDismiServiceDecomposer<T extends ServiceAction> {

    /**
     * Registers the specified ONOS intent in the decomposer.
     *
     * @param cls intent class
     */
    <V extends ServiceProviderIntent> void registerIntent(Class<V> cls);

    /**
     * Unregisters the specified ONOS intent in the decomposer.
     *
     * @param cls intent class
     */
    <V extends ServiceProviderIntent> void unregisterIntent(Class<V> cls);

    /**
     * Returns all the specific {Service}{Provider}Intents
     *
     * @param intentExtended the DISMI intent
     * @return a set of ONOS intents
     */
    Set<ServiceProviderIntent> getServiceProviderIntents(IntentExtended intentExtended, ApplicationId id,
                                                                       DismiIntentId dismiIntentId, AbstractionLink link);

    /**
     * Find the {Service}{Provider}Intent selected by the user
     *
     * @param intentExtended the DISMI intent
     * @param applicationId  the applicationId associated to the IntentDecomposer
     * @return the associated ONOS {Service}{Provider}intent
     */
    ServiceProviderIntent getSelectedIntent(IntentExtended intentExtended, ApplicationId applicationId, DismiIntentId dismiIntentId);
}
