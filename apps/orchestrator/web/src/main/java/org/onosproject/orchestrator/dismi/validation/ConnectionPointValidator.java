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

import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.Issue;
import org.onosproject.orchestrator.dismi.primitives.Tracker;
import org.onosproject.orchestrator.dismi.primitives.extended.ConnectionPointExtended;
import org.onosproject.orchestrator.dismi.store.DismiStoreIface;
import org.slf4j.Logger;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class ConnectionPointValidator extends FieldValidator {
    String className = "ConnectionPoint";
    private final Logger log = getLogger(getClass());

    //    @Override
    public ConnectionPointExtended validateAndResolve(ConnectionPoint field, Tracker tracker) {
        /* Fields of a ConnectionPoint primitive.
        private String name = null;.
       */
        //log.info("Validating and resolving ConnectionPoint !");
        ConnectionPoint point = null;
        ConnectionPointExtended resolvedPoint = null;

        if ((null == field) || (!(field instanceof ConnectionPoint))) {
            log.error("Invalid ConnectionPoint !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             className);
            tracker.setInvalid();
            return resolvedPoint;
        }


        point = (ConnectionPoint) field;
        resolvedPoint = new ConnectionPointExtended(point);
        if (null == point.getName()) {
            log.error("ConnectionPoint name is null !");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.NULLPOINTER,
                             "");

            return resolvedPoint;
        }

        //traceMe("Trying to get a pointer to the DISMI store service");
        DismiStoreIface store = get(DismiStoreIface.class);

        // The store always returns a Set, even if it is empty. We add it to the ConnectionPointExtended,
        // to avoid null pointer stories...
        //log.info("Validating and resolving endpoint(s) associated with ConnectionPoint !");
        Set<EndPoint> endPointSet = store.getEndPoints(point);
        resolvedPoint.setEndpoints(endPointSet);
        if (endPointSet.size() == 0) {
            log.error("Unable to find connection point '\" " + point.getName() + " \"' from local repository !\"");
            tracker.addIssue(className,
                             Issue.SeverityEnum.ERROR,
                             Issue.ErrorTypeEnum.CONNECTIONPOINTNOTFOUND,
                             "Unable to find connection point '" + point.getName() + "' from local repository !");
            resolvedPoint = null;
        } else {
            //traceMe("    Found Set:\n    " + endPointSet);
            //log.info("Found endpoint(s) associated with ConnectionPoint '"+point.getName()+"' !");
        }
        //log.info("ConnectionPoint successfully validated and resolved !");
        return resolvedPoint;

    }

    void traceMe(String s) {
        System.err.println(s);
    }
}
