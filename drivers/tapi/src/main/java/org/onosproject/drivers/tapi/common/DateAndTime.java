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
 * This primitive type defines the date and time according to the following structure:
 * yyyyMMddhhmmss.s[Z|{+|-}HHMm] where:
 * yyyy    0000..9999    year
 * MM    01..12            month
 * dd        01..31            day
 * hh        00..23            hour
 * mm    00..59            minute
 * ss        00..59            second
 * s        .0...9            tenth of second (set to .0 if EMS or NE cannot support this granularity)
 * Z        Z                indicates UTC (rather than local time)
 * {+|-}    + or -            delta from UTC
 * HH        00..23            time zone difference in hours
 * Mm    00..59            time zone difference in minutes.
 */
public class DateAndTime implements Serializable {


  private static final long serialVersionUID = 1L;
  private final String dateAndTime;

  public DateAndTime(String dateAndTime) {
    this.dateAndTime = dateAndTime;
  }

  @JsonValue
  public String getDateAndTime(){
    return dateAndTime;
  }

  @Override
  public int hashCode() {
    return dateAndTime.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DateAndTime that = (DateAndTime) o;
    return Objects.equals(this.dateAndTime, that.dateAndTime);
  }

}