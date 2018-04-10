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

package org.onosproject.orchestrator.dismi.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.onosproject.orchestrator.dismi.utils.JsonMapper;

import javax.ws.rs.core.Response;

@javax.xml.bind.annotation.XmlRootElement
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaJerseyServerCodegen",
        date = "2016-03-22T15:29:51.886Z")

public class ApiResponseMessage {

    ErrorCode errorCode;
    String message;

    public ApiResponseMessage() {
    }

    public ApiResponseMessage(ErrorCode code, String message) {
        this.errorCode = code;
        this.message = message;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("errorCode")
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode code) {
        this.errorCode = code;
    }

    @ApiModelProperty(required = true, value = "")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toJson() {
        try {
            JsonMapper jsonMapper = new JsonMapper();
            return jsonMapper.toJson(this);
        } catch (Exception e) {
            return "" + Response.Status.BAD_REQUEST;
        }
    }

    public enum ErrorCode {
        BODY_MALFORMED(-1, "The body content is malformed"),
        EMPTY_PARAMETER(-2, "A parameter is null or empty"),
        PROCESSING_ERROR(-3, "Error while processing the request"),
        OBJECT_ALREADY_EXISTS(-4, "Object already exists"),
        OBJECT_NOT_FOUND(-5, "Requested object not found");

        private int value;
        private String reasonPhrase;

        ErrorCode(int value, String reasonPhrase) {
            this.value = value;
            this.reasonPhrase = reasonPhrase;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
