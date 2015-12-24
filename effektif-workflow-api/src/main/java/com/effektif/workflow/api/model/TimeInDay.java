/* Copyright (c) 2014, Effektif GmbH.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
package com.effektif.workflow.api.model;


/**
 * @author Tom Baeyens
 */
public class TimeInDay {

  /** in 24 hour format */
  protected Integer hour;
  protected Integer minutes;

  public Integer getHour() {
    return this.hour;
  }
  public void setHour(Integer hour) {
    this.hour = hour;
  }
  public TimeInDay hour(Integer hour) {
    this.hour = hour;
    return this;
  }
  
  public Integer getMinutes() {
    return this.minutes;
  }
  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
  }
  public TimeInDay minutes(Integer minutes) {
    this.minutes = minutes;
    return this;
  }
  
  public String toString() {
    return (hour!=null?hour:"")+":"+minutes; 
  }
}
