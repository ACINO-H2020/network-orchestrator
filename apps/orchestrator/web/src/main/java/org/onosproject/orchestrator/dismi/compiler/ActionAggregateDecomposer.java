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
import org.onosproject.orchestrator.dismi.primitives.Issue;
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
public class ActionAggregateDecomposer implements IDismiDecomposer {

    public static final String className = "ActionAggregateDecomposer";
    private final Logger log = getLogger(getClass());

    /**
     * decompose, decomposes action and returns set of Paths
     *
     * @param action:  action which is being decomposed. It should be the sub-class of action.
     * @param tracker: tracker of any error occur during decomposition process
     * @return: If action is a valid Aggregate action, then it decomposes into simple set of paths.
     */
    public Set<Path> decompose(Object action, Tracker tracker) {
        log.info("Decomposing Aggregate action !");
        if (action instanceof Aggregate) {
            return decompose((Aggregate) action, tracker);
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTANAGGREGATE, "");
        tracker.setInvalid();
        log.error("Invalid instance of Aggregate action !");
        return null;
    }

    /**
     * decompose, decomposes Aggregate action and returns set of Paths
     *
     * @param action:  aggregate action which is being decomposed.
     * @param tracker: tracker of any error occur during decomposition process.
     * @return: it decomposes into simple set of paths.
     */
    private Set<Path> decompose(Aggregate action, Tracker tracker) {
        log.info("Decomposing aggregate into simple paths !");
        Set<Path> paths = new HashSet<Path>();
        List<Subject> sources = action.getSource();
        Subject destination = action.getDestination();
        for (Subject source : sources) {
            Path path = new Path();
            path.setSource(source);
            path.setDestination(destination);
            paths.add(path);
        }
        return paths;
    }

    public Set<Connection> decomposeBidirectional(Object primitive, Tracker tracker) {
        log.info("Decomposing aggregate into simple paths !");
        Set<Connection> connections = new HashSet<Connection>();
        log.info("Decomposing Aggregate action !");
        if (primitive instanceof Aggregate) {
            Aggregate aggregate = (Aggregate) primitive;
            List<Subject> sources = aggregate.getSource();
            Subject destination = aggregate.getDestination();
            for (Subject source : sources) {
                Connection connection = new Connection();
                connection.setSource(source);
                connection.setDestination(destination);
                connections.add(connection);
            }
            return connections;
        }
        tracker.addIssue(className, Issue.SeverityEnum.ERROR, Issue.ErrorTypeEnum.OBJECTISNOTANAGGREGATE, "");
        tracker.setInvalid();
        log.error("Invalid instance of Aggregate action !");
        return null;
    }

}
