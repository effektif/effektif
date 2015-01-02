/* Copyright 2014 Effektif GmbH.
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
package com.effektif.workflow.impl.type;

import java.util.Map;

import com.effektif.workflow.api.variables.JavaBean;
import com.effektif.workflow.impl.definition.VariableImpl;
import com.effektif.workflow.impl.definition.WorkflowValidator;
import com.effektif.workflow.impl.json.JsonService;
import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("javaBean")
public class JavaBeanType extends AbstractDataType<JavaBean> {
  
  public Class<?> javaClass;
  public JsonService jsonService;

  public JavaBeanType() {
    super(JavaBean.class);
  }

  @Override
  public void validate(VariableImpl variable, JavaBean javaBean, WorkflowValidator validator) {
    this.jsonService = validator.getServiceRegistry().getService(JsonService.class);
    this.javaClass = javaBean.getJavaClass();
  }

  @Override
  public void validateInternalValue(Object internalValue) throws InvalidValueException {
    if (internalValue==null) {
      return;
    }
    if (! javaClass.isAssignableFrom(internalValue.getClass())) {
      throw new InvalidValueException("Invalid internal value: was "+internalValue+" ("+internalValue.getClass().getName()+"), expected "+javaClass.getName());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convertJsonToInternalValue(Object jsonValue) throws InvalidValueException {
    if (jsonValue==null) return null;
    if (Map.class.isAssignableFrom(jsonValue.getClass())) {
      return jsonService.jsonMapToObject((Map<String,Object>)jsonValue, javaClass);
    }
    throw new InvalidValueException("Couldn't convert json: "+jsonValue+" ("+jsonValue.getClass().getName()+")");
  }
  
  @Override
  public Object convertInternalToJsonValue(Object internalValue) {
    if (internalValue==null) return null;
    return jsonService.objectToJsonMap(internalValue);
  }
  
  @Override
  public Class< ? > getValueType() {
    return javaClass;
  }
  
  public JsonService getJsonService() {
    return jsonService;
  }
  
  public void setJsonService(JsonService jsonService) {
    this.jsonService = jsonService;
  }
}
