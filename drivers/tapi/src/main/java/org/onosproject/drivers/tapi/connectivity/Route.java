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

package org.onosproject.drivers.tapi.connectivity;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.onosproject.drivers.tapi.common.LocalClass;
import org.onosproject.drivers.tapi.common.NameAndValue;
import org.onosproject.drivers.tapi.common.UniversalId;

/**
 * The FC Route (FcRoute) object class models the individual routes of an FC. 
 * The route of an FC object is represented by a list of FCs at a lower level. 
 * Note that depending on the service supported by an FC, an the FC can have multiple routes.
 */
@JsonInclude(Include.NON_NULL)
public class Route extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<UniversalId> lowerConnection;


  @JsonCreator
  public Route (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("lower-connection") List<UniversalId> lowerConnection){
    super(localId, name);
    this.lowerConnection = lowerConnection != null ? ImmutableList.copyOf(lowerConnection) : ImmutableList.of();
  }


  @JsonProperty("lower-connection")
  public List<UniversalId> getLowerConnection(){
    return this.lowerConnection;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), lowerConnection);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Route that = (Route) o;
    return super.equals(o) &&
       Objects.equals(this.lowerConnection, that.lowerConnection);
  }

}