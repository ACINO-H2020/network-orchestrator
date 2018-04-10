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

package org.onosproject.drivers.tapi.common;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A scoped name-value pair
 */
@JsonInclude(Include.NON_NULL)
public class NameAndValue implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The name of the value. The value need not have a name.
   */
  private final String valueName;

  /**
   * The value
   */
  private final String value;


  @JsonCreator
  public NameAndValue (
    @JsonProperty("value-name") String valueName,
    @JsonProperty("value") String value){
    this.valueName = valueName;
    this.value = value;
  }


  @JsonProperty("value-name")
  public String getValueName(){
    return this.valueName;
  }

  @JsonProperty("value")
  public String getValue(){
    return this.value;
  }


  @Override
  public int hashCode() {
    return Objects.hash(valueName, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NameAndValue that = (NameAndValue) o;
    return Objects.equals(this.valueName, that.valueName) &&
       Objects.equals(this.value, that.value);
  }

}