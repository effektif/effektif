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

import java.util.List;
import java.util.Map;

import com.effektif.workflow.api.workflow.Workflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.json.JsonObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * A facade for API object serialisation and deserialisation to and from the MongoDB JSON variant.
 *
 * @author Tom Baeyens
 */
public class MongoObjectMapper extends JsonObjectMapper {

  public MongoObjectMapper() {
    this.mappings.setJsonFieldName(Workflow.class, "id", "_id");
    this.mappings.setJsonFieldName(WorkflowInstance.class, "id", "_id");
    this.mappings.registerTypeMapperFactory(new WorkflowIdMongoMapper());
    this.mappings.registerTypeMapperFactory(new WorkflowInstanceIdMongoMapper());
  }
  
  public <T> BasicDBObject write(T bean) {
    return (BasicDBObject) super.write(bean);
  }

  @Override
  protected Map<String, Object> newObjectMap() {
    return new BasicDBObject();
  }

  @Override
  public List<Object> newArray() {
    return new BasicDBList();
  }
}
