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
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-01-23.
 */
public class ActionPathDecomposer implements IDismiDecomposer {

    public static final String className = "ActionPathDecomposer";
    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Path action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Path action !");
        if (action instanceof Path) {
            return decompose((Path) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAPATH, "");
        tracker.setInvalid();
        log.error("Invalid instance of Path action !");
        return null;
    }

    /**
     * decompose, decomposes Aggregate action and returns set of Paths
     *
     * @param action:  aggregate action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Path action, Tracker tracker) {
        log.info("Decomposing Path into simple paths !");
        Set<Path> paths = new HashSet<Path>();
        paths.add(action);

        return paths;
    }

    public Set<Connection> decomposeBidirectional(Object action, Tracker tracker) {
        Set<Connection> connections = new HashSet<Connection>();
        if (action instanceof Path) {
            Path path = (Path) action;
            Connection connection = new Connection();
            connection.setSource(path.getSource());
            connection.setDestination(path.getDestination());
            connections.add(connection);
            return connections;
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAPATH, "");
        tracker.setInvalid();
        log.error("Invalid instance of Path action !");
        return null;
    }
}
