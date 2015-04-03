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

import com.effektif.workflow.api.mapper.Reader;
import com.effektif.workflow.api.mapper.Writer;


/**
 * @author Tom Baeyens
 */
public abstract class AbstractMapper {
  
  protected Mappings mappings = new Mappings();

  public Mappings getSubclassMappings() {
    return mappings;
  }

  public void setJsonMappings(Mappings mappings) {
    this.mappings = mappings;
  }

  public abstract Reader createReader();

  public abstract Writer createWriter();
}
