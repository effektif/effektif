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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.effektif.workflow.api.activities.EmbeddedSubprocess;
import com.effektif.workflow.api.activities.EndEvent;
import com.effektif.workflow.api.activities.ExclusiveGateway;
import com.effektif.workflow.api.activities.HttpServiceTask;
import com.effektif.workflow.api.activities.JavaServiceTask;
import com.effektif.workflow.api.activities.NoneTask;
import com.effektif.workflow.api.activities.ParallelGateway;
import com.effektif.workflow.api.activities.ReceiveTask;
import com.effektif.workflow.api.condition.And;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.condition.Contains;
import com.effektif.workflow.api.condition.ContainsIgnoreCase;
import com.effektif.workflow.api.condition.Equals;
import com.effektif.workflow.api.condition.EqualsIgnoreCase;
import com.effektif.workflow.api.condition.GreaterThan;
import com.effektif.workflow.api.condition.GreaterThanOrEqual;
import com.effektif.workflow.api.condition.HasNoValue;
import com.effektif.workflow.api.condition.HasValue;
import com.effektif.workflow.api.condition.IsFalse;
import com.effektif.workflow.api.condition.IsTrue;
import com.effektif.workflow.api.condition.LessThan;
import com.effektif.workflow.api.condition.LessThanOrEqual;
import com.effektif.workflow.api.condition.Not;
import com.effektif.workflow.api.condition.NotContains;
import com.effektif.workflow.api.condition.NotContainsIgnoreCase;
import com.effektif.workflow.api.condition.NotEquals;
import com.effektif.workflow.api.condition.NotEqualsIgnoreCase;
import com.effektif.workflow.api.condition.Or;
import com.effektif.workflow.api.deprecated.acl.AccessIdentity;
import com.effektif.workflow.api.deprecated.acl.GroupIdentity;
import com.effektif.workflow.api.deprecated.acl.OrganizationIdentity;
import com.effektif.workflow.api.deprecated.acl.PublicIdentity;
import com.effektif.workflow.api.deprecated.acl.UserIdentity;
import com.effektif.workflow.api.deprecated.activities.ScriptTask;
import com.effektif.workflow.api.deprecated.activities.UserTask;
import com.effektif.workflow.api.deprecated.triggers.FormTrigger;
import com.effektif.workflow.api.serialization.bpmn.BpmnElement;
import com.effektif.workflow.api.serialization.bpmn.BpmnReader;
import com.effektif.workflow.api.serialization.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.serialization.bpmn.BpmnWriter;
import com.effektif.workflow.api.serialization.bpmn.XmlElement;
import com.effektif.workflow.api.serialization.json.JsonIgnore;
import com.effektif.workflow.api.serialization.json.JsonPropertyOrder;
import com.effektif.workflow.api.serialization.json.JsonWriter;
import com.effektif.workflow.api.serialization.json.TypeName;
import com.effektif.workflow.api.types.Type;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.api.workflow.Trigger;
import com.effektif.workflow.impl.deprecated.email.EmailTrigger;
import com.effektif.workflow.impl.deprecated.job.TaskEscalateJobType;
import com.effektif.workflow.impl.deprecated.job.TaskReminderJobType;
import com.effektif.workflow.impl.job.JobType;


/**
 * @author Tom Baeyens
 */
public class Mappings {
  
  private static final Logger log = LoggerFactory.getLogger(Mappings.class);

  Boolean isPretty;

  /** Maps registered base classes (e.g. <code>Trigger</code> to their subclass mappings. */
  Map<Class<?>, SubclassMapping> subclassMappings = new HashMap<>();

  Map<Class<?>, TypeField> typeFields = new HashMap<>();
  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();
  Map<Class<?>, Map<String,java.lang.reflect.Type>> fieldTypes = new HashMap<>();
  Map<Class<?>, List<Field>> fields = new HashMap<>();
  
  public Mappings() {
    registerBaseClass(Type.class, "name");
    registerBaseClass(Trigger.class);
    registerSubClass(FormTrigger.class);
    registerSubClass(EmailTrigger.class);

    registerBaseClass(Activity.class);
    registerSubClass(UserTask.class);
    registerSubClass(EmbeddedSubprocess.class);
    registerSubClass(EndEvent.class);
    registerSubClass(ExclusiveGateway.class);
    registerSubClass(HttpServiceTask.class);
    registerSubClass(JavaServiceTask.class);
    registerSubClass(NoneTask.class);
    registerSubClass(ParallelGateway.class);
    registerSubClass(ReceiveTask.class);
    registerSubClass(ScriptTask.class);

    registerBaseClass(AccessIdentity.class);
    registerSubClass(GroupIdentity.class);
    registerSubClass(OrganizationIdentity.class);
    registerSubClass(PublicIdentity.class);
    registerSubClass(UserIdentity.class);
    
    registerBaseClass(Condition.class);
    registerSubClass(And.class);
    registerSubClass(Or.class);
    registerSubClass(Not.class);
    registerSubClass(Contains.class);
    registerSubClass(NotContains.class);
    registerSubClass(ContainsIgnoreCase.class);
    registerSubClass(NotContainsIgnoreCase.class);
    registerSubClass(EqualsIgnoreCase.class);
    registerSubClass(NotEqualsIgnoreCase.class);
    registerSubClass(Equals.class);
    registerSubClass(NotEquals.class);
    registerSubClass(GreaterThan.class);
    registerSubClass(GreaterThanOrEqual.class);
    registerSubClass(LessThan.class);
    registerSubClass(LessThanOrEqual.class);
    registerSubClass(HasNoValue.class);
    registerSubClass(HasValue.class);
    registerSubClass(IsFalse.class);
    registerSubClass(IsTrue.class);

    registerBaseClass(JobType.class);
    registerSubClass(TaskEscalateJobType.class);
    registerSubClass(TaskReminderJobType.class);
  }

  public void registerBaseClass(Class<?> baseClass) {
    registerBaseClass(baseClass, "type");
  }

  public void registerBaseClass(Class<?> baseClass, String typeField) {
    SubclassMapping subclassMapping = new SubclassMapping(typeField);
    subclassMappings.put(baseClass, subclassMapping);
  }

  public void registerSubClass(Class<?> subClass) {
    TypeName typeName = subClass.getAnnotation(TypeName.class);
    if (typeName!=null) {
      registerSubClass(subClass, typeName.value(), subClass);
      if (Activity.class.isAssignableFrom(subClass) || Condition.class.isAssignableFrom(subClass)) {
        BpmnElement bpmnElement = subClass.getAnnotation(BpmnElement.class);
        if (bpmnElement!=null) {
          BpmnTypeMapping bpmnTypeMapping = new BpmnTypeMapping();
          String elementName = bpmnElement.value();
          bpmnTypeMapping.setBpmnElementName(elementName);
          bpmnTypeMapping.setType(subClass);
          Annotation[] annotations = subClass.getAnnotations();
          for (Annotation annotation: annotations) {
            if (annotation instanceof BpmnTypeAttribute) {
              BpmnTypeAttribute bpmnTypeAttribute = (BpmnTypeAttribute) annotation;
              bpmnTypeMapping.addBpmnTypeAttribute(bpmnTypeAttribute.attribute(), bpmnTypeAttribute.value());
            }
          }
          bpmnTypeMappingsByClass.put(subClass, bpmnTypeMapping);
  
          List<BpmnTypeMapping> typeMappings = bpmnTypeMappingsByElement.get(elementName);
          if (typeMappings==null) {
            typeMappings = new ArrayList<>();
            bpmnTypeMappingsByElement.put(elementName, typeMappings);
          }
          typeMappings.add(bpmnTypeMapping);
        } else {
          // throw new RuntimeException("No bpmn element specified on "+subclass);
        }
      }
    } else {
      for (Class<?> baseClass: subclassMappings.keySet()) {
        if (baseClass.isAssignableFrom(subClass)) {
          throw new RuntimeException(subClass.getName()+" does not declare "+TypeName.class.toString());
        }
      }
    }
  }

  public BpmnTypeMapping getBpmnTypeMapping(XmlElement activityXml, BpmnReaderImpl bpmnReaderImpl) {
    List<BpmnTypeMapping> typeMappings = bpmnTypeMappingsByElement.get(activityXml.getName());
    if (typeMappings!=null) {
      if (typeMappings.size()==1) {
        return typeMappings.get(0);
      } else if (!typeMappings.isEmpty()) {
        for (BpmnTypeMapping typeMapping: typeMappings) {
          if (isBpmnTypeAttributeMatch(activityXml, typeMapping, bpmnReaderImpl)) {
            return typeMapping;
          }
        }
      }
    }
    return null;
  }

  private boolean isBpmnTypeAttributeMatch(XmlElement xmlElement, BpmnTypeMapping typeMapping, BpmnReaderImpl bpmnReaderImpl) {
    Map<String, String> bpmnTypeAttributes = typeMapping.getBpmnTypeAttributes();
    if (bpmnTypeAttributes==null) {
      return true;
    }
    for (String localPart: bpmnTypeAttributes.keySet()) {
      String typeValue = bpmnTypeAttributes.get(localPart);
      // get the attribute value in the xml element
      String xmlValue = xmlElement.getAttribute(Bpmn.EFFEKTIF_URI, localPart);
      if (typeValue.equals(xmlValue)) {
        // only if there is a match we read (==remove) the the attribute from the xml element
        xmlElement.removeAttribute(Bpmn.EFFEKTIF_URI, localPart);
        return true;
      }
    }
    return false;
  }

  public void registerSubClass(Class<?> baseClass, String typeName, Class<?> subClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    if (subclassMapping!=null) {
      subclassMapping.registerSubclass(typeName, subClass);
      typeFields.put(subClass, new TypeField(subclassMapping.getTypeField(), typeName));
    }
    Class< ? > superClass = baseClass.getSuperclass();
    if (superClass!=null) {
      registerSubClass(superClass, typeName, subClass);
    }
    for (Class<?> i: baseClass.getInterfaces()) {
      registerSubClass(i, typeName, subClass);
    }
  }

  public <T> Class<T> getConcreteClass(Map<String,Object> jsonObject, Class<T> baseClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    return subclassMapping!=null ? (Class<T>) subclassMapping.getSubclass(jsonObject) : baseClass;
  }

  public <T> Class<T> getConcreteClass(BpmnReader bpmnReader, Class<T> baseClass) {
    SubclassMapping subclassMapping = subclassMappings.get(baseClass);
    return subclassMapping!=null ? (Class<T>) subclassMapping.getSubclass(bpmnReader) : baseClass;
  }

  public BpmnTypeMapping getBpmnTypeMapping(Class<?> subClass) {
    if (!bpmnTypeMappingsByClass.containsKey(subClass)) {
      throw new IllegalArgumentException("No BPMN type mapping defined for " + subClass.getName());
    }
    return bpmnTypeMappingsByClass.get(subClass);
  }

  public void writeTypeField(JsonWriter jsonWriter, Object o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      jsonWriter.writeString(typeField.getTypeField(), typeField.getTypeName());
    }
  }
  
  public void writeTypeAttribute(BpmnWriter bpmnWriter, Object o) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      bpmnWriter.writeStringAttributeEffektif(typeField.getTypeField(), typeField.getTypeName());
    }
  }
  
  public synchronized java.lang.reflect.Type getFieldType(Class< ? > clazz, String fieldName) {
    // could be cached in this mappings object
    java.lang.reflect.Type fieldType = getFieldTypeFromCache(clazz, fieldName);
    if (fieldType!=null) {
      return fieldType;
    }
    Map<String,java.lang.reflect.Type> fieldTypesForClass = fieldTypes.get(clazz);
    if (fieldTypesForClass==null) {
      fieldTypesForClass = new HashMap<>();
      fieldTypes.put(clazz, fieldTypesForClass);
    }
    fieldType = findFieldType(clazz, fieldName);
    if (fieldType==null) {
      throw new RuntimeException("Field "+clazz.getName()+"."+fieldName+" not found");
    }
    fieldTypesForClass.put(fieldName, fieldType);
    return fieldType;
  }

  private java.lang.reflect.Type findFieldType(Class< ? > clazz, String fieldName) {
    try {
      for (Field field: clazz.getDeclaredFields()) {
        if (field.getName().equals(fieldName)) {
          return field.getGenericType();
        }
      }
      if (clazz.getSuperclass()!=Object.class) {
        return findFieldType(clazz.getSuperclass(), fieldName);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return null;
  }

  private java.lang.reflect.Type getFieldTypeFromCache(Class< ? > type, String fieldName) {
    Map<String,java.lang.reflect.Type> types = fieldTypes.get(type);
    if (types==null) {
      return null;
    }
    return types.get(fieldName);
  }

  public List<Field> getAllFields(Class<?> type) {
    List<Field> allFields = fields.get(type);
    if (allFields!=null) {
      return allFields;
    }
    allFields = new ArrayList<>();
    scanFields(allFields, type);
    
    JsonPropertyOrder jsonPropertyOrder = type.getAnnotation(JsonPropertyOrder.class);
    if (jsonPropertyOrder!=null) {
      String[] fieldNamesOrder = jsonPropertyOrder.value();
      for (int i=fieldNamesOrder.length-1; i>=0; i--) {
        String fieldName = fieldNamesOrder[i];
        Field field = removeField(allFields, fieldName);
        if (field!=null) {
          allFields.add(0, field);
        }
      }
    }
    
    fields.put(type, allFields);
    return allFields;
  }

  private Field removeField(List<Field> allFields, String fieldName) {
    Iterator<Field> iterator = allFields.iterator();
    while (iterator.hasNext()) {
      Field field = iterator.next();
      if (field.getName().equals(fieldName)) {
        iterator.remove();
        return field;
      }
    }
    return null;
  }

  public void scanFields(List<Field> allFields, Class< ? > type) {
    Field[] declaredFields = type.getDeclaredFields();
    if (declaredFields!=null) {
      for (Field field: declaredFields) {
        if (!Modifier.isStatic(field.getModifiers())
            && field.getAnnotation(JsonIgnore.class)==null) {
          field.setAccessible(true);
          allFields.add(field);
        }
      }
    }
    Class< ? > superclass = type.getSuperclass();
    if (Object.class!=superclass) {
      scanFields(allFields, superclass);
    }
  }

  public boolean isPretty() {
    return Boolean.TRUE.equals(isPretty);
  }

  public void setPretty(Boolean isPretty) {
    this.isPretty = isPretty;
  }

  public void pretty() {
    this.isPretty = true;
  }

}
