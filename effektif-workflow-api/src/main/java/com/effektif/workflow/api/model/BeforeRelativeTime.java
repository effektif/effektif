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

import com.effektif.workflow.api.json.TypeName;


/**
 * @author Tom Baeyens
 */
@TypeName("before")
public class BeforeRelativeTime extends AfterRelativeTime {

  protected Integer duration;
  public String durationUnit;

  @Override
  public String getType() {
    return BEFORE;
  }

  @Override
  public Integer getDurationAsInt() {
    return duration!=null ? -duration : null;
  }
}
