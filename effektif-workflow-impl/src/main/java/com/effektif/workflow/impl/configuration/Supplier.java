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


/** Callback invoked by the {@link Brewery} when the brewery requires
 * a component to be be created.  This is like a factory method.
 * 
 * <p>This can be used if the created component is a singleton, but based on
 * other configurable components in the brewery. In this case ensure that
 * {@link #isSingleton()} returns true.</p>
 * 
 * <p>Or it can also be used when a new component needs to be created each
 * time it is requested from the brewery.  In this case, ensure that
 * {@link #isSingleton()} returns false.</p>
 */
public interface Supplier {

  /** Callback invoked by the {@link Brewery} when the brewery requires
   * a component to be be created.  This is like a factory method. */
  Object supply(Brewery brewery);

  /** Callback for the {@link Brewery} to know if the created components
   * need to be cached or not.  Singletons are cached in the brewery,
   * which means that only one component is created and the same one is
   * used for all situations where it is requested from the brewery.*/
  boolean isSingleton();
}
