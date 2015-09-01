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
package com.effektif.workflow.impl.data.types;

import com.effektif.workflow.api.json.TypeName;
import com.effektif.workflow.api.types.DataType;


/** an object type without any declared fields.
 * 
 * Any field can be used for any type of value, but the 
 * tooling doesn't know, and  
 * hence can't give support, on the fields.
 * 
 * Deserializing will happen to Map.
 * 
 * @author Tom Baeyens
 */
@TypeName("object")
public class ObjectType extends DataType {

  public static final ObjectType INSTANCE = new ObjectType();

}
