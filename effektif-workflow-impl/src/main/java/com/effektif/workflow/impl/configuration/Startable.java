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
 * {@link Brewery.start() Starting} of the brewery must be done after 
 * all configuration is performed and before the first object is requested.
 *    
 * Implementation note: Because configuration subclasses can 
 * add and change the ingredients and configurations, it's not 
 * easy to know when the configuration is complete.  Therefore
 * we added checks on each method of the brewery to ensure it 
 * is initialized.  So the initialize method will be triggered
 * when the first object (any object) is requested from the brewery.
 * 
 * This is different from Brewable because Brewable.brew is 
 * called when the object is actually requested from the 
 * brewery (lazy initialization).
 * 
 * @author Tom Baeyens
 */
public interface Startable {

  /** called when the brewery is started. */
  void start(Brewery brewery);
}
