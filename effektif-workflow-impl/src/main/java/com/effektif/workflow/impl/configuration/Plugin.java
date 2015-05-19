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


/** Enables auto-loading of plugins into the brewery.
 * 
 * To auto-load things into the brewery, implement the Plugin interface 
 * and ensure the classname is in a resource file called
 * META-INF/services/com.effektif.workflow.impl.configuration.Plugin.
 * 
 * When a DefaultConfiguration is created, all plugins will be called 
 * giving them an opportunity to register ingredients and suppliers.
 * 
 * @author Tom Baeyens
 */
public interface Plugin {

  void plugin(Brewery brewery);
}
