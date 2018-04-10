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

import org.onosproject.drivers.tapi.common.DateAndTime;


@JsonInclude(Include.NON_NULL)
public class TimeRange implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final DateAndTime endTime;

  
  private final DateAndTime startTime;


  @JsonCreator
  public TimeRange (
    @JsonProperty("end-time") DateAndTime endTime,
    @JsonProperty("start-time") DateAndTime startTime){
    this.endTime = endTime;
    this.startTime = startTime;
  }


  @JsonProperty("end-time")
  public DateAndTime getEndTime(){
    return this.endTime;
  }

  @JsonProperty("start-time")
  public DateAndTime getStartTime(){
    return this.startTime;
  }


  @Override
  public int hashCode() {
    return Objects.hash(endTime, startTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TimeRange that = (TimeRange) o;
    return Objects.equals(this.endTime, that.endTime) &&
       Objects.equals(this.startTime, that.startTime);
  }

}