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

package org.onosproject.orchestrator.dismi.abstraction;

import org.onosproject.orchestrator.dismi.primitives.EndPoint;
import org.onosproject.orchestrator.dismi.primitives.extended.EndPointList;


/**
 * Created by aghafoor on 2017-08-14.
 */
public class AbstractionUnitls {
    /**
     * @param srcEndPointsAsList : All source endpoints
     * @param dstEndPointsAsList : All destination endpoints
     * @return AbstractionLinkList : Returns all possible combinations of source and destination endpoints
     */
    public AbstractionLinkList possibleLinksForNewModel(EndPointList srcEndPointsAsList, EndPointList
            dstEndPointsAsList) {
        AbstractionLinkList abstractionLinkList = new AbstractionLinkList();

        for (EndPoint srcP : srcEndPointsAsList) {
            for (EndPoint dstP : dstEndPointsAsList) {
                AbstractionLink abstractionLink = new AbstractionLink();
                abstractionLink.setSrc(srcP);
                abstractionLink.setDst(dstP);
                abstractionLinkList.add(abstractionLink);
            }
        }
        return abstractionLinkList;
    }
}
