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


/** Enables dependency lookup.
 * 
 *  Rather then performing injection through reflection, the brewery 
 *  calls the {@link #brew(Brewery)} method before a Brewable object 
 *  is delivered by the brewery.  This way, an object can initialize 
 *  itself with other objects from the brewery.
 */
public interface Brewable {

  /** Called before the object is delivered by the brewery and allows
   * the Brewable to initialize (inject) itself with dependencies 
   * from the brewery. */
  void brew(Brewery brewery);
}
