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

package org.onosproject.orchestrator.dismi.primitives;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * This is the GPRS Selector.
 **/

/**
 * This is the GPRS Selector.
 */
@ApiModel(description = "This is the GPRS Selector.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class GprsSelector extends Selector {
    private Integer gtptEid = null;

    public GprsSelector() {
    }

    public GprsSelector gtptEid(Integer gtptEid) {
        this.gtptEid = gtptEid;
        return this;
    }

    /**
     * GPRS tunnel ID.
     *
     * @return gtptEid
     **/
    @ApiModelProperty(required = true, value = "GPRS tunnel ID")
    public Integer getGtptEid() {
        return gtptEid;
    }

    public void setGtptEid(Integer gtptEid) {
        this.gtptEid = gtptEid;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GprsSelector gprsSelector = (GprsSelector) o;
        return Objects.equals(this.gtptEid, gprsSelector.gtptEid) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gtptEid, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GprsSelector {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    gtptEid: ").append(toIndentedString(gtptEid)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

