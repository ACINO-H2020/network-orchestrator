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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;


/**
 * The SecurityConstraint allows requesting a protected (encrypted) connection.
 **/

@ApiModel(description = "The SecurityConstraint allows requesting a " +
        "protected (encrypted) connection.")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")
public class SecurityConstraint extends Constraint {

    private Boolean encryption = false;
    private EncStrengthEnum encStrength = EncStrengthEnum.AES128;
    private Boolean sensitive = false;
    private String sensitiveSpec = null;
    private Boolean integrity = false;
    private IntStrengthEnum intStrength = IntStrengthEnum.SHA256;

    /**
     **/
    public SecurityConstraint() {
    }

    /**
     * true or false - boolean.
     **/
    public SecurityConstraint encryption(Boolean encryption) {
        this.encryption = encryption;
        return this;
    }

    @ApiModelProperty(required = true, value = "true or false")
    @JsonProperty("encryption")
    public Boolean getEncryption() {
        return encryption;
    }

    public void setEncryption(Boolean encryption) {
        this.encryption = encryption;
    }

    /**
     * Specify the type of encryption: cypher and key length.
     **/
    public SecurityConstraint encStrength(EncStrengthEnum encStrength) {
        this.encStrength = encStrength;
        return this;
    }

    @ApiModelProperty(value = "Specify the type of encryption: " +
            "cypher and key length")
    @JsonProperty("enc_strength")
    public EncStrengthEnum getEncStrength() {
        return encStrength;
    }

    public void setEncStrength(EncStrengthEnum encStrength) {
        this.encStrength = encStrength;
    }

    /**
     * Whether some regions or operators should be avoided for this connection.
     **/
    public SecurityConstraint sensitive(Boolean sensitive) {
        this.sensitive = sensitive;
        return this;
    }

    @ApiModelProperty(value = "Whether some regions or operators should be " +
            "avoided for this connection")
    @JsonProperty("sensitive")
    public Boolean getSensitive() {
        return sensitive;
    }

    public void setSensitive(Boolean sensitive) {
        this.sensitive = sensitive;
    }

    /**
     * List of flags describing the regions to keep the traffic inside
     * of/outside of (syntax to be defined).
     **/
    public SecurityConstraint sensitiveSpec(String sensitiveSpec) {
        this.sensitiveSpec = sensitiveSpec;
        return this;
    }

    @ApiModelProperty(value = "List of flags describing the regions to keep " +
            "the traffic inside of/outside of " +
            "(syntax to be defined)")
    @JsonProperty("sensitive_spec")
    public String getSensitiveSpec() {
        return sensitiveSpec;
    }

    public void setSensitiveSpec(String sensitiveSpec) {
        this.sensitiveSpec = sensitiveSpec;
    }

    /**
     * true or false - boolean.
     **/
    public SecurityConstraint integrity(Boolean integrity) {
        this.integrity = integrity;
        return this;
    }

    @ApiModelProperty(value = "true or false")
    @JsonProperty("integrity")
    public Boolean getIntegrity() {
        return integrity;
    }

    public void setIntegrity(Boolean integrity) {
        this.integrity = integrity;
    }

    /**
     * Specify the type of integrity algorithm.
     **/
    public SecurityConstraint intStrength(IntStrengthEnum intStrength) {
        this.intStrength = intStrength;
        return this;
    }

    @ApiModelProperty(value = "Specify the type of integrity algorithm")
    @JsonProperty("int_strength")
    public IntStrengthEnum getIntStrength() {
        return intStrength;
    }

    public void setIntStrength(IntStrengthEnum intStrength) {
        this.intStrength = intStrength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecurityConstraint securityConstraint = (SecurityConstraint) o;
        return Objects.equals(encryption, securityConstraint.encryption) &&
                Objects.equals(encStrength, securityConstraint.encStrength) &&
                Objects.equals(sensitive, securityConstraint.sensitive) &&
                Objects.equals(sensitiveSpec, securityConstraint.sensitiveSpec) &&
                Objects.equals(integrity, securityConstraint.integrity) &&
                Objects.equals(intStrength, securityConstraint.intStrength) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encryption, encStrength,
                            sensitive, sensitiveSpec, integrity, intStrength);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SecurityConstraint {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    encryption: ").append(toIndentedString(encryption)).append("\n");
        sb.append("    encStrength: ").append(toIndentedString(encStrength)).append("\n");
        sb.append("    sensitive: ").append(toIndentedString(sensitive)).append("\n");
        sb.append("    sensitiveSpec: ").append(toIndentedString(sensitiveSpec)).append("\n");
        sb.append("    integrity: ").append(toIndentedString(integrity)).append("\n");
        sb.append("    intStrength: ").append(toIndentedString(intStrength)).append("\n");
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

    /**
     * Specify the type of encryption: cypher and key length.
     */
    public enum EncStrengthEnum {
        AES128("AES128"),
        AES256("AES256");

        private String value;

        EncStrengthEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Specify the type of integrity algorithm.
     */
    public enum IntStrengthEnum {
        SHA256("SHA256"),
        SHA384("SHA384");

        private String value;

        IntStrengthEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

