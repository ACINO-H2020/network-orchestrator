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

package org.onosproject.drivers.tapi.topology;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Transfer intergrity characteristic covers expected/specified/acceptable characteristic of degradation of the transfered signal.
 * It includes all aspects of possible degradation of signal content as well as any damage of any form to the total TopologicalEntity and to the carried signals.
 * Note that the statement is of total impact to the TopologicalEntity so any partial usage of the TopologicalEntity (e.g. a signal that does not use full capacity) will only suffer its portion of the impact.
 */
@JsonInclude(Include.NON_NULL)
public class TransferIntegrityPac implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Describes the degree to which the signal propagated can be errored. 
   * Applies to TDM systems as the errored signal will be propagated and not packet as errored packets will be discarded.
   */
  private final String errorCharacteristic;

  /**
   * Describes the acceptable characteristic of lost packets where loss may result from discard due to errors or overflow.
   * Applies to packet systems and not TDM (as for TDM errored signals are propagated unless grossly errored and overflow/underflow turns into timing slips).
   */
  private final String lossCharacteristic;

  /**
   * Primarily applies to packet systems where a packet may be delivered more than once (in fault recovery for example). 
   * It can also apply to TDM where several frames may be received twice due to switching in a system with a large differential propagation delay.
   */
  private final String repeatDeliveryCharacteristic;

  /**
   * Describes the degree to which packets will be delivered out of sequence.
   * Does not apply to TDM as the TDM protocols maintain strict order.
   */
  private final String deliveryOrderCharacteristic;

  /**
   * Describes the duration for which there may be no valid signal propagated.
   */
  private final String unavailableTimeCharacteristic;

  /**
   * Describes the effect of any server integrity enhancement process on the characteristics of the TopologicalEntity.
   */
  private final String serverIntegrityProcessCharacteristic;


  @JsonCreator
  public TransferIntegrityPac (
    @JsonProperty("error-characteristic") String errorCharacteristic,
    @JsonProperty("loss-characteristic") String lossCharacteristic,
    @JsonProperty("repeat-delivery-characteristic") String repeatDeliveryCharacteristic,
    @JsonProperty("delivery-order-characteristic") String deliveryOrderCharacteristic,
    @JsonProperty("unavailable-time-characteristic") String unavailableTimeCharacteristic,
    @JsonProperty("server-integrity-process-characteristic") String serverIntegrityProcessCharacteristic){
    this.errorCharacteristic = errorCharacteristic;
    this.lossCharacteristic = lossCharacteristic;
    this.repeatDeliveryCharacteristic = repeatDeliveryCharacteristic;
    this.deliveryOrderCharacteristic = deliveryOrderCharacteristic;
    this.unavailableTimeCharacteristic = unavailableTimeCharacteristic;
    this.serverIntegrityProcessCharacteristic = serverIntegrityProcessCharacteristic;
  }


  @JsonProperty("error-characteristic")
  public String getErrorCharacteristic(){
    return this.errorCharacteristic;
  }

  @JsonProperty("loss-characteristic")
  public String getLossCharacteristic(){
    return this.lossCharacteristic;
  }

  @JsonProperty("repeat-delivery-characteristic")
  public String getRepeatDeliveryCharacteristic(){
    return this.repeatDeliveryCharacteristic;
  }

  @JsonProperty("delivery-order-characteristic")
  public String getDeliveryOrderCharacteristic(){
    return this.deliveryOrderCharacteristic;
  }

  @JsonProperty("unavailable-time-characteristic")
  public String getUnavailableTimeCharacteristic(){
    return this.unavailableTimeCharacteristic;
  }

  @JsonProperty("server-integrity-process-characteristic")
  public String getServerIntegrityProcessCharacteristic(){
    return this.serverIntegrityProcessCharacteristic;
  }


  @Override
  public int hashCode() {
    return Objects.hash(errorCharacteristic, lossCharacteristic, repeatDeliveryCharacteristic, deliveryOrderCharacteristic, unavailableTimeCharacteristic, serverIntegrityProcessCharacteristic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransferIntegrityPac that = (TransferIntegrityPac) o;
    return Objects.equals(this.errorCharacteristic, that.errorCharacteristic) &&
       Objects.equals(this.lossCharacteristic, that.lossCharacteristic) &&
       Objects.equals(this.repeatDeliveryCharacteristic, that.repeatDeliveryCharacteristic) &&
       Objects.equals(this.deliveryOrderCharacteristic, that.deliveryOrderCharacteristic) &&
       Objects.equals(this.unavailableTimeCharacteristic, that.unavailableTimeCharacteristic) &&
       Objects.equals(this.serverIntegrityProcessCharacteristic, that.serverIntegrityProcessCharacteristic);
  }

}