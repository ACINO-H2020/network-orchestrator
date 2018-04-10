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

package org.onosproject.orchestrator.dismi.primitives.extended;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.onosproject.orchestrator.dismi.primitives.IPSelector;

import java.util.List;
import java.util.Objects;

/**
 * Created by aghafoor on 2016-10-14.
 */
@ApiModel(description = "A IPSelectorExtended is an inernal primitive that extends the IPSelector with" +
        " its associated attributes and add resolved attributes. It is created during the resolution phase of an " +
        "intent validation and resolution process.")
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-06T17:39:51.886Z")
public class IPSelectorExtended extends IPSelector {

    private IPAddress sourceAddress;
    private IPAddress destinationAddress;
    private List<String> ipdscpList;

    public IPSelectorExtended(IPSelector ipSelector) {
        super.setIpSrcAddr(ipSelector.getIpSrcAddr());
        super.setIpDestAddr(ipSelector.getIpDestAddr());
        super.setIpProtocol(ipSelector.getIpProtocol());
        super.setIpTos(ipSelector.getIpTos());
        super.setIpDscp(ipSelector.getIpDscp());
    }

    @ApiModelProperty(value = "This is not exposed to the client application")
    @JsonProperty("ipSrcAddr")
    public IPAddress getSourceAddressExt() {
        return sourceAddress;
    }

    public void setSourceAddressExt(IPAddress sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    @ApiModelProperty(value = "This is not exposed to the client application")
    @JsonProperty("ipDestAddr")
    public IPAddress getDestinationAddressExt() {
        return destinationAddress;
    }

    public void setDestinationAddressExt(IPAddress destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    @ApiModelProperty(value = "This is not exposed to the client application")
    @JsonProperty("DscpList")
    public List<String> getIPDscpListExt() {
        return ipdscpList;
    }

    public void setIPDscpListExt(List<String> dscpList) {
        this.ipdscpList = dscpList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IPSelectorExtended ipSelectorExtended = (IPSelectorExtended) o;

        return Objects.equals(getIPDscpListExt(), ipSelectorExtended.getIPDscpListExt()) &&
                Objects.equals(getSourceAddressExt(), ipSelectorExtended.getSourceAddressExt()) &&
                Objects.equals(getDestinationAddressExt(), ipSelectorExtended.getDestinationAddressExt());

    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceAddress, destinationAddress, ipdscpList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class IPSelectorExtended {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    ipSrcAddr: ").append(toIndentedString(this.getSourceAddressExt())).append("\n");
        sb.append("    ipDestAddr: ").append(toIndentedString(this.getDestinationAddressExt())).append("\n");
        sb.append("    ipDscpList: ").append(toIndentedString(this.getIPDscpListExt())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}



