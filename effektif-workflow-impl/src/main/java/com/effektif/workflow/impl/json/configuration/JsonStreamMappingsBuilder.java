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
package com.effektif.workflow.impl.json.configuration;

import com.effektif.workflow.api.model.DataContainer;
import com.effektif.workflow.impl.json.MappingsBuilder;
import com.effektif.workflow.impl.json.types.*;
import com.effektif.workflow.impl.workflowinstance.BaseInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class JsonStreamMappingsBuilder extends MappingsBuilder {

  public JsonStreamMappingsBuilder() {
    configureDefaults();
    typeMapperFactory(new LocalDateTimeStreamMapper());
    typeMapperFactory(new DateStreamMapper());
    typeMapperFactory(new UriMapper());
    typeMapperFactory(new WorkflowIdStreamMapper());
    typeMapperFactory(new WorkflowInstanceIdStreamMapper());
    ignore(BaseInstanceImpl.class, "transientProperties");
    ignore(DataContainer.class, "transientData");
  }
}
