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
package com.effektif.workflow.impl.configuration;


/** Callback that is invoked when the {@link Brewery} is started.
 * 
 * When the brewery is started, the collections suppliers, ingredients 
 * and brews will be scanned for Startables in that order.  
 * And each collection elements are scanned in order of addition.
 * 
 * {@link Brewery.start() Starting} of the brewery is done automatically 
 * when the first object is requested from the brewery.  But it's also 
 * possible to invoke start manually.   
 *    
 * This is different from Brewable because Brewable.brew is 
 * called when the object is actually being added as a brew (lazy initialization).
 * 
 * @author Tom Baeyens
 */
public interface Startable {

  /** called when the brewery is started. */
  void start(Brewery brewery);
}
