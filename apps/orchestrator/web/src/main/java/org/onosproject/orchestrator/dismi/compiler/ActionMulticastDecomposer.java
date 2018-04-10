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
import org.onosproject.orchestrator.dismi.primitives.Multicast;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2016-12-16.
 */
public class ActionMulticastDecomposer implements IDismiDecomposer {

    public static final String className = "ActionMulticastDecomposer";
    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Mesh action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Multicast action !");
        if (action instanceof Multicast) {
            return decompose((Multicast) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAMULTICAST, "");
        tracker.setInvalid();
        log.error("Invalid instance of Multicast action !");
        return null;
    }

    /**
     * decompose, decomposes Multicast action and returns set of Paths
     *
     * @param action:  Multicast action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Multicast action, Tracker tracker) {
        log.info("Decomposing Multicast into simple paths !");
        Subject source = action.getSource();
        List<Subject> destinations = action.getDestination();
        Set<Path> paths = new HashSet<Path>();
        for (Subject destination : destinations) {
            Path path = new Path();
            path.setSource(source);
            path.setDestination(destination);
            paths.add(path);
        }
        return paths;
    }

    public Set<Connection> decomposeBidirectional(Object primitive, Tracker tracker) {
        log.info("Decomposing Multicast action !");
        if (primitive instanceof Multicast) {
            Multicast multicast = (Multicast) primitive;
            Subject source = multicast.getSource();
            List<Subject> destinations = multicast.getDestination();
            Set<Connection> connections = new HashSet<Connection>();
            for (Subject destination : destinations) {
                Connection connection = new Connection();
                connection.setSource(source);
                connection.setDestination(destination);
                connections.add(connection);
            }
            return connections;
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTAMULTICAST, "");
        tracker.setInvalid();
        log.error("Invalid instance of Multicast action !");
        return null;
    }
}
