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
import org.onosproject.drivers.tapi.common.UniversalId;

/**
 * The TAPI GlobalComponent serves as the super class for all TAPI entities that can be directly retrieved by their ID. As such, these are first class entities and their ID is expected to be globally unique. 
 */
@JsonInclude(Include.NON_NULL)
public class GlobalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * UUID: An identifier that is universally unique within an identifier space, where the identifier space is itself globally unique, and immutable. An UUID carries no semantics with respect to the purpose or state of the entity.
   * UUID here uses string representation as defined in RFC 4122.  The canonical representation uses lowercase characters.
   * Pattern: [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-' + '[0-9a-fA-F]{4}-[0-9a-fA-F]{12} 
   * Example of a UUID in string representation: f81d4fae-7dec-11d0-a765-00a0c91e6bf6
   */
  private final UniversalId uuid;

  /**
   * List of names. A property of an entity with a value that is unique in some namespace but may change during the life of the entity. A name carries no semantics with respect to the purpose of the entity.
   */
  private final List<NameAndValue> name;

  /**
   * List of labels.A property of an entity with a value that is not expected to be unique and is allowed to change. A label carries no semantics with respect to the purpose of the entity and has no effect on the entity behavior or state.
   */
  private final List<NameAndValue> label;


  @JsonCreator
  public GlobalClass (
    @JsonProperty("uuid") UniversalId uuid,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("label") List<NameAndValue> label){
    this.uuid = uuid;
    this.name = name != null ? ImmutableList.copyOf(name) : ImmutableList.<NameAndValue>of();
    this.label = label != null ? ImmutableList.copyOf(label) : ImmutableList.<NameAndValue>of();
  }


  @JsonProperty("uuid")
  public UniversalId getUuid(){
    return this.uuid;
  }

  @JsonProperty("name")
  public List<NameAndValue> getName(){
    return this.name;
  }

  @JsonProperty("label")
  public List<NameAndValue> getLabel(){
    return this.label;
  }


  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, label);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalClass that = (GlobalClass) o;
    return Objects.equals(this.uuid, that.uuid) &&
       Objects.equals(this.name, that.name) &&
       Objects.equals(this.label, that.label);
  }

}