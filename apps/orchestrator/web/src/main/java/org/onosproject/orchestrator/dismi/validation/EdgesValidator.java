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

import org.onosproject.orchestrator.dismi.primitives.Subject;

import java.util.ArrayList;
import java.util.List;

public class EdgesValidator {

    // Usage : Connection and Path actions
    public boolean isSrcAndDstUnique(Subject src, Subject dst) {
        return src.getConnectionPoint().getName().equals(dst.getConnectionPoint().getName());
    }

    // Usage : Aggregate Action
    public boolean isSrcAndDstUnique(List<Subject> srcs, Subject dst) {
        for (Subject src : srcs) {
            if (src.getConnectionPoint().getName().equals(dst.getConnectionPoint().getName())) {
                return true;
            }
        }
        return false;
    }

    // Usage : Mesh Action
    public boolean isSrcAndDstUnique(List<Subject> srcs) {
        List<String> srcContainer = new ArrayList<String>();
        for (Subject src : srcs) {
            if (srcContainer.contains(src.getConnectionPoint().getName())) {
                return true;
            } else {
                srcContainer.add(src.getConnectionPoint().getName());
            }
        }
        return false;
    }

    // Usage : Multicast Action
    public boolean isSrcAndDstUnique(Subject src, List<Subject> dsts) {
        for (Subject dst : dsts) {
            if (src.getConnectionPoint().getName().equals(dst.getConnectionPoint().getName())) {
                return true;
            }
        }
        return false;
    }
}
