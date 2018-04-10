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

import java.util.Objects;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The univeral ID value where the mechanism for generation is defned by some authority not directly referenced in the structure.
 * UUID here uses string representation as defined in RFC 4122.  The canonical representation uses lowercase characters.
 * Pattern: [0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-' + '[0-9a-fA-F]{4}-[0-9a-fA-F]{12} 
 * Example of a UUID in string representation: f81d4fae-7dec-11d0-a765-00a0c91e6bf6
 */
public class UniversalId implements Serializable {


  private static final long serialVersionUID = 1L;
  private final String universalId;

  public UniversalId(String universalId) {
    this.universalId = universalId;
  }

  @JsonValue
  public String getUniversalId(){
    return universalId;
  }

  @Override
  public int hashCode() {
    return universalId.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UniversalId that = (UniversalId) o;
    return Objects.equals(this.universalId, that.universalId);
  }

}