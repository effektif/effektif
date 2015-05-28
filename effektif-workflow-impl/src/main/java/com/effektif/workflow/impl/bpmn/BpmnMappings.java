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
package com.effektif.workflow.impl.bpmn;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.effektif.workflow.api.bpmn.BpmnElement;
import com.effektif.workflow.api.bpmn.BpmnTypeAttribute;
import com.effektif.workflow.api.bpmn.BpmnWriter;
import com.effektif.workflow.api.bpmn.XmlElement;
import com.effektif.workflow.api.condition.Condition;
import com.effektif.workflow.api.workflow.Activity;
import com.effektif.workflow.impl.json.Mappings;
import com.effektif.workflow.impl.json.MappingsBuilder;
import com.effektif.workflow.impl.json.PolymorphicMapping;
import com.effektif.workflow.impl.json.TypeField;


/**
 * @author Tom Baeyens
 */
public class BpmnMappings extends Mappings {

  Map<Class<?>, BpmnTypeMapping> bpmnTypeMappingsByClass = new HashMap<>();
  Map<String, List<BpmnTypeMapping>> bpmnTypeMappingsByElement = new HashMap<>();

  public BpmnMappings(MappingsBuilder mappingsBuilder) {
    super(mappingsBuilder);
    initializeBpmnMappingFields();
  }

  public BpmnMappings(Mappings mappings) {
    super(mappings);
    initializeBpmnMappingFields();
  }

  protected void initializeBpmnMappingFields() {
    PolymorphicMapping activityMapping = polymorphicMappings.get(Activity.class);
    for (Class activitySubclass: activityMapping.getSubClasses()) {
      registerBpmnSubclass(activitySubclass);
    }
    PolymorphicMapping conditionMapping = polymorphicMappings.get(Condition.class);
    for (Class conditionSubclass: conditionMapping.getSubClasses()) {
      registerBpmnSubclass(conditionSubclass);
    }
  }

  protected void registerBpmnSubclass(Class<?> subClass) {
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

  public void writeTypeAttribute(BpmnWriter bpmnWriter, Object o, String attributeName) {
    TypeField typeField = typeFields.get(o.getClass());
    if (typeField!=null) {
      bpmnWriter.writeStringAttributeEffektif(attributeName, typeField.getTypeName());
    }
  }

  
  public BpmnTypeMapping getBpmnTypeMapping(Class<?> subClass) {
    if (!bpmnTypeMappingsByClass.containsKey(subClass)) {
      throw new IllegalArgumentException("No BPMN type mapping defined for " + subClass.getName());
    }
    return bpmnTypeMappingsByClass.get(subClass);
  }

  public SortedSet<Class< ? >> getBpmnClasses() {
    // TODO only add condition classes
    SortedSet<Class< ? >> bpmnClasses = new TreeSet(new Comparator<Class<?>>() {
      public int compare(Class c1, Class c2) {
      return c1.getName().compareTo(c2.getName());
      }
    });
    bpmnClasses.addAll(bpmnTypeMappingsByClass.keySet());
    return bpmnClasses;
  }
}
