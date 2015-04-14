/*
 * Copyright 2014 Effektif GmbH.
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
 * limitations under the License.
 */
package com.effektif.workflow.api.types;

import java.util.ArrayList;
import java.util.List;

import com.effektif.workflow.api.mapper.JsonReader;
import com.effektif.workflow.api.mapper.JsonWriter;
import com.effektif.workflow.api.mapper.TypeName;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * Represents decision buttons at the end of a
 * {@link com.effektif.workflow.api.activities.UserTask} form -
 * a choice that completes the task.
 * 
 * @author Tom Baeyens
 */
@JsonTypeName("decision")
@TypeName("decision")
public class ChoiceType extends TextType {

  protected List<ChoiceOption> options;
  
//  @Override
//  public void readJson(JsonReader r) {
//    options = r.readList("options");
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    super.writeJson(w);
//    w.writeList("options", options);
//  }

  public List<ChoiceOption> getOptions() {
    return options;
  }
  public void setOptions(List<ChoiceOption> options) {
    this.options = options;
  }
  public ChoiceType option(String option) {
    if (options==null) {
      options = new ArrayList<>();
    }
    options.add(new ChoiceOption().id(option));
    return this;
  }
}
