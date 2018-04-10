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
import org.onosproject.orchestrator.dismi.primitives.Subject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by aghafoor on 2016-12-16.
 */
public class DecomposerUtils {

    public Set<Connection> meshTopology(List<Subject> endNodes) {
        Set<Connection> meshConnections = new HashSet<Connection>();
        for (int source = 0; source < endNodes.size(); source++) {
            for (int dest = source + 1; dest < endNodes.size(); dest++) {
                Connection connection = new Connection();
                connection.setSource(endNodes.get(source));
                connection.setDestination(endNodes.get(dest));
                meshConnections.add(connection);
            }
        }
        return meshConnections;
    }

    public Set<Connection> treeTopologyDecompoistion(Subject source, List<Subject> destinations) {
        Set<Connection> treeConnections = new HashSet<Connection>();
        for (Subject destination : destinations) {
            Connection connection = new Connection();
            connection.setSource(source);
            connection.setDestination(destination);
            treeConnections.add(connection);
        }
        return treeConnections;
    }

}
