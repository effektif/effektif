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
package com.effektif.workflow.api.workflow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.effektif.workflow.api.mapper.XmlElement;


/** 
 * common data fields for any object in a {@link Workflow}. 
 * 
 * @author Tom Baeyens
 */
public abstract class Element extends Extensible {
  
  public static final Set<String> INVALID_PROPERTY_KEYS = new HashSet<>(Arrays.asList(
          "name", "description", "bpmn"));

  protected String name;
  protected String description;
  protected XmlElement bpmn;

  /** human readable label used when visually displaying the element.
   * This corresponds to the BPMN name attribute. */
  public String getName() {
    return this.name;
  }
  /** @see #getName() */
  public void setName(String name) {
    this.name = name;
  }
  /** @see #getName() */
  public Element name(String name) {
    this.name = name;
    return this;
  }

  /** longer human readable description for the element.
   * This corresponds to the BPMN documentation/ attribute. */
  public String getDescription() {
    return this.description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Element description(String description) {
    this.description = description;
    return this;
  }

  /** This way the BPMN parser can stick all non-parsed BPMN XML in the object. 
   * BPMN requires that all XML in an imported file that is not understood should 
   * be kept and included into the exported BPMN file. */
  public XmlElement getBpmn() {
    return this.bpmn;
  }
  /** @see #getBpmn() */
  public void setBpmn(XmlElement bpmn) {
    this.bpmn = bpmn;
  }
  
  @Override
  protected void checkPropertyKey(String key) {
    checkPropertyKey(key, INVALID_PROPERTY_KEYS);
  }
}
