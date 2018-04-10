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
import org.onosproject.drivers.tapi.connectivity.ControlParametersPac;
import org.onosproject.drivers.tapi.connectivity.Switch;

/**
 * Represents the capability to control and coordinate switches, to add/delete/modify FCs and to add/delete/modify LTPs/LPs so as to realize a protection scheme.
 */
@JsonInclude(Include.NON_NULL)
public class SwitchControl extends LocalClass implements Serializable {

  private static final long serialVersionUID = 1L;

  
  private final List<String> subSwitchControl;

  
  private final List<Switch> _switch;

  
  private final ControlParametersPac controlParameters;


  @JsonCreator
  public SwitchControl (@JsonProperty("local-id") String localId,
    @JsonProperty("name") List<NameAndValue> name,
    @JsonProperty("sub-switch-control") List<String> subSwitchControl,
    @JsonProperty("switch") List<Switch> _switch,
    @JsonProperty("control-parameters") ControlParametersPac controlParameters){
    super(localId, name);
    this.subSwitchControl = subSwitchControl != null ? ImmutableList.copyOf(subSwitchControl) : ImmutableList.of();
    this._switch = _switch != null ? ImmutableList.copyOf(_switch) : ImmutableList.<Switch>of();
    this.controlParameters = controlParameters;
  }


  @JsonProperty("sub-switch-control")
  public List<String> getSubSwitchControl(){
    return this.subSwitchControl;
  }

  @JsonProperty("switch")
  public List<Switch> getSwitch(){
    return this._switch;
  }

  @JsonProperty("control-parameters")
  public ControlParametersPac getControlParameters(){
    return this.controlParameters;
  }


  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), subSwitchControl, _switch, controlParameters);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SwitchControl that = (SwitchControl) o;
    return super.equals(o) &&
       Objects.equals(this.subSwitchControl, that.subSwitchControl) &&
       Objects.equals(this._switch, that._switch) &&
       Objects.equals(this.controlParameters, that.controlParameters);
  }

}