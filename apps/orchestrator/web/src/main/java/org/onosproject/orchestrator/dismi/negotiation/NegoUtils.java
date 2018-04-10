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

package org.onosproject.orchestrator.dismi.negotiation;

import org.onlab.osgi.DefaultServiceDirectory;
import org.onosproject.net.intent.Key;
import org.onosproject.orchestrator.dismi.aciIntents.AciIntentKeyStatus;
import org.onosproject.orchestrator.dismi.aciIntents.AciStoreIface;
import org.onosproject.orchestrator.dismi.aciIntents.DismiIntentId;
import org.onosproject.orchestrator.dismi.primitives.Connection;
import org.onosproject.orchestrator.dismi.primitives.ConnectionPoint;
import org.onosproject.orchestrator.dismi.primitives.Intent;
import org.onosproject.orchestrator.dismi.primitives.Path;
import org.onosproject.orchestrator.dismi.primitives.SDWAN;
import org.onosproject.orchestrator.dismi.primitives.Subject;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by aghafoor on 2017-06-01.
 */
public class NegoUtils {

    private final Logger log = getLogger(getClass());

    public String extractServiceId(String dismiIntentId) {
        String serviceId = null;
        if (null != dismiIntentId && dismiIntentId.lastIndexOf("-") >= 0) {
            serviceId = dismiIntentId.substring(0, dismiIntentId.lastIndexOf("-"));
        }
        return serviceId;
    }

    /**
     * Description : Transfer action which contains enpoints
     *
     * @param originalIntent
     * @param newIntent
     * @return
     */
    public Intent copyEndpoints(Intent originalIntent, Intent newIntent) {
        if (originalIntent.getAction() instanceof Path) {
            Path path = (Path) originalIntent.getAction();
            Path newPath = new Path();
            Subject temp = new Subject();
            ConnectionPoint source = new ConnectionPoint();
            source.setName(path.getSource().getConnectionPoint().getName());
            temp.setConnectionPoint(source);
            newPath.setSource(temp);
            temp = new Subject();
            ConnectionPoint destination = new ConnectionPoint();
            destination.setName(path.getDestination().getConnectionPoint().getName());
            temp.setConnectionPoint(destination);
            newPath.setDestination(temp);
            newIntent.setAction(newPath);
            return newIntent;
        }

        if (originalIntent.getAction() instanceof SDWAN) {
            SDWAN orginalSDWAN = (SDWAN) originalIntent.getAction();
            SDWAN newSDWAN = new SDWAN();
            Subject temp = new Subject();
            ConnectionPoint source = new ConnectionPoint();
            source.setName(orginalSDWAN.getSource().getConnectionPoint().getName());
            temp.setConnectionPoint(source);
            newSDWAN.setSource(temp);
            temp = new Subject();
            ConnectionPoint destination = new ConnectionPoint();
            destination.setName(orginalSDWAN.getDestination().getConnectionPoint().getName());
            temp.setConnectionPoint(destination);
            newSDWAN.setDestination(temp);
            newIntent.setAction(newSDWAN);
            return newIntent;
        }

        if (originalIntent.getAction() instanceof Connection) {
            Connection connection = (Connection) originalIntent.getAction();
        }
        return newIntent;
    }

    public String toNewDismiId(int no, String intentId) {
        return no + "_" + intentId;
    }

    public Intent getASIntentForInstallation(String intentId, String updatedintent_id) {
        Set<Intent> intentSet = null;
        synchronized (this) {
            AlternativeSolutionIntentIface aci2dismiStoreIface = DefaultServiceDirectory.getService
                    (AlternativeSolutionIntentIface.class);
            try {
                intentSet = aci2dismiStoreIface.get(DismiIntentId.getId(intentId));
                Iterator<Intent> it = intentSet.iterator();
                while (it.hasNext()) {
                    Intent temp = it.next();
                    if (temp.getIntentId().equals(updatedintent_id)) {
                        return temp;
                    }
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                log.info(exp.toString());
            }
        }
        return null;
    }
}
