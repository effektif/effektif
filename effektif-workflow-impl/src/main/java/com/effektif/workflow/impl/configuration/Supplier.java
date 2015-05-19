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
package com.effektif.workflow.impl.configuration;


/** factory constructing new objects that are requested from the brewery.
 * 
 * This can be used if the created object is a singleton, but based on 
 * other configurable objects in the brewery. In this case ensure that 
 * {@link #isSingleton()} returns true.
 * 
 * Or it can also be used when a new object needs to be created each 
 * time it is requested from the brewery.  In this case, ensure that 
 * {@link #isSingleton()} returns false.
 */
public interface Supplier {
  
  Object supply(Brewery brewery);
  
  boolean isSingleton();
}
