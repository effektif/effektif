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
package com.effektif.workflow.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.form.Form;
import com.effektif.workflow.api.form.FormField;
import com.effektif.workflow.api.form.FormInstance;
import com.effektif.workflow.api.form.FormInstanceField;
import com.effektif.workflow.api.workflow.Binding;
import com.effektif.workflow.impl.workflow.BindingImpl;
import com.effektif.workflow.impl.workflow.ExpressionImpl;
import com.effektif.workflow.impl.workflow.ScopeImpl;
import com.effektif.workflow.impl.workflow.VariableImpl;
import com.effektif.workflow.impl.workflowinstance.ScopeInstanceImpl;


/**
 * @author Tom Baeyens
 */
public class FormBindings {
  
  private static final Logger log = LoggerFactory.getLogger(FormBindings.class);

  public Form form;
  public Map<String,FormFieldBinding> formFieldBindings;

  public void parse(Form form, WorkflowParser parser) {
    this.form = form;
    if (form!=null && form.getFields()!=null && !form.getFields().isEmpty()) {
      int index = 0;
      Set<String> fieldIds = collectFieldIds(form.getFields());
      for (FormField field: form.getFields()) {
        parser.pushContext("fields", field, null, index);
        Binding binding = field.getBinding();
        if (binding!=null) {
          BindingImpl bindingImpl = parser.parseBinding(binding, "binding");
          if ( !field.isReadOnly() 
               && ( bindingImpl.expression==null
                    || bindingImpl.expression.fields!=null ) ){
            parser.addWarning("Writable form fields should have an expression containing a variable id");
            
          } else {
            String variableId = null;
            if (bindingImpl!=null) {
              ExpressionImpl expression = bindingImpl.expression;
              if ( !field.isReadOnly() 
                   && expression!=null 
                   && expression.fields!=null ) {
                parser.addWarning("Writable form fields can only specify variables, not fields inside variables");
              }
              if ( field.getType()==null
                   && expression.type!=null ) {
                field.setType(expression.type.serialize());
              }
              if ( field.getName()==null
                   && expression!=null 
                   && expression.fields==null) {
                ScopeImpl scope = parser.getCurrentScope();
                VariableImpl variableImpl = scope.findVariableByIdRecursive(bindingImpl.expression.variableId);
                if (variableImpl!=null) {
                  variableId = variableImpl.id;
                  field.setName(variableImpl.variable.getName());
                }
              }
            }
            String fieldId = field.getId();
            if (fieldId==null) {
              fieldId = generateFieldId(fieldIds, variableId);
              field.setId(fieldId);
              fieldIds.add(fieldId);
            }
            addBinding(field, bindingImpl);
          }
        }
        parser.popContext();
        index++;
      }
    }
  }

  public void addBinding(FormField formField, BindingImpl binding) {
    if (formFieldBindings==null) {
      formFieldBindings = new HashMap<>();
    }
    formFieldBindings.put(formField.getId(), new FormFieldBinding(formField, binding));
  }

  protected String generateFieldId(Set<String> fieldIds, String variableId) {
    if (variableId!=null && !fieldIds.contains(variableId)) {
      return variableId;
    }
    Long fieldNumber = fieldIds.size()+1L;
    String fieldId = Long.toString(fieldNumber);
    while (fieldIds.contains(fieldId)) {
      fieldNumber = fieldNumber+1;
      fieldId = Long.toString(fieldNumber);
    }
    return fieldId;
  }

  protected Set<String> collectFieldIds(List<FormField> fields) {
    Set<String> fieldIds = new HashSet<>();
    for (FormField field: fields) {
      if (field.getId()!=null) {
        fieldIds.add(field.getId());
      }
    }
    return fieldIds;
  }

  public void applyFormInstanceData(FormInstance formInstance, ScopeInstanceImpl scopeInstance, boolean deserialize) {
    if (formInstance!=null && formInstance.getFields()!=null) {
      List<FormInstanceField> renderedFields = new ArrayList<>();
      for (FormInstanceField field: formInstance.getFields()) {
        Object value = field.getValue();
        FormFieldBinding formFieldBinding = formFieldBindings.get(field.getId());
        if (isWritable(formFieldBinding)) {
          String variableId = formFieldBinding.binding.expression.variableId;
          scopeInstance.setVariableValue(variableId, value, deserialize);
          FormField formField = formFieldBinding.formField;
          // The name and type are copied from the form to the form instance
          // This is for the start form rendering
          // The start form instance will later be added to the event
          field.setName(formField.getName());
          field.setType(formField.getType());
          renderedFields.add(field);
        }
      }
      formInstance.setFields(renderedFields);
    }
  }
  
  public FormInstance createFormInstance(ScopeInstanceImpl scopeInstance) {
    FormInstance formInstance = new FormInstance(form);
    if (formFieldBindings!=null) {
      for (String fieldId : formFieldBindings.keySet()) {
        FormFieldBinding formFieldBinding = formFieldBindings.get(fieldId);
        Object value = scopeInstance.getValue(formFieldBinding.binding);
        formInstance.value(fieldId, value);
      }
    }
    return formInstance;
  }

  protected boolean isWritable(FormFieldBinding formFieldBinding) {
    return formFieldBinding!=null
           && formFieldBinding.binding!=null
           && formFieldBinding.binding.expression!=null
           && formFieldBinding.binding.expression.variableId!=null
           && formFieldBinding.binding.expression.fields==null;
  }

  public void deserializeFormInstance(FormInstance formInstance) {
    if (formInstance==null) {
      return;
    }
    if (formInstance.getFields()!=null) {
      for (FormInstanceField field: formInstance.getFields()) {
        String fieldId = field.getId();
        FormFieldBinding formFieldBinding = formFieldBindings.get(fieldId);
        if (formFieldBinding!=null) {
          formFieldBinding.deserializeFormField(field);
        } else {
          log.debug("Ignoring undefined form field '"+fieldId+"'");
        }
      }
    }
  }
}
