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
package com.effektif.workflow.impl.json.types;

import java.util.Map;

import com.effektif.workflow.impl.json.PolymorphicMapping;
import com.effektif.workflow.impl.json.TypeMapping;


/**
 * @author Tom Baeyens
 */
public class PolymorphicBeanMapper extends AbstractBeanMapper {
  
  PolymorphicMapping polymorphicMapping;
  
  public PolymorphicBeanMapper(PolymorphicMapping polymorphicMapping) {
    super(polymorphicMapping.getBaseClass().getSimpleName());
    this.polymorphicMapping = polymorphicMapping;
  }

  @Override
  protected TypeMapping getTypeMapping(Map jsonObject) {
    return polymorphicMapping.getTypeMapping(jsonObject);
  }

  @Override
  protected TypeMapping getTypeMapping(Class beanClass) {
    return polymorphicMapping.getTypeMapping(beanClass);
  }
}
