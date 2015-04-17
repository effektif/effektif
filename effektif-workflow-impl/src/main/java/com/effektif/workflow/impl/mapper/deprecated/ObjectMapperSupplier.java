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
package com.effektif.workflow.impl.mapper.deprecated;

import org.joda.time.LocalDateTime;

import com.effektif.workflow.api.model.CaseId;
import com.effektif.workflow.api.model.EmailId;
import com.effektif.workflow.api.model.FileId;
import com.effektif.workflow.api.model.GroupId;
import com.effektif.workflow.api.model.TaskId;
import com.effektif.workflow.api.model.UserId;
import com.effektif.workflow.api.model.WorkflowId;
import com.effektif.workflow.api.model.WorkflowInstanceId;
import com.effektif.workflow.api.workflowinstance.VariableInstance;
import com.effektif.workflow.impl.configuration.Brewery;
import com.effektif.workflow.impl.configuration.Supplier;
import com.effektif.workflow.impl.data.DataTypeService;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * @author Tom Baeyens
 */
public class ObjectMapperSupplier implements Supplier {

  @Override
  public Object supply(Brewery brewery) {
    final ObjectMapper objectMapper = new ObjectMapper();
    brewery.brew(objectMapper);
    
    final DataTypeService dataTypeService = brewery.get(DataTypeService.class);
    
    objectMapper
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .setVisibility(PropertyAccessor.ALL, Visibility.NONE)
      .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
      .setSerializationInclusion(Include.NON_EMPTY);
  
    SimpleModule module = new SimpleModule();

    module.addSerializer(new IdSerializer());
    module.addDeserializer(UserId.class, new UserIdDeserializer());
    module.addDeserializer(GroupId.class, new GroupIdDeserializer());
    module.addDeserializer(FileId.class, new FileIdDeserializer());
    module.addDeserializer(EmailId.class, new EmailIdDeserializer());
    module.addDeserializer(TaskId.class, new TaskIdDeserializer());
    module.addDeserializer(CaseId.class, new CaseIdDeserializer());
    module.addDeserializer(WorkflowId.class, new WorkflowIdDeserializer());
    module.addDeserializer(WorkflowInstanceId.class, new WorkflowInstanceIdDeserializer());

    module.addSerializer(new LocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer< ? > modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer< ? > deserializer) {
        if (beanDesc.getBeanClass() == VariableInstance.class) {
          return new VariableInstanceDeserializer(deserializer, dataTypeService, objectMapper);
        }
        return deserializer;
      }
    });
    objectMapper.registerModule(module);
    
//    ConditionService conditionService = brewery.getOpt(ConditionService.class);
//    if (conditionService!=null) {
//      conditionService.registerSubclasses(objectMapper);
//    }
    
    return objectMapper;
  }
}
