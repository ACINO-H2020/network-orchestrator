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

package org.onosproject.orchestrator.dismi.validation;

import org.onosproject.orchestrator.dismi.primitives.AvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.BandwidthConstraint;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.DelayConstraint;
import org.onosproject.orchestrator.dismi.primitives.EthSelector;
import org.onosproject.orchestrator.dismi.primitives.GprsSelector;
import org.onosproject.orchestrator.dismi.primitives.HighAvailabilityConstraint;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;
import org.onosproject.orchestrator.dismi.primitives.LambdaSelector;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.SecurityConstraint;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.onosproject.orchestrator.dismi.primitives.VPN;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

public class Vocabulary extends HashMap<String, HashMap> {

    private final Logger log = getLogger(getClass());
    public static final String ACTION = "action";
    public static final String SELECTOR = "selector";
    public static final String CONSTRAINT = "constraint";

    public Vocabulary() {
        loadVocabulary();
    }

    public HashMap<String, Class> put(String word, HashMap<String, Class> values) {
        return super.put(word, values);
    }

    public HashMap<String, Class> get(int index) {
        return super.get(index);
    }

    public HashMap<String, Class> get(String word) {
        return super.get(word);
    }


    public FieldValidator getValidator(String action, Class actionName) {
        FieldValidator fieldValidator = null;

        boolean isExists = get(action).containsKey(actionName);

        if (isExists) {
            Class cl = get(action).get(actionName);

            try {
                fieldValidator = (FieldValidator) cl.newInstance();
            } catch (Exception e) {
            }

        }
        return fieldValidator;
    }

    private void loadVocabulary() {
        // log.info("Loading vocabulary for validating and resolving Actions/Constraints/Selectors !");
        HashMap<Class, Class> actions = new HashMap<Class, Class>();
        actions.put(Connection.class, ActionConnectionValidator.class);
        actions.put(Multicast.class, ActionMulticastValidator.class);
        actions.put(Path.class, ActionPathValidator.class);
        actions.put(SDWAN.class, ActionSDWANValidator.class);
        actions.put(VPN.class, ActionVPNValidator.class);
        actions.put(Tree.class, ActionTreeValidator.class);
        actions.put(Mesh.class, ActionMeshValidator.class);
        this.put(ACTION, actions);

        // --------------------------------------
        HashMap<Class, Class> constraint = new HashMap<Class, Class>();
        constraint.put(BandwidthConstraint.class, BandwidthConstraintValidator.class);
        constraint.put(DelayConstraint.class, DelayConstraintValidator.class);
        constraint.put(SecurityConstraint.class, SecurityConstraintValidator.class);
        constraint.put(AvailabilityConstraint.class, AvailabilityConstraintValidator.class);
        constraint.put(HighAvailabilityConstraint.class, HighAvailabilityConstraintValidator.class);
        this.put(CONSTRAINT, constraint);

        // ---------------------------------------
        HashMap<Class, Class> selector = new HashMap<Class, Class>();
        selector.put(IPSelector.class, IPSelectorValidator.class);
        selector.put(EthSelector.class, EthSelectorValidator.class);
        selector.put(GprsSelector.class, GprsSelectorValidator.class);
        selector.put(LambdaSelector.class, LambdaSelectorValidator.class);
        this.put(SELECTOR, selector);
    }
}
