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
package com.effektif.mongo;

import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.json.MappingsBuilder;
import com.effektif.workflow.impl.json.types.DateDateMapper;
import com.effektif.workflow.impl.json.types.LocalDateTimeDateMapper;
import com.effektif.workflow.impl.json.types.UriMapper;
import com.effektif.workflow.impl.workflowinstance.BaseInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class MongoObjectMappingsBuilder extends MappingsBuilder {

  public MongoObjectMappingsBuilder() {
    configureDefaults();
    typeMapperFactory(new ObjectIdMapper());
    typeMapperFactory(new DateDateMapper());
    typeMapperFactory(new LocalDateTimeDateMapper());
    typeMapperFactory(new WorkflowIdMongoMapper());
    typeMapperFactory(new WorkflowInstanceIdMongoMapper());
    typeMapperFactory(new UriMapper());
    jsonFieldName(AbstractWorkflow.class, "id", "_id");
    jsonFieldName(WorkflowInstance.class, "id", "_id");
    ignore(BaseInstanceImpl.class, "transientProperties");
  }
}
