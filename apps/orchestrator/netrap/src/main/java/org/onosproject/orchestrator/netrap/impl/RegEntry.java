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

package org.onosproject.orchestrator.netrap.impl;

import org.apache.commons.codec.binary.Hex;

public class RegEntry {
    private String name = null;
    private byte[] routeId = null;

    public RegEntry(byte[] routeId, String name) {
        this.name = name;
        this.routeId = routeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getRouteId() {
        return routeId;
    }

    public void setRouteId(byte[] routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        if (routeId != null) {
            return "N2P Name: " + name + " RouteId: " + Hex.encodeHexString(routeId);
        }

        return "N2P Name: " + name + " RouteId: null";
    }
}
