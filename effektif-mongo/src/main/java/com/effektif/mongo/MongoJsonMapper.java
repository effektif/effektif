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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.effektif.workflow.api.model.CaseId;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.workflow.AbstractWorkflow;
import com.effektif.workflow.api.workflowinstance.WorkflowInstance;
import com.effektif.workflow.impl.mapper.AbstractMapper;


/**
 * @author Tom Baeyens
 */
public class MongoJsonMapper extends AbstractMapper {
  
  Set<Field> documentIdFields = new HashSet<>();
  Set<Class<?>> objectIdClasses = new HashSet<>();
  
  public MongoJsonMapper() {
    documentIdFields.add(getField(AbstractWorkflow.class, "id"));
    documentIdFields.add(getField(WorkflowInstance.class, "id"));
    
    objectIdClasses.add(WorkflowId.class);
    objectIdClasses.add(WorkflowInstanceId.class);
    objectIdClasses.add(UserId.class);
    objectIdClasses.add(GroupId.class);
    objectIdClasses.add(EmailId.class);
    objectIdClasses.add(FileId.class);
    objectIdClasses.add(TaskId.class);
    objectIdClasses.add(CaseId.class);
  }
  
  public static Field getField(Class<?> clazz, String fieldName) {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }
  
  public MongoJsonReader createReader() {
    return new MongoJsonReader(mappings, this);
  }

  public MongoJsonWriter createWriter() {
    return new MongoJsonWriter(mappings, this);
  }

  public String getFieldName(Field field) {
    if (documentIdFields.contains(field)) {
      return "_id";
    }
    return field.getName();
  }

  public boolean isObjectIdClass(Class<?> idClass) {
    return objectIdClasses.contains(idClass);
  }
}
