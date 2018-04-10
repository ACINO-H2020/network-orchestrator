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
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.Tree;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-12-16.
 */
public class ActionTreeDecomposer extends ActionConnectionDecomposer {

    public static final String className = "ActionTreeDecomposer";

    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Mesh action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Tree action !");
        if (action instanceof Tree) {
            return decompose((Tree) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_TREE, "");
        tracker.setInvalid();
        log.error("Invalid instance of Tree action !");
        return null;
    }

    /**
     * decompose, decomposes Tree action and returns set of Paths
     *
     * @param action:  Tree action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Tree action, Tracker tracker) {
        log.info("Decomposing Tree into simple paths !");
        Subject source = action.getSource();
        List<Subject> destinations = action.getDestination();
        Set<Path> paths = new HashSet<Path>();
        DecomposerUtils decomposerUtils = new DecomposerUtils();
        Set<Connection> treeConnections = decomposerUtils.treeTopologyDecompoistion(source, destinations);
        for (Connection connection : treeConnections) {
            Set<Path> tempPaths = super.decompose(connection, tracker);
            if (null != tempPaths) {
                paths.addAll(tempPaths);
            }
        }
        return paths;
    }
    /*public Set<Connection> getPossiableConections() {
        return treeConnections;
    }*/

    public Set<Connection> decomposeBidirectional(Object primitive, Tracker tracker) {
        log.info("Decomposing Tree into connections !");
        if (primitive instanceof Tree) {
            Tree tree = (Tree) primitive;
            Subject source = tree.getSource();
            List<Subject> destinations = tree.getDestination();
            DecomposerUtils decomposerUtils = new DecomposerUtils();
            Set<Connection> treeConnections = decomposerUtils.treeTopologyDecompoistion(source, destinations);
            return treeConnections;
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_TREE, "");
        tracker.setInvalid();
        log.error("Invalid instance of Tree action !");
        return null;
    }
}
