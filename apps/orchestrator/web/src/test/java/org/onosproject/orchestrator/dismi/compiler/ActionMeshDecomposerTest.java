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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.Mesh;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.onosproject.orchestrator.dismi.primitives.Tracker;

import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ActionMeshDecomposerTest {

    private Subject subjectPoint1 = new Subject();
    private Subject subjectPoint2 = new Subject();
    private Subject subjectPoint3 = new Subject();

    private Mesh meshAction = new Mesh();

    private ActionMeshDecomposer actionMeshDecomposer;


    @Before
    public void setUp() throws Exception {

        String CP_1 = "CP_1";
        String CP_2 = "CP_2";
        String CP_3 = "CP_3";

        ConnectionPoint connectionPoint1 = new ConnectionPoint();
        connectionPoint1.setName(CP_1);

        ConnectionPoint connectionPoint2 = new ConnectionPoint();
        connectionPoint1.setName(CP_2);

        ConnectionPoint connectionPoint3 = new ConnectionPoint();
        connectionPoint1.setName(CP_3);

        subjectPoint1.setConnectionPoint(connectionPoint1);
        subjectPoint2.setConnectionPoint(connectionPoint2);
        subjectPoint3.setConnectionPoint(connectionPoint3);

        meshAction.addSourceItem(subjectPoint1);
        meshAction.addSourceItem(subjectPoint2);
        meshAction.addSourceItem(subjectPoint3);

        actionMeshDecomposer = new ActionMeshDecomposer();
    }

    @After
    public void tearDown() throws Exception {
        actionMeshDecomposer = null;
    }

    @Test
    public void testMeshDecompositionBidirectional() throws Exception {

        Tracker tracker = new Tracker();
        Set<Connection> connections = actionMeshDecomposer.decomposeBidirectional(meshAction, tracker);
        assertEquals(3, connections.size());
    }

    @Test
    public void testMeshDecompositionUnidirectional() throws Exception {

        Tracker tracker = new Tracker();
        Set<Path> connections = actionMeshDecomposer.decompose(meshAction, tracker);
        assertEquals(6, connections.size());
    }


}