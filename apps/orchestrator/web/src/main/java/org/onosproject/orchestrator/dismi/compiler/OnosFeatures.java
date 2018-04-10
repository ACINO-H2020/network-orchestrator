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

/**
 * Created by aghafoor on 2017-03-31.
 */
public class OnosFeatures {

    private static boolean bidirectional = true;
    private static boolean unidirectional = false;

    public static boolean isBidirectional() {
        return bidirectional;
    }

    public static void setBidirectional(boolean newBidirectional) {
        bidirectional = newBidirectional;
    }

    public static boolean isUnidirectional() {
        return unidirectional;
    }

    public static void setUnidirectional(boolean newUnidirectional) {
        unidirectional = newUnidirectional;
    }
}
