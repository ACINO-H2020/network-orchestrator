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
 * Information, warning or error related to the submitted DISMI service.
 **/

/**
 * Information, warning or error related to the submitted DISMI service.
 */
@ApiModel(description = "Information, warning or error related to the " +
        "submitted DISMI service")
@javax.annotation.Generated(
        value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-10-12T14:35:41.641Z")
public class Issue {
    private String primitive = null;
    private SeverityEnum severity = null;
    private ErrorTypeEnum errorType = null;
    private String message = null;

    public Issue() {
        init();
    }

    public Issue(String p, Issue.SeverityEnum s, Issue.ErrorTypeEnum e, String m) {
        init();
        if (null != p) {
            primitive = p;
        }
        severity = s;
        errorType = e;
        if (null != m) {
            message = m;
        }
    }

    private void init() {
        primitive = "null";
        severity = SeverityEnum.INFO;
        errorType = ErrorTypeEnum.ERRORUNDEFINED;
        message = "";
    }

    public Issue primitive(String primitive) {
        this.primitive = primitive;
        return this;
    }

    /**
     * Get primitive.
     *
     * @return primitive
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("primitive")
    public String getPrimitive() {
        return primitive;
    }

    public void setPrimitive(String primitive) {
        this.primitive = primitive;
    }

    public Issue severity(SeverityEnum severity) {
        this.severity = severity;
        return this;
    }

    /**
     * Get severity.
     *
     * @return severity
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("severity")
    public SeverityEnum getSeverity() {
        return severity;
    }

    public void setSeverity(SeverityEnum severity) {
        this.severity = severity;
    }

    public Issue errorType(ErrorTypeEnum errorType) {
        this.errorType = errorType;
        return this;
    }

    /**
     * Get errorType.
     *
     * @return errorType
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("errorType")
    public ErrorTypeEnum getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorTypeEnum errorType) {
        this.errorType = errorType;
    }

    public Issue message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get message.
     *
     * @return message
     **/
    @ApiModelProperty(value = "")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Issue issue = (Issue) o;
        return Objects.equals(this.primitive, issue.primitive) &&
                Objects.equals(this.severity, issue.severity) &&
                Objects.equals(this.errorType, issue.errorType) &&
                Objects.equals(this.message, issue.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primitive, severity, errorType, message);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Issue {\n");
        sb.append("    primitive: ").append(toIndentedString(primitive)).append("\n");
        sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
        sb.append("    errorType: ").append(toIndentedString(errorType)).append("\n");
        sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

    public String toString(String tab) {
        String ret = tab
                + "From: \"" + primitive
                + "\", class:" + severity.toString()
                + ",  Type: " + errorType.toString();

        if (null != message) {
            /*if (message.length() > 0)*/
            ret += ",  \"" + message + "\"\n";
        }

        return ret;
    }

    /**
     * Gets or Sets severity.
     */
    public enum SeverityEnum {
        INFO("Info"),
        WARNING("Warning"),
        ERROR("Error");

        private String value;

        SeverityEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Gets or Sets errorType.
     */
    public enum ErrorTypeEnum {

        OBJECTISNOTASERVICE("ObjectIsNotAService"),
        OBJECTISNOTANINTENT("ObjectIsNotAnIntent"),
        OBJECTISNOTASUBJECT("ObjectIsNotASubject"),
        //Actions
        OBJECT_IS_NOT_A_CONNECTION("Object-is-not-a-connection"),
        OBJECT_IS_NOT_A_TREE("Object-is-not-a-tree"),
        OBJECTISNOTANACTION("ObjectIsNotAnAction"),
        OBJECTISNOTACONNECTION("ObjectIsNotAConnection"),
        OBJECTISNOTAPATH("ObjectIsNotAPath"),
        OBJECTISNOTATREE("ObjectIsNotATree"),
        OBJECTISNOTAMULTICAST("ObjectIsNotAMulticast"),
        OBJECTISNOTAMESH("ObjectIsNotAMesh"),
        OBJECTISNOTANAGGREGATE("ObjectIsNotAnAggregate"),
        ACTION_TYPE_NOT_FOUND("Action type not recognized"),
        ACTIONNAMEISNULL("ActionNameIsNull"),
        ACTIONNOTRECOGNISED("ActionNotRecognised"),
        // ConnectionPoints (Edges)
        INVALIDCONNECTIONPOINT("InvalidConnectionpoint"),
        // Constraints
        OBJECTISNOTABANDWIDTHCONSTRAINT("ObjectIsNotABandwidthConstraint"),
        OBJECTISNOTADELAYCONSTRAINT("ObjectIsNotADelayConstraint"),
        OBJECTISNOTACALENDARINGCONSTRAINT("ObjectIsNotACalendaringConstraint"),
        OBJECT_IS_NOT_A_CALENDARING_LIST("Object-is-not-a-calendaring-list"),
        OBJECT_IS_NOT_A_CONSTRAINT_LIST("Object-is-not-a-constraint-list"),
        OBJECT_IS_NOT_A_SELECTOR_LIST("Object-is-not-a-selector-list"),
        // Selectors
        OBJECTISNOTANIPSELECTOR("ObjectIsNotAnIPSelector"),
        OBJECTISNOTANETHSELECTOR("ObjectIsNotAnEthSelector"),
        OBJECTISNOTALAMBDASELECTOR("ObjectIsNotALambdaSelector"),
        OBJECTISNOTAGPRSSELECTOR("ObjectIsNotAGPRSSelector"),

        OBJECTISNOTACONNECTIONPOINT("ObjectIsNotAConnectionPoint"),
        OBJECTISNOTACONNECTIONPOINTEXTENDED("ObjectIsNotAConnectionPointExtended"),

        NOOPTIONALPARAMETER("NoOptionalParameter"),
        RESOLUTIONFAILED("ResolutionFailed"),

        INVALIDVALUE("InvalidValue"),
        VALUECANNOTBENEGATIVE("ValueCannotBeNegative"),

        NOSTARTDATE("NoStartDate"),
        NOSTOPDATE("NoStopDate"),
        DATEINVALID("DateInvalid"),

        BITRATEISNULL("BitrateIsNull"),
        JITTERISNULL("JitterIsNull"),
        LATENCYISNULL("LatencyIsNull"),

        OBJECTISNOTACONSTRAINT("ObjectIsNotAConstraint"),
        OBJECTISNOTASELECTOR("ObjectIsNotASelector"),

        CONSTRAINTNOTRECOGNISED("ConstraintNotRecognised"),
        //ConnectionPoint
        CONNECTIONPOINTISNOTUNIQUE("ConnectionPointIsNotUnique"),
        CONNECTIONPOINTHASNOENDPOINTS("ConnectionPointHasNoEndPoints"),
        CONNECTIONPOINTNOTFOUND("ConnectionPointNotFound"),
        ENDPOINTBELONGSTONOSUBCLASS("EndPointBelongsToNoSubclass"),
        NOT_SUPPORTED("not-supported"),

        NULLPOINTER("nullPointer"),
        NAMEMISSING("NameMissing"),
        IDMISSING("IdMissing"),
        OBJECTISNOTALIST("ObjectIsNotAList"),
        ERRORUNDEFINED("ErrorUndefined");

        private String value;

        ErrorTypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}

