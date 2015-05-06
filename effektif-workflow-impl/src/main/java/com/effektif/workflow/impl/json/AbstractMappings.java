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
package com.effektif.workflow.impl.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.effektif.workflow.api.json.JsonFieldName;
import com.effektif.workflow.api.json.JsonIgnore;
import com.effektif.workflow.api.json.JsonPropertyOrder;
import com.effektif.workflow.impl.json.types.BeanMapper;
import com.effektif.workflow.impl.json.types.PolymorphicBeanMapper;
import com.effektif.workflow.impl.util.Reflection;


/**
 * @author Tom Baeyens
 */
public class AbstractMappings {

  /** Maps registered base classes (like e.g. <code>Activity</code>) to *unparameterized* polymorphic mappings.
   * Polymorphic parameterized types are not yet supported.
   * Initialized from the mapping builder information */
  Map<Class<?>, PolymorphicMapping> polymorphicMappings = new HashMap<>();

  /** Type mappings contain the field mappings for each type.  
   * Types can be parameterized.
   * Dynamically initialized */ 
  Map<Type, TypeMapping> typeMappings = new HashMap<>();

  Map<Class<?>, TypeField> typeFields = new HashMap<>();

}
