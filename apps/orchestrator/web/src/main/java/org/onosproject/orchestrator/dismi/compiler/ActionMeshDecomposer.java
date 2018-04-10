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

import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @Class : ActionMeshDecomposer.
 * @Author: Created by Acreo, ACINO Team on 2016-12-16.
 * @Description : This class implements decompose method of interface IDismiDecomposer. It decomposes Mesh action object
 * into paths which represents unidirectional flow.
 */
public class ActionMeshDecomposer extends ActionConnectionDecomposer {

    public static final String className = "ActionMeshDecomposer";
    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Mesh action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Mesh action !");
        if (action instanceof Mesh) {
            return decompose((Mesh) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAMESH, "");
        tracker.setInvalid();
        log.error("Invalid instance of Mesh action !");
        return null;
    }

    /**
     * decompose, decomposes Mesh action and returns set of Paths
     *
     * @param action:  Mesh action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Mesh action, Tracker tracker) {
        log.info("Decomposing Mesh into simple paths !");
        List<Subject> sources = action.getSource();
        Set<Path> paths = new HashSet<Path>();
        DecomposerUtils decomposerUtils = new DecomposerUtils();
        Set<Connection> meshConnections = decomposerUtils.meshTopology(sources);
        for (Connection connection : meshConnections) {
            Set<Path> tempPaths = super.decompose(connection, tracker);
            if (null != tempPaths) {
                paths.addAll(tempPaths);
            }
        }
        return paths;
    }

    public Set<Connection> decomposeBidirectional(Object primitive, Tracker tracker) {
        log.info("Decomposing Mesh action !");
        DecomposerUtils decomposerUtils = new DecomposerUtils();
        if (primitive instanceof Mesh) {
            Mesh mesh = (Mesh) primitive;
            return decomposerUtils.meshTopology(mesh.getSource());
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAMESH, "");
        tracker.setInvalid();
        log.error("Invalid instance of Mesh action !");
        return null;
    }
}
