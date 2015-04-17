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
package com.effektif.workflow.api.types;

import com.effektif.workflow.api.mapper.TypeName;
import com.fasterxml.jackson.annotation.JsonTypeName;


/**
 * @author Tom Baeyens
 */
@JsonTypeName("list")
@TypeName("list")
public class ListType extends Type {

  protected Type elementType;

  public ListType() {
  }
  public ListType(Type elementType) {
    this.elementType = elementType;
  }

//  @Override
//  public void readJson(JsonReader r) {
//    elementType = r.readObject("elementType");
//    super.readJson(r);
//  }
//
//  @Override
//  public void writeJson(JsonWriter w) {
//    super.writeJson(w);
//    w.writeWritable("elementType", elementType);
//  }

  public Type getElementType() {
    return this.elementType;
  }
  public void setElementType(Type elementType) {
    this.elementType = elementType;
  }
  public ListType elementType(Type elementType) {
    this.elementType = elementType;
    return this;
  }
}
