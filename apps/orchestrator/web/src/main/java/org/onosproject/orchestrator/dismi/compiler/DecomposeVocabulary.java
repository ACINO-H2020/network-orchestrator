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

import org.onosproject.orchestrator.dismi.primitives.Aggregate;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-12-16.
 */
public class DecomposeVocabulary extends HashMap<String, HashMap> {

    public static final String ACTION = "action";
    private final Logger log = getLogger(getClass());

    public DecomposeVocabulary() {
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


    public IDismiDecomposer getDecomposer(String action, Class actionName) {
        IDismiDecomposer dismiDecomposer = null;

        boolean isExists = get(action).containsKey(actionName);

        if (isExists) {
            Class cl = get(action).get(actionName);

            try {
                dismiDecomposer = (IDismiDecomposer) cl.newInstance();
            } catch (Exception e) {
                return null;
            }

        }
        return dismiDecomposer;
    }

    private void loadVocabulary() {
        //log.info("Loading Decomposition vocabulary !");
        HashMap<Class, Class> actions = new HashMap<Class, Class>();
        actions.put(Connection.class, ActionConnectionDecomposer.class);
        actions.put(Multicast.class, ActionMulticastDecomposer.class);
        actions.put(Mesh.class, ActionMeshDecomposer.class);
        actions.put(Tree.class, ActionTreeDecomposer.class);
        actions.put(Aggregate.class, ActionAggregateDecomposer.class);
        actions.put(Path.class, ActionPathDecomposer.class);
        this.put(ACTION, actions);

        // --------------------------------------
        /*HashMap<Class, Class> constraint = new HashMap<Class, Class>();
        constraint.put(BandwidthConstraint.class, BandwidthConstraintValidator.class);
        constraint.put(DelayConstraint.class, DelayConstraintValidator.class);
        constraint.put(SecurityConstraint.class, SecurityConstraintValidator.class);
        constraint.put(AvailabilityConstraint.class, AvailabilityConstraintValidator.class);
        this.put(CONSTRAINT, constraint);*/
    }
}
