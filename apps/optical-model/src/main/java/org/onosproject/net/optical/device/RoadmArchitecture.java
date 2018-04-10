/*
 * Copyright 2017-present Open Networking Laboratory
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

package org.onosproject.net.optical.device;

/**
 * Common type of ROADMs architecture.
 */
public enum RoadmArchitecture {

    /**
     * Colorless, directionless, contentionless.
     */
    CDC,

    /**
     * Colorless.
     */
    C,

    /**
     * Directionless.
     */
    D,

    /**
     * Colorless, directionless.
     */
    CD;

    /**
     * Annotation key to store ROADM Architecture type.
     */

    /**
     * Transponder device id.
     */
    public static final String TRANSPONDER_ID = "tr";
    /**
     * Top ROADM device id.
     */
    public static final String TOP_ROADM_ID = "top";

    /**
     * Bottom ROADM device id.
     */
    public static final String BOTTOM_ROADM_ID = "bottom";

    /**
     * WSS ROADM device id.
     */
    public static final String WSS_ROADM_ID = "wss";

    /**
     * Annotation for the node architecture type;
     */
    public static final String ARCH_ANNOTATION = "nodeArchitecture";

    /**
     * Annotation for the original node device id;
     */
    public static final String ORIGINAL_NODE_ANNOTATION = "originalNode";

}
