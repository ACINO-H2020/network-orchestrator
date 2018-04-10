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

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.NameAndValue;

/**
 * The TAPI GlobalComponent serves as the super class for all TAPI entities that can be directly retrieved by their ID. As such, these are first class entities and their ID is expected to be globally unique. 
 */
@JsonInclude(Include.NON_NULL)
public class LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final String localId;

  /**
   * List of names. A property of an entity with a value that is unique in some namespace but may change during the life of the entity. A name carries no semantics with respect to the purpose of the entity.
   */
  private final List<NameAndValue> name;


  @JsonCreator
  public LocalClass (
    @JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name){
    this.localId = localId;
    this.name = name != null ? ImmutableList.copyOf(name) : ImmutableList.<NameAndValue>of();
  }


  @JsonProperty("local-id")
  public String getLocalId(){
    return this.localId;
  }

  @JsonProperty("name")
  public List<NameAndValue> getName(){
    return this.name;
  }


  @Override
  public int hashCode() {
    return Objects.hash(localId, name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocalClass that = (LocalClass) o;
    return Objects.equals(this.localId, that.localId) &&
       Objects.equals(this.name, that.name);
  }

}