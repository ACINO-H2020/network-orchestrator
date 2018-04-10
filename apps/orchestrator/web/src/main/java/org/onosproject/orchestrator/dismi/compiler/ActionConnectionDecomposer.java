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
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @Class : ActionConnectionDecomposer.
 * @Author: Created by Acreo, ACINO Team on 2016-12-16.
 * @Description : This class implements decompose method of interface IDismiDecomposer. It decomposes Connection into
 * paths which represents unidirectional flow.
 */
public class ActionConnectionDecomposer implements IDismiDecomposer {

    public static final String className = "ActionConnectionDecomposer";
    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Connection action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Aggregate action !");
        if (action instanceof Connection) {
            return decompose((Connection) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_CONNECTION, "");
        tracker.setInvalid();
        log.error("Invalid instance of Connection action !");
        return null;
    }

    /**
     * decompose, decomposes Connection action and returns set of Paths
     *
     * @param action:  connection action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Connection action, Tracker tracker) {
        log.info("Decomposing Connection into simple paths !");
        Set<Path> paths = new HashSet<Path>();
        Path path = new Path();

        Subject source = action.getSource();
        Subject destination = action.getDestination();

        path.setSource(source);
        path.setDestination(destination);
        paths.add(path);

        path = new Path();
        path.setSource(destination);
        path.setDestination(source);
        paths.add(path);

        return paths;
    }

    public Set<Connection> decomposeBidirectional(Object primitive, Tracker tracker) {
        Set<Connection> connections = new HashSet<Connection>();
        log.info("Decomposing Aggregate action !");
        if (primitive instanceof Connection) {
            connections.add((Connection) primitive);
            return connections;
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECT_IS_NOT_A_CONNECTION, "");
        tracker.setInvalid();
        log.error("Invalid instance of Connection action !");
        return null;

    }
}
