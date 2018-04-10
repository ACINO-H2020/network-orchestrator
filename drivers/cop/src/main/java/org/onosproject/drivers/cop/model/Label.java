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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Label {

    public enum LabelTypeEnum {
        GMPLS_FIXED, GMPLS_FLEXI
    }

    private final LabelTypeEnum labelType;
    private final Integer labelValue;

    public Label(@JsonProperty("labelType") LabelTypeEnum labelType,
                 @JsonProperty("labelValue") Integer labelValue) {
        this.labelType = labelType;
        this.labelValue = labelValue;
    }

    @JsonProperty("labelType")
    public LabelTypeEnum getLabelType() {
        return labelType;
    }

    @JsonProperty("labelValue")
    public Integer getLabelValue() {
        return labelValue;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Label label = (Label) o;

        return Objects.equals(labelType, label.labelType)
                && Objects.equals(labelValue, label.labelValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelType, labelValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Label {\n");
        sb.append("  labelType: ").append(toIndentedString(labelType))
                .append("\n");
        sb.append("  labelValue: ").append(toIndentedString(labelValue))
                .append("\n");
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
