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
package com.effektif.workflow.impl.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.serialization.json.JsonWriter;


/**
 * Implements the parts of JSON serialisation that are not specific to one of the concrete implementations in its
 * subclasses.
 *
 * TODO Rename to AbstractJsonWriter
 *
 * @author Tom Baeyens
 */
public abstract class AbstractWriter implements JsonWriter {

  protected static final Logger log = LoggerFactory.getLogger(AbstractWriter.class);
  
  protected Boolean isPretty;
  protected Mappings mappings;

  public AbstractWriter() {
    this(new Mappings());
  }

  public AbstractWriter(Mappings mappings) {
    this.mappings = mappings;
  }
  
  public Boolean isPretty() {
    return Boolean.TRUE.equals(isPretty) || mappings.isPretty();
  }

  public void setPretty(Boolean isPretty) {
    this.isPretty = isPretty;
  }

  public void pretty() {
    this.isPretty = true;
  }
}
