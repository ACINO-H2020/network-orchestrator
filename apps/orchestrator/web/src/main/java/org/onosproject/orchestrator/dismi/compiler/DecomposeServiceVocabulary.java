package org.onosproject.orchestrator.dismi.compiler;

import org.onosproject.orchestrator.dismi.primitives.Action;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.ServiceAction;
import org.onosproject.orchestrator.dismi.primitives.VPN;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Decomposer Vocabulary for ACINO App Store Services
 */
public class DecomposeServiceVocabulary {

    private final Logger log = getLogger(getClass());
    private final HashMap<Class<? extends ServiceAction>, IDismiServiceDecomposer<? extends ServiceAction>> serviceDecomposers =
            new HashMap<>();

    public <T extends ServiceAction> void registerDecomposer(Class<T> serviceAction, IDismiServiceDecomposer<T> decomposer) {
        serviceDecomposers.put(serviceAction, decomposer);
    }

    public <T extends ServiceAction> void unregisterDecomposer(Class<T> serviceAction) {
        serviceDecomposers.remove(serviceAction);
    }

    public <T extends ServiceAction> IDismiServiceDecomposer<T> getDecomposer(T actionName) {


        IDismiServiceDecomposer<T> compiler = (IDismiServiceDecomposer<T>) serviceDecomposers.get(actionName.getClass());

        if (compiler == null) {
            return null;
        }
        return compiler;
    }
}
