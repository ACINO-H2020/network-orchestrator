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

package org.onosproject.drivers.cop.model;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class PathType {

    private final Boolean multiLayer;
    private final Boolean noPath;
    private final String id;
    private final List<Endpoint> topoComponents;
    private final Label label;

    public PathType(@JsonProperty("multiLayer") Boolean multiLayer,
                    @JsonProperty("noPath") Boolean noPath,
                    @JsonProperty("id") String id,
                    @JsonProperty("topoComponents") List<Endpoint> topoComponents,
                    @JsonProperty("label") Label label) {
        this.multiLayer = multiLayer;
        this.noPath = noPath;
        this.id = id;
        this.topoComponents = topoComponents != null ? ImmutableList
                .copyOf(topoComponents) : ImmutableList.of();
        this.label = label;
    }

    @JsonProperty("multiLayer")
    public Boolean getMultiLayer() {
        return multiLayer;
    }

    @JsonProperty("noPath")
    public Boolean getNoPath() {
        return noPath;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("topoComponents")
    public List<Endpoint> getTopoComponents() {
        return topoComponents;
    }

    @JsonProperty("label")
    public Label getLabel() {
        return label;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PathType pathType = (PathType) o;

        return Objects.equals(multiLayer, pathType.multiLayer)
                && Objects.equals(noPath, pathType.noPath)
                && Objects.equals(id, pathType.id)
                && Objects.equals(topoComponents, pathType.topoComponents)
                && Objects.equals(label, pathType.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(multiLayer, noPath, id, topoComponents, label);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PathType {\n");
        sb.append("  multiLayer: ").append(toIndentedString(multiLayer))
                .append("\n");
        sb.append("  noPath: ").append(toIndentedString(noPath)).append("\n");
        sb.append("  id: ").append(toIndentedString(id)).append("\n");
        sb.append("  topoComponents: ").append(toIndentedString(topoComponents))
                .append("\n");
        sb.append("  label: ").append(toIndentedString(label)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n  ");
    }
}
